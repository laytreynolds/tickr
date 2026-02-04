package com.tickr.tickr.domain.notification;

import com.tickr.tickr.domain.reminder.Reminder;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Builds a {@link Notification} from a {@link Reminder} by delegating to channel-specific builders.
 */
@Service
public class NotificationService {

    private final List<NotificationBuilder> builders;

    public NotificationService(List<NotificationBuilder> builders) {
        this.builders = builders;
    }

    /**
     * Create a notification for the given reminder (SMS, email, etc. based on reminder channel).
     */
    public Notification create(Reminder reminder) {
        for (NotificationBuilder builder : builders) {
            if (builder.supports(reminder)) {
                return builder.build(reminder);
            }
        }
        throw new IllegalStateException("No builder found for reminder channel: " + reminder.getChannel());
    }
}
