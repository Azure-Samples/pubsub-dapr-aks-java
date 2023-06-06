package com.azdaks.fraudservice.controller;

import com.azdaks.fraudservice.model.StateRequest;
import com.azdaks.fraudservice.model.TransferRequest;
import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TransferEventController {

    private static final Logger logger = LoggerFactory.getLogger(TransferEventController.class);

    private static final String PUBSUB_NAME = "money-transfer-pubsub";
    private static final String SUBSCRIBED_TOPIC_NAME = "transfer";
    private static final String STATE_TOPIC_NAME = "state";
    private static final String PUBLISHED_TOPIC_NAME = "validated";

    @Autowired
    DaprClient client;

    @Topic(name = SUBSCRIBED_TOPIC_NAME, pubsubName = PUBSUB_NAME)
    @PostMapping(path = "/transfers", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity> handleTransferRequest(
            @RequestBody(required = false) CloudEvent<TransferRequest> cloudEvent) {
        return Mono.fromSupplier(() -> {
            try {
                logger.info("Fraud service received: " + cloudEvent.getData().toString());

                var transferRequest = cloudEvent.getData();

                var stateRequest = StateRequest.builder()
                        .transferId(transferRequest.getTransferId())
                        .status("ACCEPTED")
                        .build();

                if (transferRequest.getAmount() > 1000) {
                    logger.error("Fraud detected, amount has to be less than 1000");
                    publishStateRequest(stateRequest, "REJECTED");

                    return ResponseEntity.ok("SUCCESS");
                }

                logger.info("Validated, amount is less than 1000");
                publishStateRequest(stateRequest, "VALIDATED");

                logger.info("Publishing validated transfer request: " + transferRequest);
                client.publishEvent(PUBSUB_NAME, PUBLISHED_TOPIC_NAME, transferRequest).block();

                return ResponseEntity.ok("SUCCESS");

            } catch (Exception e) {

                logger.error("Error while processing transfer request: " + e.getMessage());
                return ResponseEntity.badRequest().body("ERROR PROCESSING TRANSFER REQUEST");
            }
        });
    }

    private void publishStateRequest(StateRequest stateRequest, String newStatus) {
        stateRequest.setStatus(newStatus);

        logger.info("Publishing state request: " + stateRequest);
        client.publishEvent(PUBSUB_NAME, STATE_TOPIC_NAME, stateRequest).block();
    }
}
