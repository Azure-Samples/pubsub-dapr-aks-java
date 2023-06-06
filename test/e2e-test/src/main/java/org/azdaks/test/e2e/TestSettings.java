package org.azdaks.test.e2e;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class TestSettings {
    private String apiUrl;
    private int timeoutSeconds;

    @Setter
    private String owner;

    @Setter
    private double amount;

    @Setter
    private double transferAmount;
    private double fraudAmount;


    @Setter
    private String transferId;
}
