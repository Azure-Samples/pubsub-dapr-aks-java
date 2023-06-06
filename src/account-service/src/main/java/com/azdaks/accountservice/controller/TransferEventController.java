package com.azdaks.accountservice.controller;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.CloudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.azdaks.accountservice.model.CreateAccountRequest;
import com.azdaks.accountservice.model.StateRequest;
import com.azdaks.accountservice.model.TransferRequest;

import reactor.core.publisher.Mono;

@RestController
public class TransferEventController {

    private static final Logger logger = LoggerFactory.getLogger(TransferEventController.class);

    private static final String PUBSUB_NAME = "money-transfer-pubsub";
    private static final String TRANSFER_SUBSCRIBED_TOPIC_NAME = "validated";
    private static final String STATE_TOPIC_NAME = "state";
    private static final String PUBLISHED_TOPIC_NAME = "approved";
    private static final String STATE_STORE = "money-transfer-state";
    private static final String ACCOUNT_UPDATE_TOPIC = "account-update";

    @Autowired
    DaprClient client;

    @Topic(name = TRANSFER_SUBSCRIBED_TOPIC_NAME, pubsubName = PUBSUB_NAME)
    @PostMapping(path = "/transfers", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity> handleTransferRequest(@RequestBody(required = false) CloudEvent<TransferRequest> cloudEvent) {
        return Mono.fromSupplier(() -> {
            try {
                logger.info("Account service transfer request received: " + cloudEvent.getData().toString());

                var transferRequest = cloudEvent.getData();
                var sender = transferRequest.getSender();

                var stateRequest = StateRequest.builder()
                        .transferId(transferRequest.getTransferId())
                        .status("APPROVED")
                        .build();

                logger.info("Retrieving account limit: " + sender);

                var accountAmount = client.getState(STATE_STORE, sender, Double.class).block();
                var amount = accountAmount.getValue();

                logger.info("Account limit: " + sender + " Amount: " + amount);

                if (amount - transferRequest.getAmount() < 0) {
                    logger.error("Insufficient funds, amount has to be less than " + amount);
                    publishStateRequest(stateRequest, "INSUFFICIENT_FUNDS");

                    return ResponseEntity.ok("SUCCESS");
                }

                var newAmount = amount - transferRequest.getAmount();
                client.saveState(STATE_STORE, sender, newAmount).block();

                logger.info("Account limit updated: " + transferRequest.getSender() + ", new amount: " + newAmount);

                var createAccountRequest = CreateAccountRequest.builder()
                        .amount(newAmount)
                        .owner(transferRequest.getSender())
                        .build();

                logger.info("Publishing Account Limit Updated: " + createAccountRequest);
                client.publishEvent(PUBSUB_NAME, ACCOUNT_UPDATE_TOPIC, createAccountRequest).block();

                logger.info("Publishing state request: " + stateRequest);
                publishStateRequest(stateRequest, "APPROVED");

                logger.info("Publishing transfer request: " + transferRequest);
                client.publishEvent(PUBSUB_NAME, PUBLISHED_TOPIC_NAME, transferRequest).block();
                

                return ResponseEntity.ok("SUCCESS");

            } catch (Exception e) {

                logger.error("Error while processing transfer request: " + e.getMessage());
                return ResponseEntity.ok("SUCCESS");
            }
        });
    }

    private void publishStateRequest(StateRequest stateRequest, String newStatus) {
        stateRequest.setStatus(newStatus);

        logger.info("Publishing state request: " + stateRequest);
        client.publishEvent(PUBSUB_NAME, STATE_TOPIC_NAME, stateRequest).block();
    }
}
