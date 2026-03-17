package com.tickr.tickr.domain.notification;

import com.tickr.tickr.domain.reminder.Reminder;
import com.tickr.tickr.dto.SmsNotification;

import org.springframework.stereotype.Component;

@Component
public class SmsNotificationBuilder implements NotificationBuilder {

    @Override
    public boolean supports(Reminder.Channel channel) {
        return channel == Reminder.Channel.SMS;
    }

    @Override
    public Notification build(Reminder reminder, Reminder.Channel channel) {
        if (!supports(channel)) {
            throw new IllegalArgumentException("SmsNotificationBuilder does not support " + channel);
        }
        String to = reminder.getUser().getPhoneNumber();
        String body = buildBody(reminder);
        return new SmsNotification(to, body);
    }

    private String buildBody(Reminder reminder) {
        String title = reminder.getEvent().getTitle();
        return "Reminder: " + title;
    }
}
