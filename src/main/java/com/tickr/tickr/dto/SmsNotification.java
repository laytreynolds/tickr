package com.tickr.tickr.dto;

import com.tickr.tickr.domain.notification.Notification;
import com.tickr.tickr.domain.reminder.Reminder;

public class SmsNotification implements Notification {

    private final String to;
    private final String body;

    public SmsNotification(String to, String body) {
        this.to = to;
        this.body = body;
    }

    @Override
    public Reminder.Channel getChannel() {
        return Reminder.Channel.SMS;
    }

    public String getTo() {
        return to;
    }

    public String getBody() {
        return body;
    }
}
