package com.azdaks.publicapiservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TransferResponse {
    private String message;
    private String status;
    private String transferId;


}
