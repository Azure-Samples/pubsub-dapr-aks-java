package com.azdaks.publicapiservice.model;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Builder
@Jacksonized
public class AccountResponse {
    private String owner;
    private double amount;

    public String toString() {
        return "Owner: " + owner + ", Amount: " + amount;
    }

    public static String generateId() {
        return NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, "abcdef12345".toCharArray(), 5);
    }
}
