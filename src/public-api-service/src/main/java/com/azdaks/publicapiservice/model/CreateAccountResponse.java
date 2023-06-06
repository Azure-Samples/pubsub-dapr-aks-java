package com.azdaks.publicapiservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Setter
@Jacksonized
public class CreateAccountResponse {
    private AccountResponse account;
    private String message;
}
