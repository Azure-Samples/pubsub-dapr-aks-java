package org.azdaks.test.e2e.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.azdaks.test.e2e.TestSettings;
import org.azdaks.test.e2e.api.ApiRequest;
import org.azdaks.test.e2e.contract.request.CreateAccountRequest;
import org.azdaks.test.e2e.util.Print;

import java.io.IOException;
import java.net.http.HttpRequest;

public class CreateAccountEndpoint implements Endpoint {

    @Override
    public HttpRequest createRequest(TestSettings settings, ObjectMapper objectMapper) throws IOException {
        var createAccountRequest = CreateAccountRequest.builder()
                .owner(settings.getOwner())
                .amount(settings.getAmount())
                .build();

        var payload = objectMapper.writeValueAsString(createAccountRequest);
        Print.request(payload);

        return ApiRequest.buildPostRequest(settings.getApiUrl() + "/accounts", payload);
    }
}
