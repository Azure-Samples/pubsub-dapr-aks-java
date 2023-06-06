package com.azdaks.accountservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Jacksonized
public class TransferRequest {
    private String sender;
    private String receiver;
    private double amount;
    private String transferId;

    public String toString() {
        return "ID" + transferId + "Sender: " + sender + ", Receiver: " + receiver + ", Amount: " + amount;
    }
}