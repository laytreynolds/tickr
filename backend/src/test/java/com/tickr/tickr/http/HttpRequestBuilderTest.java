package com.tickr.tickr.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("HttpRequestBuilder")
class HttpRequestBuilderTest {

    @Mock
    private RestTemplate restTemplate;

    private HttpRequestBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new HttpRequestBuilder(restTemplate);
    }

    @Nested
    @DisplayName("URL building")
    class UrlBuilding {

        @Test
        @DisplayName("should throw when URL is not set")
        void shouldThrowWhenUrlNotSet() {
            assertThatThrownBy(() -> builder.get().execute())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Base URL must be set");
        }

        @Test
        @DisplayName("should throw when URL is empty")
        void shouldThrowWhenUrlEmpty() {
            assertThatThrownBy(() -> builder.url("").get().execute())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Base URL must be set");
        }

        @Test
        @DisplayName("should build URL without params")
        void shouldBuildUrlWithoutParams() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            given(restTemplate.exchange(eq("https://api.example.com"), any(), any(), eq(String.class)))
                    .willReturn(mockResponse);

            HttpRequestBuilder.HttpResponse response = builder.url("https://api.example.com").get().execute();

            assertThat(response.getStatusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("should build URL with query params")
        void shouldBuildUrlWithParams() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            given(restTemplate.exchange(contains("key=value"), any(), any(), eq(String.class)))
                    .willReturn(mockResponse);

            HttpRequestBuilder.HttpResponse response = builder
                    .url("https://api.example.com")
                    .param("key", "value")
                    .get()
                    .execute();

            assertThat(response.getStatusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("should skip null param values")
        void shouldSkipNullParamValues() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            given(restTemplate.exchange(eq("https://api.example.com"), any(), any(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.url("https://api.example.com").param("key", null).get().execute();

            // URL should not contain query params since value was null
        }

        @Test
        @DisplayName("should add multiple params via params map")
        void shouldAddMultipleParamsViaMap() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            given(restTemplate.exchange(contains("?"), any(), any(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.url("https://api.example.com")
                    .params(Map.of("a", "1", "b", "2"))
                    .get()
                    .execute();
        }

        @Test
        @DisplayName("should handle null params map")
        void shouldHandleNullParamsMap() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            given(restTemplate.exchange(eq("https://api.example.com"), any(), any(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.url("https://api.example.com").params(null).get().execute();
        }

        @Test
        @DisplayName("should skip null values in params map")
        void shouldSkipNullValuesInParamsMap() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            java.util.HashMap<String, String> params = new java.util.HashMap<>();
            params.put("key", null);
            given(restTemplate.exchange(eq("https://api.example.com"), any(), any(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.url("https://api.example.com").params(params).get().execute();
        }
    }

    @Nested
    @DisplayName("HTTP methods")
    class HttpMethods {

        @BeforeEach
        void setUrl() {
            builder.url("https://api.example.com");
        }

        @Test
        @DisplayName("should default to GET")
        void shouldDefaultToGet() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.execute();
        }

        @Test
        @DisplayName("should set POST method")
        void shouldSetPostMethod() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.post().execute();
        }

        @Test
        @DisplayName("should set PUT method")
        void shouldSetPutMethod() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            given(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.put().execute();
        }

        @Test
        @DisplayName("should set DELETE method")
        void shouldSetDeleteMethod() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            given(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.delete().execute();
        }

        @Test
        @DisplayName("should set custom HTTP method")
        void shouldSetCustomMethod() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            given(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.method(HttpMethod.PATCH).execute();
        }
    }

    @Nested
    @DisplayName("Headers")
    class Headers {

        @BeforeEach
        void setUrl() {
            builder.url("https://api.example.com");
        }

        @Test
        @DisplayName("should set single header")
        void shouldSetSingleHeader() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            ArgumentCaptor<HttpEntity<Object>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
            given(restTemplate.exchange(anyString(), any(), entityCaptor.capture(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.header("X-Custom", "custom-value").get().execute();

            assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Custom"))
                    .isEqualTo("custom-value");
        }

        @Test
        @DisplayName("should set multiple headers via map")
        void shouldSetMultipleHeaders() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            ArgumentCaptor<HttpEntity<Object>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
            given(restTemplate.exchange(anyString(), any(), entityCaptor.capture(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.headers(Map.of("X-A", "a", "X-B", "b")).get().execute();

            HttpHeaders captured = entityCaptor.getValue().getHeaders();
            assertThat(captured.getFirst("X-A")).isEqualTo("a");
            assertThat(captured.getFirst("X-B")).isEqualTo("b");
        }

        @Test
        @DisplayName("should handle null headers map")
        void shouldHandleNullHeadersMap() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            given(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.headers(null).get().execute();
        }

        @Test
        @DisplayName("should set content type")
        void shouldSetContentType() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            ArgumentCaptor<HttpEntity<Object>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
            given(restTemplate.exchange(anyString(), any(), entityCaptor.capture(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.contentType("application/xml").get().execute();

            assertThat(entityCaptor.getValue().getHeaders().getContentType())
                    .isEqualTo(MediaType.APPLICATION_XML);
        }

        @Test
        @DisplayName("should set authorization header")
        void shouldSetAuthorizationHeader() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            ArgumentCaptor<HttpEntity<Object>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
            given(restTemplate.exchange(anyString(), any(), entityCaptor.capture(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.authorization("Bearer jwt-token").get().execute();

            assertThat(entityCaptor.getValue().getHeaders().getFirst("Authorization"))
                    .isEqualTo("Bearer jwt-token");
        }
    }

    @Nested
    @DisplayName("Body and execution")
    class BodyAndExecution {

        @BeforeEach
        void setUrl() {
            builder.url("https://api.example.com");
        }

        @Test
        @DisplayName("should set default Content-Type to JSON when body is present")
        void shouldSetDefaultContentTypeWithBody() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            ArgumentCaptor<HttpEntity<Object>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
            given(restTemplate.exchange(anyString(), any(), entityCaptor.capture(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.body("{\"key\":\"value\"}").post().execute();

            assertThat(entityCaptor.getValue().getHeaders().getContentType())
                    .isEqualTo(MediaType.APPLICATION_JSON);
        }

        @Test
        @DisplayName("should not override explicit Content-Type when body is present")
        void shouldNotOverrideExplicitContentType() {
            ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
            ArgumentCaptor<HttpEntity<Object>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
            given(restTemplate.exchange(anyString(), any(), entityCaptor.capture(), eq(String.class)))
                    .willReturn(mockResponse);

            builder.contentType("text/plain").body("plain text").post().execute();

            assertThat(entityCaptor.getValue().getHeaders().getContentType())
                    .isEqualTo(MediaType.TEXT_PLAIN);
        }

        @Test
        @DisplayName("should return response with status, body, and headers")
        void shouldReturnResponse() {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("X-Response", "value");
            ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"result\":true}", responseHeaders, HttpStatus.OK);
            given(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .willReturn(mockResponse);

            HttpRequestBuilder.HttpResponse response = builder.get().execute();

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getBody()).isEqualTo("{\"result\":true}");
            assertThat(response.getHeaders().getFirst("X-Response")).isEqualTo("value");
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("should propagate RestClientException")
        void shouldPropagateRestClientException() {
            given(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .willThrow(new RestClientException("Connection refused"));

            assertThatThrownBy(() -> builder.get().execute())
                    .isInstanceOf(RestClientException.class)
                    .hasMessageContaining("Connection refused");
        }
    }

    @Nested
    @DisplayName("HttpResponse")
    class HttpResponseTest {

        @Test
        @DisplayName("isSuccess should return true for 2xx status codes")
        void isSuccessShouldReturnTrueFor2xx() {
            assertThat(new HttpRequestBuilder.HttpResponse(200, "OK", null).isSuccess()).isTrue();
            assertThat(new HttpRequestBuilder.HttpResponse(201, "Created", null).isSuccess()).isTrue();
            assertThat(new HttpRequestBuilder.HttpResponse(204, "", null).isSuccess()).isTrue();
            assertThat(new HttpRequestBuilder.HttpResponse(299, "", null).isSuccess()).isTrue();
        }

        @Test
        @DisplayName("isSuccess should return false for non-2xx status codes")
        void isSuccessShouldReturnFalseForNon2xx() {
            assertThat(new HttpRequestBuilder.HttpResponse(400, "Bad Request", null).isSuccess()).isFalse();
            assertThat(new HttpRequestBuilder.HttpResponse(404, "Not Found", null).isSuccess()).isFalse();
            assertThat(new HttpRequestBuilder.HttpResponse(500, "Error", null).isSuccess()).isFalse();
            assertThat(new HttpRequestBuilder.HttpResponse(199, "", null).isSuccess()).isFalse();
        }

        @Test
        @DisplayName("should handle null headers in constructor")
        void shouldHandleNullHeaders() {
            HttpRequestBuilder.HttpResponse response = new HttpRequestBuilder.HttpResponse(200, "OK", null);
            assertThat(response.getHeaders()).isNotNull();
        }

        @Test
        @DisplayName("toString should truncate long body")
        void toStringShouldTruncateLongBody() {
            String longBody = "a".repeat(200);
            HttpRequestBuilder.HttpResponse response = new HttpRequestBuilder.HttpResponse(200, longBody, null);
            String result = response.toString();

            assertThat(result).contains("...");
            assertThat(result).contains("statusCode=200");
        }

        @Test
        @DisplayName("toString should not truncate short body")
        void toStringShouldNotTruncateShortBody() {
            HttpRequestBuilder.HttpResponse response = new HttpRequestBuilder.HttpResponse(200, "short", null);
            String result = response.toString();

            assertThat(result).doesNotContain("...");
            assertThat(result).contains("short");
        }

        @Test
        @DisplayName("toString should handle null body")
        void toStringShouldHandleNullBody() {
            HttpRequestBuilder.HttpResponse response = new HttpRequestBuilder.HttpResponse(200, null, null);
            String result = response.toString();

            assertThat(result).contains("null");
        }
    }
}
