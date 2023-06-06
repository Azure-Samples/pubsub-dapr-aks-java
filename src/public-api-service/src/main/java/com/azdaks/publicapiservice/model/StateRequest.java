package com.azdaks.publicapiservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Builder
@Jacksonized
public class StateRequest {
    private String transferId;
    private String status;

    public String toString() {
        return "TransferId: " + transferId + ", Status: " + status;
    }
}
