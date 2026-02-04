package com.tickr.tickr.notification;

import com.tickr.tickr.dto.NotificationMessage;
import org.springframework.stereotype.Component;

@Component
public class SmsNotificationSender implements NotificationSender {

    @Override
    public void send(NotificationMessage message) {
        // TODO: integrate Twilio / AWS SNS


        System.out.println("Sending SMS to " +
                message.phoneNumber() +
                ": " + message.body());
    }
}
