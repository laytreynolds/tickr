package com.tickr.tickr.dto;

import com.tickr.tickr.domain.reminder.Reminder;

public record NotificationMessage(
        String phoneNumber,
        String body
)

{
    public static NotificationMessage from(Reminder reminder) {
        return new NotificationMessage(
                reminder.getUser().getPhoneNumber(),
                "Reminder: " + reminder.getEvent().getTitle()
        );
    }
}