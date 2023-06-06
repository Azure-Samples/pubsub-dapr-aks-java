package com.azdaks.notificationservice.controller;

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

import com.azdaks.notificationservice.model.StateRequest;
import com.azdaks.notificationservice.model.TransferRequest;

import reactor.core.publisher.Mono;

@RestController
public class TransferEventController {

    private static final Logger logger = LoggerFactory.getLogger(TransferEventController.class);

    private static final String PUBSUB_NAME = "money-transfer-pubsub";
    private static final String TRANSFER_SUBSCRIBED_TOPIC_NAME = "approved";
    private static final String STATE_TOPIC_NAME = "state";

    @Autowired
    DaprClient client;

    @Topic(name = TRANSFER_SUBSCRIBED_TOPIC_NAME, pubsubName = PUBSUB_NAME)
    @PostMapping(path = "/transfers", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity> handleTransferRequest(
            @RequestBody(required = false) CloudEvent<TransferRequest> cloudEvent) {
        return Mono.fromSupplier(() -> {
            try {
                logger.info("Notification service transfer request received: " + cloudEvent.getData().toString());

                var transferRequest = cloudEvent.getData();

                var stateRequest = StateRequest.builder()
                        .transferId(transferRequest.getTransferId())
                        .status("COMPLETED")
                        .build();

                logger.info("Publishing state request: " + stateRequest);
                publishStateRequest(stateRequest, "COMPLETED");

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
