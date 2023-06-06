package org.azdaks.test.e2e.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.azdaks.test.e2e.TestSettings;
import org.azdaks.test.e2e.api.ApiRequest;

import java.io.IOException;
import java.net.http.HttpRequest;

public class HomeEndpoint implements Endpoint {
    @Override
    public HttpRequest createRequest(TestSettings settings, ObjectMapper objectMapper) throws IOException {
        return ApiRequest.buildGetRequest(settings.getApiUrl());
    }

}
