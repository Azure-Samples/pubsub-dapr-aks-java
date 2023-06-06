package org.azdaks.test.e2e.contract.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CreateTransferRequest {
    private String sender;
    private String receiver;
    private double amount;
}

