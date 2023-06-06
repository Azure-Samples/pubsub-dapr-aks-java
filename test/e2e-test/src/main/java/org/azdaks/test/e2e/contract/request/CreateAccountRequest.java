package org.azdaks.test.e2e.contract.request;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Jacksonized
public class CreateAccountRequest {
    private String owner;
    private double amount;
}
