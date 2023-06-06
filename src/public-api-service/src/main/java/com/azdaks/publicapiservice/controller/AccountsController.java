package com.azdaks.publicapiservice.controller;

import com.azdaks.publicapiservice.model.CreateAccountResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.azdaks.publicapiservice.model.AccountResponse;
import com.azdaks.publicapiservice.model.CreateAccountRequest;
import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class AccountsController {

    private static final String STATE_STORE = "money-transfer-state";
    private static final String PUBSUB_NAME = "money-transfer-pubsub";
    private static final String TOPIC_NAME = "deposit";
    private static final String ACCOUNT_UPDATE_TOPIC = "account-update";

    private static final Logger logger = LoggerFactory.getLogger(AccountsController.class.getName());

    @Autowired
    DaprClient client;

    // Create Transfer Request Endpoint
    @PostMapping(path = "/accounts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateAccountResponse> createAccount(@RequestBody CreateAccountRequest request) {

        logger.info("Create Account Request Received");

        var message = "Account created for: " + request;

        logger.info(String.format("Saving to State: Owner: %s, Amount: %f", request.getOwner(), request.getAmount()));
        client.saveState(STATE_STORE, request.getOwner(), request.getAmount()).block();

        logger.info("Publishing event to Dapr Pub/Sub Broker: %s, %s".formatted(PUBSUB_NAME, request.toString()));
        client.publishEvent(PUBSUB_NAME, TOPIC_NAME, request).block();

        return ResponseEntity.ok(CreateAccountResponse.builder()
                .account(AccountResponse.builder()
                        .owner(request.getOwner())
                        .amount(request.getAmount())
                        .build())
                .message(message)
                .build());
    }

    @GetMapping(path = "/accounts/{owner}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String owner) {

        logger.info("Get Account Request Received");

        var accountAmount = client.getState(STATE_STORE, owner, Double.class).block();

        return ResponseEntity.ok(AccountResponse.builder()
                .owner(owner)
                .amount(accountAmount.getValue())
                .build());
    }

    @Topic(name = ACCOUNT_UPDATE_TOPIC, pubsubName = PUBSUB_NAME)
    @PostMapping(path = "/account-updates", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity> handleAccountRequest(
            @RequestBody(required = false) CloudEvent<CreateAccountRequest> cloudEvent) {
        return Mono.fromSupplier(() -> {
            try {
                logger.info("Account update received: " + cloudEvent.getData().toString());

                var request = cloudEvent.getData();

                logger.info(String.format("Saving to State: Owner: %s, Amount: %,.2f", request.getOwner(),
                        request.getAmount()));
                client.saveState(STATE_STORE, request.getOwner(), request.getAmount()).block();

                return ResponseEntity.ok("SUCCESS");

            } catch (Exception e) {

                logger.error("Error while processing account update request: " + e.getMessage());
                return ResponseEntity.ok("SUCCESS");
            }
        });
    }
}
