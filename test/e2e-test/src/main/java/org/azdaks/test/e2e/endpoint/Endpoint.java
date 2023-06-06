package org.azdaks.test.e2e.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.azdaks.test.e2e.TestSettings;

import java.io.IOException;
import java.net.http.HttpRequest;

public interface Endpoint {
    HttpRequest createRequest(TestSettings settings, ObjectMapper objectMapper) throws IOException;
}
