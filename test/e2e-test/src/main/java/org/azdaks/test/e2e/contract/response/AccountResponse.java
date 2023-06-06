package org.azdaks.test.e2e.contract.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Setter
@Jacksonized
public class AccountResponse {
    private String owner;
    private double amount;
}
