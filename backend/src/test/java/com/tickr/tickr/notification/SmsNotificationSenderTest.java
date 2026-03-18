package com.tickr.tickr.notification;

import com.tickr.tickr.domain.notification.Notification;
import com.tickr.tickr.domain.reminder.Reminder;
import com.tickr.tickr.dto.EmailNotification;
import com.tickr.tickr.dto.SmsNotification;
import com.tickr.tickr.http.HttpRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SmsNotificationSender")
class SmsNotificationSenderTest {

    @Mock
    private HttpRequestBuilder httpRequestBuilder;

    private SmsNotificationSender sender;

    @BeforeEach
    void setUp() {
        sender = new SmsNotificationSender(httpRequestBuilder, "https://example.test/sms", "testuser", "testpass");
    }

    @Nested
    @DisplayName("supports")
    class Supports {

        @Test
        @DisplayName("should return true for SMS channel")
        void shouldSupportSmsChannel() {
            SmsNotification notification = new SmsNotification("+15551234567", "Reminder");
            assertThat(sender.supports(notification)).isTrue();
        }

        @Test
        @DisplayName("should return false for EMAIL channel")
        void shouldNotSupportEmailChannel() {
            EmailNotification notification = new EmailNotification("a@b.com", "Sub", "Body");
            assertThat(sender.supports(notification)).isFalse();
        }

        @Test
        @DisplayName("should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(sender.supports(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for PHONE channel")
        void shouldReturnFalseForPhoneChannel() {
            Notification notification = mock(Notification.class);
            when(notification.getChannel()).thenReturn(Reminder.Channel.PHONE);
            assertThat(sender.supports(notification)).isFalse();
        }
    }

    @Nested
    @DisplayName("send")
    class Send {

        @Test
        @DisplayName("should send SMS via ClickSend API")
        void shouldSendSms() {
            SmsNotification notification = new SmsNotification("+15551234567", "Reminder: Meeting");

            HttpRequestBuilder.HttpResponse mockResponse =
                    new HttpRequestBuilder.HttpResponse(200, "{\"success\":true}", new HttpHeaders());

            given(httpRequestBuilder.url(anyString())).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.contentType(anyString())).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.authorization(anyString())).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.body(any())).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.post()).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.execute()).willReturn(mockResponse);

            sender.send(notification);
        }

        @Test
        @DisplayName("should throw when SMS API returns failure")
        void shouldThrowWhenApiReturnsFail() {
            SmsNotification notification = new SmsNotification("+15551234567", "Reminder");

            HttpRequestBuilder.HttpResponse mockResponse =
                    new HttpRequestBuilder.HttpResponse(500, "Internal Server Error", new HttpHeaders());

            given(httpRequestBuilder.url(anyString())).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.contentType(anyString())).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.authorization(anyString())).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.body(any())).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.post()).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.execute()).willReturn(mockResponse);

            assertThatThrownBy(() -> sender.send(notification))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to send SMS");
        }

        @Test
        @DisplayName("should throw for unsupported notification type")
        void shouldThrowForUnsupportedType() {
            EmailNotification emailNotification = new EmailNotification("a@b.com", "Sub", "Body");

            assertThatThrownBy(() -> sender.send(emailNotification))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not support");
        }

        @Test
        @DisplayName("should include Basic auth header")
        void shouldIncludeBasicAuthHeader() {
            SmsNotification notification = new SmsNotification("+15551234567", "Test");

            HttpRequestBuilder.HttpResponse mockResponse =
                    new HttpRequestBuilder.HttpResponse(200, "OK", new HttpHeaders());

            given(httpRequestBuilder.url(anyString())).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.contentType(anyString())).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.authorization(anyString())).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.body(any())).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.post()).willReturn(httpRequestBuilder);
            given(httpRequestBuilder.execute()).willReturn(mockResponse);

            sender.send(notification);

            // Verify Basic auth was used (base64 of "testuser:testpass")
            org.mockito.Mockito.verify(httpRequestBuilder)
                    .authorization(org.mockito.ArgumentMatchers.startsWith("Basic "));
        }
    }

    @Nested
    @DisplayName("buildBasicAuthHeader")
    class BuildBasicAuth {

        @Test
        @DisplayName("should throw when username is null")
        void shouldThrowWhenUsernameNull() {
            SmsNotificationSender nullUserSender = new SmsNotificationSender(
                    httpRequestBuilder,
                    "https://example.test/sms",
                    null,
                    "pass");
            SmsNotification notification = new SmsNotification("+1234", "Test");

            assertThatThrownBy(() -> nullUserSender.send(notification))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Username or Password not set");
        }

        @Test
        @DisplayName("should throw when password is null")
        void shouldThrowWhenPasswordNull() {
            SmsNotificationSender nullPassSender = new SmsNotificationSender(
                    httpRequestBuilder,
                    "https://example.test/sms",
                    "user",
                    null);
            SmsNotification notification = new SmsNotification("+1234", "Test");

            assertThatThrownBy(() -> nullPassSender.send(notification))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Username or Password not set");
        }
    }
}
