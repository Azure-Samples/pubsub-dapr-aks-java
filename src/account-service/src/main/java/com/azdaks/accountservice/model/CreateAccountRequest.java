package com.azdaks.accountservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Jacksonized
@Builder
public class CreateAccountRequest {
    private String owner;
    private double amount;

    public String toString() {
        return "Owner: " + owner + ", Amount: " + amount;
    }
}
