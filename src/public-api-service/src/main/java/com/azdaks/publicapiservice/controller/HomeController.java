package com.azdaks.publicapiservice.controller;

import com.azdaks.publicapiservice.model.HomeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class.getName());

    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HomeResponse> getHome() {

        logger.info("Ping Request Received");

        var version = System.getenv().getOrDefault("APP_VERSION", "!!!!UNKNOWN!!!!");
        var message = "Public API Service Started, version: %s".formatted(version);

        logger.info("Ping Response:" + message);

        return ResponseEntity.ok(HomeResponse.builder().message(message).build());
    }
}

