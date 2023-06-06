package com.azdaks.publicapiservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Builder
@Jacksonized
public class MoneyTransfer {
    private String sender;
    private String receiver;
    private double amount;
    private String transferId;
    private String status;
}
