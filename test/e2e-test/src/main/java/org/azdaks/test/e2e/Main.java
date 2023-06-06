package org.azdaks.test.e2e;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.util.Random;

public class Main {

    private static final String API_URL = "http://localhost:8080";
    private static final int TIMEOUT_SECONDS = 10;

    public static void main(String[] args) throws Exception {

        var owner = NanoIdUtils.randomNanoId(new Random(), NanoIdUtils.DEFAULT_ALPHABET, 10);
        var amount = 1000.0d;
        var transferAmount = 100.0d;
        var fraudAmount = 10000.0d;

        var testSettings = TestSettings.builder()
                .apiUrl(API_URL)
                .timeoutSeconds(TIMEOUT_SECONDS)
                .owner(owner)
                .amount(amount)
                .transferAmount(transferAmount)
                .fraudAmount(fraudAmount)
                .build();

        new TestRunner(testSettings).run();
    }


}