package org.azdaks.test.e2e.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import org.azdaks.test.e2e.TestSettings;
import org.azdaks.test.e2e.endpoint.Endpoint;
import org.azdaks.test.e2e.util.Print;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

@Builder
@Getter
public class ApiClient<T> {
    private TestSettings settings;
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private Endpoint endpoint;

    public ApiResponse<T> send(Class<T> response) throws IOException, InterruptedException {
        var request = endpoint.createRequest(settings, objectMapper);
        var result = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Print.response(result.body());

        var body = objectMapper.readValue(result.body(), response);

        return ApiResponse.<T>builder()
                .response(result)
                .body(body)
                .build();
    }
}
