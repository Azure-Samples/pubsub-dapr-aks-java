package com.azdaks.publicapiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Logger;

@SpringBootApplication
public class PublicApiServiceApplication {

	private static final Logger logger = Logger.getLogger(PublicApiServiceApplication.class.getName());

	public static void main(String[] args) {
		SpringApplication.run(PublicApiServiceApplication.class, args);

		var version = System.getenv().getOrDefault("APP_VERSION", "!!!!UNKOWN!!!!");
		logger.info("Public API Service Started, version: %s".formatted(version));
	}

}
