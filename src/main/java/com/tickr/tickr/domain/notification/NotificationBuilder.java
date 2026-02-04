package com.tickr.tickr.domain.notification;

import com.tickr.tickr.domain.reminder.Reminder;

/**
 * Builds a {@link Notification} from a {@link Reminder}.
 * Implementations are channel-specific (SMS, email, etc.).
 */
public interface NotificationBuilder {

    /**
     * Whether this builder can build a notification for the given reminder's channel.
     */
    boolean supports(Reminder reminder);

    /**
     * Build a notification from the reminder (e.g. recipient and message body from event/user).
     */
    Notification build(Reminder reminder);
}
