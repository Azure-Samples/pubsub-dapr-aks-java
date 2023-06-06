package org.azdaks.test.e2e.api;

import lombok.Builder;
import lombok.Getter;

import java.net.http.HttpResponse;

@Builder
@Getter
public class ApiResponse<T> {
    HttpResponse<String> response;
    T body;
}
