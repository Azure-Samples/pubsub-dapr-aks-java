package com.azdaks.accountservice.controller;

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

import com.azdaks.accountservice.model.CreateAccountRequest;
import reactor.core.publisher.Mono;

@RestController
public class AccountsEventController {

    private static final Logger logger = LoggerFactory.getLogger(AccountsEventController.class);

    private static final String STATE_STORE = "money-transfer-state";
    private static final String PUBSUB_NAME = "money-transfer-pubsub";
    private static final String SUBSCRIBED_TOPIC_NAME = "deposit";

    @Autowired
    DaprClient client;

    @Topic(name = SUBSCRIBED_TOPIC_NAME, pubsubName = PUBSUB_NAME)
    @PostMapping(path = "/accounts", consumes = MediaType.ALL_VALUE)
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
