package com.tickr.tickr.notification;

import com.tickr.tickr.domain.notification.Notification;

/**
 * Sends a notification via a specific channel (SMS, email, etc.).
 * Implementations declare which notification type they support via
 * {@link #supports(Notification)}.
 */
public interface NotificationSender {

    /**
     * Whether this sender can handle the given notification (e.g. by channel).
     */
    boolean supports(Notification notification);

    /**
     * Send the notification. Call only when {@link #supports(Notification)} returns
     * true.
     */
    void send(Notification notification);
}
