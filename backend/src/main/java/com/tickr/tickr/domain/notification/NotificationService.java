package com.tickr.tickr.domain.notification;

import com.tickr.tickr.domain.reminder.Reminder;
import com.tickr.tickr.domain.reminder.ReminderChannel;

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
     * Create notifications for all channels configured on the reminder.
     */
    public List<Notification> createAll(Reminder reminder) {
        List<Reminder.Channel> channels = reminder.getChannels().stream()
                .map(ReminderChannel::getChannel)
                .distinct()
                .toList();

        if (channels.isEmpty()) {
            throw new IllegalStateException("No channels configured for reminder: " + reminder.getId());
        }

        return channels.stream()
                .map(channel -> createForChannel(reminder, channel))
                .toList();
    }

    private Notification createForChannel(Reminder reminder, Reminder.Channel channel) {
        for (NotificationBuilder builder : builders) {
            if (builder.supports(channel)) {
                return builder.build(reminder, channel);
            }
        }
        throw new IllegalStateException("No builder found for reminder channel: " + channel);
    }
}
