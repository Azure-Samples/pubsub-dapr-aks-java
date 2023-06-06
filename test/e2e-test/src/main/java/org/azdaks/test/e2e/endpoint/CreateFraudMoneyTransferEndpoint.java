package org.azdaks.test.e2e.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.azdaks.test.e2e.TestSettings;
import org.azdaks.test.e2e.api.ApiRequest;
import org.azdaks.test.e2e.contract.request.CreateTransferRequest;
import org.azdaks.test.e2e.util.Print;

import java.io.IOException;
import java.net.http.HttpRequest;

public class CreateFraudMoneyTransferEndpoint implements Endpoint {

    @Override
    public HttpRequest createRequest(TestSettings settings, ObjectMapper objectMapper) throws IOException {
        var createTransferRequest = CreateTransferRequest.builder()
                .sender(settings.getOwner())
                .receiver("Receiver")
                .amount(settings.getFraudAmount())
                .build();

        var payload = objectMapper.writeValueAsString(createTransferRequest);
        Print.request(payload);

        return ApiRequest.buildPostRequest(settings.getApiUrl() + "/transfers", payload);
    }
}
