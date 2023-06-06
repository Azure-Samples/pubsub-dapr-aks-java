package com.azdaks.fraudservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;

@Configuration
public class FraudServiceConfiguration {

    @Bean
    public DaprClient getDaprClient() {
        return new DaprClientBuilder().build();
    }
}