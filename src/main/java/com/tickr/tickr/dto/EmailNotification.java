package com.tickr.tickr.dto;

import com.tickr.tickr.domain.notification.Notification;
import com.tickr.tickr.domain.reminder.Reminder;

/**
 * Email notification payload. Implements {@link Notification} for the EMAIL channel.
 */
public class EmailNotification implements Notification {

    private final String to;
    private final String subject;
    private final String body;

    public EmailNotification(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    @Override
    public Reminder.Channel getChannel() {
        return Reminder.Channel.EMAIL;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}
