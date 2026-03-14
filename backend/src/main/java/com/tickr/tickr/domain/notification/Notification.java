package com.tickr.tickr.domain.notification;

import com.tickr.tickr.domain.reminder.Reminder;

/**
 * Represents a notification that can be sent via one or more channels.
 * Implementations define the payload and channel (SMS, EMAIL, etc.).
 */
public interface Notification {

    /**
     * The channel this notification is intended for (determines which sender handles it).
     */
    Reminder.Channel getChannel();
}

