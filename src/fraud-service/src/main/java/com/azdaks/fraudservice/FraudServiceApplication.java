package com.azdaks.fraudservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Logger;

@SpringBootApplication
public class FraudServiceApplication {

	private static final Logger logger = Logger.getLogger(FraudServiceApplication.class.getName());

	public static void main(String[] args) {
		SpringApplication.run(FraudServiceApplication.class, args);

		var version = System.getenv().getOrDefault("APP_VERSION", "!!!!UNKOWN!!!!");
		logger.info("Fraud Service Started, version: %s".formatted(version));
	}

}
