package com.azdaks.publicapiservice.controller;

import com.azdaks.publicapiservice.model.MoneyTransfer;
import com.azdaks.publicapiservice.model.StateRequest;
import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.dapr.client.domain.State;
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
public class StatesController {

    private static final String PUBSUB_NAME = "money-transfer-pubsub";
    private static final String SUBSCRIBED_TOPIC_NAME = "state";
    private static final String STATE_STORE = "money-transfer-state";

    private static final Logger logger = LoggerFactory.getLogger(StatesController.class.getName());

    @Autowired
    DaprClient client;

    @Topic(name = SUBSCRIBED_TOPIC_NAME, pubsubName = PUBSUB_NAME)
    @PostMapping(path = "/states", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity> handleStateRequest(@RequestBody(required = false) CloudEvent<StateRequest> cloudEvent) {
        return Mono.fromSupplier(() -> {
            try {
                logger.info("Update state request: " + cloudEvent.getData().toString());
                var stateRequest = cloudEvent.getData();

                logger.info("Getting state from state store: " + stateRequest.getTransferId());
                State<MoneyTransfer> moneyTransferState = client
                        .getState(STATE_STORE, stateRequest.getTransferId(), MoneyTransfer.class).block();
                var moneyTransfer = moneyTransferState.getValue();
                moneyTransfer.setStatus(stateRequest.getStatus());

                logger.info("Saving state to state store: " + moneyTransfer.toString());
                client.saveState(STATE_STORE, stateRequest.getTransferId(), moneyTransfer).block();

                return ResponseEntity.ok("SUCCESS");
            } catch (Exception e) {

                logger.error("Error while processing transfer request: " + e.getMessage());
                return ResponseEntity.badRequest().body("ERROR PROCESSING STATE REQUEST");
            }
        });
    }
}
