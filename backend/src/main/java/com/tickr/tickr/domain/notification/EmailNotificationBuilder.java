package com.tickr.tickr.domain.notification;

import com.tickr.tickr.domain.reminder.Reminder;
import com.tickr.tickr.dto.EmailNotification;

import org.springframework.stereotype.Component;

/**
 * Builds an email notification from a reminder. Stub implementation.
 */
@Component
public class EmailNotificationBuilder implements NotificationBuilder {

    @Override
    public boolean supports(Reminder.Channel channel) {
        return channel == Reminder.Channel.EMAIL;
    }

    @Override
    public Notification build(Reminder reminder, Reminder.Channel channel) {
        if (!supports(channel)) {
            throw new IllegalArgumentException("EmailNotificationBuilder does not support " + channel);
        }
        String to = "laytreynolds@hotmail.com";
        String subject = "Reminder: " + reminder.getEvent().getTitle();
        String body = "Reminder for event: " + reminder.getEvent().getTitle();
        return new EmailNotification(to, subject, body);
    }
}
