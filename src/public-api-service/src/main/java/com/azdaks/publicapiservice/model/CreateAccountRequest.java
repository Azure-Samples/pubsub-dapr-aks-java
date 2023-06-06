package com.azdaks.publicapiservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Jacksonized
public class CreateAccountRequest {
    private String owner;
    private double amount;

    public String toString() {
        return "Owner: " + owner + ", Amount: " + amount;
    }
}
