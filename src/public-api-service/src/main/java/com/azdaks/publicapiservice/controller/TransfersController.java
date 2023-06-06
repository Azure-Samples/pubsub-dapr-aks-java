package com.azdaks.publicapiservice.controller;

import com.azdaks.publicapiservice.model.MoneyTransfer;
import com.azdaks.publicapiservice.model.TransferRequest;
import com.azdaks.publicapiservice.model.TransferResponse;
import com.azdaks.publicapiservice.model.TransferStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TransfersController {

    private static final String PUBSUB_NAME = "money-transfer-pubsub";
    private static final String TOPIC_NAME = "transfer";
    private static final String STATE_STORE = "money-transfer-state";

    private static final Logger logger = LoggerFactory.getLogger(TransfersController.class.getName());

    @Autowired
    DaprClient client;

    @Autowired
    ObjectMapper objectMapper;

    // Create Transfer Request Endpoint
    @PostMapping(path = "/transfers", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransferResponse> createTransferRequest(@RequestBody TransferRequest transferRequest) {

        logger.info("Transfer Request Received");

        var message = "Transfer Request Started: " + transferRequest;
        var status = TransferStatus.ACCEPTED;
        var transferId = TransferRequest.generateId();
        transferRequest.setTransferId(transferId);

        try {
            /**
             * The official Dapr Java SDK does not support the latest reactor version in
             * Spring Boot 3+
             * Instead, we go with HTTP client to publish events to Dapr Pub/Sub for Spring
             * Boot 3+
             * https://github.com/dapr/java-sdk/issues/815
             */

            logger.info("Publishing event to Dapr Pub/Sub Broker: %s, %s".formatted(PUBSUB_NAME,
                    transferRequest.toString()));
            client.publishEvent(PUBSUB_NAME, TOPIC_NAME, transferRequest).block();

            var moneyTransfer = MoneyTransfer.builder()
                    .transferId(transferId)
                    .status(status)
                    .amount(transferRequest.getAmount())
                    .sender(transferRequest.getSender())
                    .receiver(transferRequest.getReceiver())
                    .build();

            logger.info("Publishing event to Dapr State: %s".formatted(STATE_STORE));
            client.saveState(STATE_STORE, transferId, moneyTransfer).block();

        } catch (Exception e) {
            logger.error("Error publishing message: ");

            status = TransferStatus.REJECTED;
            message = e.getMessage();
        }

        var response = TransferResponse
                .builder()
                .message(message)
                .status(status)
                .transferId(transferId)
                .build();

        logger.info("Transfer Request Published");

        return status.equals(TransferStatus.ACCEPTED)
                ? ResponseEntity.accepted().body(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping(path = "/transfers/{transferId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTransferRequest(@PathVariable String transferId) {

        try {
            logger.info("Getting state from Dapr State: %s".formatted(STATE_STORE));
            State<MoneyTransfer> moneyTransferState = client.getState(STATE_STORE, transferId, MoneyTransfer.class)
                    .block();
            var moneyTransfer = moneyTransferState.getValue();

            logger.info("State: " + moneyTransfer);
            logger.info("Error: " + moneyTransferState.getError());
            logger.info("Key: " + moneyTransferState.getKey());

            if (moneyTransferState.getError() == null) {
                return ResponseEntity.ok(objectMapper.writeValueAsString(moneyTransfer));
            }

            return ResponseEntity.badRequest().body(moneyTransferState.getError());

        } catch (Exception e) {
            logger.error("Error getting state: ");

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
