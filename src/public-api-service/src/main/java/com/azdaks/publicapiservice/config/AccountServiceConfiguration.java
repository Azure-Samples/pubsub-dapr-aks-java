package com.azdaks.publicapiservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;

@Configuration
public class AccountServiceConfiguration {

    @Bean
    public DaprClient getDaprClient() {
        return new DaprClientBuilder().build();
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        /**
         * This object mapper instance overrides the shared instance in CloudEvent class
         * To avoid serialization issues with the CloudEvent class, we need to apply their ObjectMapper configuration
         */
        return new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}