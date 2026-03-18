package com.tickr.tickr.notification;

import com.tickr.tickr.domain.notification.Notification;
import com.tickr.tickr.domain.reminder.Reminder;
import com.tickr.tickr.dto.SmsNotification;
import com.tickr.tickr.dto.SmsRequest;
import com.tickr.tickr.http.HttpRequestBuilder;
import com.tickr.tickr.http.HttpRequestBuilder.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
public class SmsNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationSender.class);

    private final HttpRequestBuilder httpRequestBuilder;
    private final String url;
    private final String username;
    private final String password;

    public SmsNotificationSender(
            HttpRequestBuilder httpRequestBuilder,
            @Value("${sms.url:}") String url,
            @Value("${sms.username}") String username,
            @Value("${sms.password}") String password) {

        this.httpRequestBuilder = httpRequestBuilder;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean supports(Notification notification) {
        return notification != null && notification.getChannel() == Reminder.Channel.SMS;
    }

    @Override
    public void send(Notification notification) {
        if (!supports(notification)) {
            throw new IllegalArgumentException("SmsNotificationSender does not support " + notification.getChannel());
        }
        SmsNotification sms = (SmsNotification) notification;
        String auth = buildBasicAuthHeader(username, password);
        SmsRequest request = new SmsRequest(List.of(sms));

        HttpResponse response = sendSms(request, auth);
        if (!response.isSuccess()) {
            throw new RuntimeException("Failed to send SMS: " + response.getBody());
        }

    }

    private HttpResponse sendSms(SmsRequest request, String auth) {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("SMS_URL is not configured (missing sms.url)");
        }
        log.debug("Sending SMS request to {} with body: {}", url, request);
        return httpRequestBuilder
                .url(url)
                .contentType("application/json")
                .authorization(auth)
                .body(request)
                .post()
                .execute();
    }

    private String buildBasicAuthHeader(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalStateException("Username or Password not set for NotificationMessage Builder");
        }
        String originalInput = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(originalInput.getBytes());
        return "Basic " + encoded;
    }
}
