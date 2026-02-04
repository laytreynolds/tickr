package com.tickr.tickr.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder class for constructing URLs and sending HTTP requests.
 * Supports query parameters, request bodies, and various HTTP methods.
 */
@Slf4j
@Component
public class HttpRequestBuilder {

    private final RestTemplate restTemplate;
    private String url;
    private final Map<String, String> queryParams = new HashMap<>();
    private HttpHeaders headers = new HttpHeaders();
    private Object body;
    private HttpMethod method = HttpMethod.GET;

    public HttpRequestBuilder(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sets the base URL for the request.
     *
     * @param url The base URL
     * @return This builder instance
     */
    public HttpRequestBuilder url(String url) {
        this.url = url;
        return this;
    }

    /**
     * Adds a query parameter to the URL.
     *
     * @param key   The parameter key
     * @param value The parameter value
     * @return This builder instance
     */
    public HttpRequestBuilder param(String key, String value) {
        if (value != null) {
            this.queryParams.put(key, value);
        }
        return this;
    }

    /**
     * Adds multiple query parameters to the URL.
     *
     * @param params Map of parameter key-value pairs
     * @return This builder instance
     */
    public HttpRequestBuilder params(Map<String, String> params) {
        if (params != null) {
            params.forEach((key, value) -> {
                if (value != null) {
                    this.queryParams.put(key, value);
                }
            });
        }
        return this;
    }

    /**
     * Sets a request header.
     *
     * @param key   The header name
     * @param value The header value
     * @return This builder instance
     */
    public HttpRequestBuilder header(String key, String value) {
        this.headers.set(key, value);
        return this;
    }

    /**
     * Sets multiple request headers.
     *
     * @param headers Map of header key-value pairs
     * @return This builder instance
     */
    public HttpRequestBuilder headers(Map<String, String> headers) {
        if (headers != null) {
            headers.forEach(this.headers::set);
        }
        return this;
    }

    /**
     * Sets the Content-Type header.
     *
     * @param contentType The content type (e.g., "application/json")
     * @return This builder instance
     */
    public HttpRequestBuilder contentType(String contentType) {
        this.headers.setContentType(MediaType.parseMediaType(contentType));
        return this;
    }

    /**
     * Sets the Authorization header.
     *
     * @param authValue The authorization value (e.g., "Basic base64string" or "Bearer token")
     * @return This builder instance
     */
    public HttpRequestBuilder authorization(String authValue) {
        this.headers.set("Authorization", authValue);
        return this;
    }

    /**
     * Sets the request body.
     *
     * @param body The request body object
     * @return This builder instance
     */
    public HttpRequestBuilder body(Object body) {
        this.body = body;
        return this;
    }

    /**
     * Sets the HTTP method to GET.
     *
     * @return This builder instance
     */
    public HttpRequestBuilder get() {
        this.method = HttpMethod.GET;
        return this;
    }

    /**
     * Sets the HTTP method to POST.
     *
     * @return This builder instance
     */
    public HttpRequestBuilder post() {
        this.method = HttpMethod.POST;
        return this;
    }

    /**
     * Sets the HTTP method to PUT.
     *
     * @return This builder instance
     */
    public HttpRequestBuilder put() {
        this.method = HttpMethod.PUT;
        return this;
    }

    /**
     * Sets the HTTP method to DELETE.
     *
     * @return This builder instance
     */
    public HttpRequestBuilder delete() {
        this.method = HttpMethod.DELETE;
        return this;
    }

    /**
     * Sets the HTTP method.
     *
     * @param method The HTTP method
     * @return This builder instance
     */
    public HttpRequestBuilder method(HttpMethod method) {
        this.method = method;
        return this;
    }

    /**
     * Builds the full URL with query parameters.
     *
     * @return The complete URL string
     */
    private String buildUrl() {
        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("Base URL must be set");
        }

        if (queryParams.isEmpty()) {
            return url;
        }

        StringBuilder urlBuilder = new StringBuilder(url);
        boolean firstParam = true;

        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (firstParam) {
                urlBuilder.append("?");
                firstParam = false;
            } else {
                urlBuilder.append("&");
            }

            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
            urlBuilder.append(encodedKey).append("=").append(encodedValue);
        }

        return urlBuilder.toString();
    }

    /**
     * Sends the HTTP request and returns the response.
     *
     * @return HttpResponse containing the response body and status code
     * @throws RestClientException if the request fails
     */
    public HttpResponse execute() {
        String url = buildUrl();

        // Set default Content-Type if body is present and Content-Type not set
        if (body != null && headers.getContentType() == null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        log.debug("Sending {} request to: {}", method, url);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    method,
                    entity,
                    String.class
            );

            return new HttpResponse(
                    response.getStatusCode().value(),
                    response.getBody(),
                    response.getHeaders()
            );
        } catch (RestClientException e) {
            log.error("HTTP request failed: {} {}", method, url, e);
            throw e;
        }
    }

    /**
     * Response class containing status code, body, and headers.
     */
    public static class HttpResponse {
        private final int statusCode;
        private final String body;
        private final HttpHeaders headers;

        public HttpResponse(int statusCode, String body, HttpHeaders headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers != null ? headers : new HttpHeaders();
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public HttpHeaders getHeaders() {
            return headers;
        }

        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }

        @Override
        public String toString() {
            return "HttpResponse{" +
                    "statusCode=" + statusCode +
                    ", body='" + (body != null && body.length() > 100 ? body.substring(0, 100) + "..." : body) + '\'' +
                    '}';
        }
    }
}
