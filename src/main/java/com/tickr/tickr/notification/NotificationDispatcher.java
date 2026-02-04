package com.tickr.tickr.notification;

import com.tickr.tickr.domain.notification.Notification;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Dispatches a notification to the first registered sender that supports it.
 */
@Component
public class NotificationDispatcher {

    private final List<NotificationSender> senders;

    public NotificationDispatcher(List<NotificationSender> senders) {
        this.senders = senders;
    }

    public void send(Notification notification) {
        for (NotificationSender sender : senders) {
            if (sender.supports(notification)) {
                sender.send(notification);
                return;
            }
        }
        throw new IllegalStateException("No sender found for notification channel: " + notification.getChannel());
    }
}
