package com.tickr.tickr.domain.reminder;

import com.tickr.tickr.domain.event.Event;
import com.tickr.tickr.domain.notification.Notification;
import com.tickr.tickr.domain.notification.NotificationService;
import com.tickr.tickr.domain.user.User;
import com.tickr.tickr.notification.NotificationDispatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final NotificationService notificationService;
    private final NotificationDispatcher notificationDispatcher;

    public List<Reminder> getReminders() {
        return reminderRepository.findAll();
    }

    public void deleteReminder(UUID id) {
        reminderRepository.deleteById(id);
    }

    @Transactional
    public void sendDueReminders() {
        List<Reminder> reminders = reminderRepository.findDueReminders(Instant.now());        
        int successCount = 0;
        int failureCount = 0;
        for (Reminder reminder : reminders) {
            try {
                Notification notification = notificationService.create(reminder);
                notificationDispatcher.send(notification);
                reminder.markSent();
                successCount++;
                log.debug("Reminder send successful for id: {}", reminder.getId());
            } catch (Exception e) {
                reminder.setStatus(Reminder.Status.FAILED);
                failureCount++;
                log.warn("Reminder send failed for id: {} - {}", reminder.getId(), e.getMessage());
            }
        }

        log.info("Processed {} reminder(s): {} successful, {} failed",
                reminders.size(), successCount, failureCount);
    }

    @Transactional
    public void createRemindersForEvent(Event event) {
        try {
            Instant now = Instant.now();
            Instant eventStartTime = event.getStartTime();

            // Don't create reminders for past events
            if (eventStartTime.isBefore(now)) {
                log.warn("Skipping reminder creation for past event: {}", event.getId());
                return;
            }

            // Calculate reminder timestamps
            List<Instant> reminderTimestamps = calculateReminderTimestamps(eventStartTime, now);

            // If no valid reminders, return early
            if (reminderTimestamps.isEmpty()) {
                log.warn("No valid reminder timestamps calculated for event: {}", event.getId());
                return;
            }

            // Get all users who should receive reminders (owner + assigned users)
            Set<User> usersToNotify = new HashSet<>();
            usersToNotify.add(event.getOwner());
            if (event.getAssignedUsers() != null) {
                usersToNotify.addAll(event.getAssignedUsers());
            }

            // Create reminders for each user and each timestamp
            List<Reminder> reminders = new ArrayList<>();
            for (User user : usersToNotify) {
                for (Instant remindAt : reminderTimestamps) {
                    Reminder reminder = Reminder.builder()
                            .event(event)
                            .user(user)
                            .remindAt(remindAt)
                            .status(Reminder.Status.PENDING)
                            .channel(Reminder.Channel.EMAIL)
                            .build();
                    reminders.add(reminder);
                }
            }

            // Batch save all reminders
            if (!reminders.isEmpty()) {
                reminderRepository.saveAll(reminders);
                log.info("Successfully created {} reminder(s) for event {} ({} users, {} timestamps)",
                        reminders.size(), event.getTitle(), usersToNotify.size(), reminderTimestamps.size());
            }
        } catch (Exception e) {
            log.error("Error creating reminders for event {}: {}", event.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create reminders for event: " + event.getId(), e);
        }
    }

    private List<Instant> calculateReminderTimestamps(Instant eventStartTime, Instant now) {
        List<Instant> timestamps = new ArrayList<>();
        Duration timeUntilEvent = Duration.between(now, eventStartTime);
        long daysUntilEvent = timeUntilEvent.toDays();
        long hoursUntilEvent = timeUntilEvent.toHours();
        long minutesUntilEvent = timeUntilEvent.toMinutes();

        // 7 days before reminder
        if (daysUntilEvent >= 7) {
            timestamps.add(eventStartTime.minus(Duration.ofDays(7)));
        }

        // 3 days before reminder
        if (daysUntilEvent >= 3) {
            timestamps.add(eventStartTime.minus(Duration.ofDays(3)));
        }

        // Daily reminders from 3 days before to 1 day before (excluding event day)
        if (daysUntilEvent >= 2) {
            // Start from 2 days before (since 3 days is already added above)
            for (long days = 2; days >= 1; days--) {
                timestamps.add(eventStartTime.minus(Duration.ofDays(days)));
            }
        }

        // Event day: 4 hours before
        if (hoursUntilEvent >= 4) {
            timestamps.add(eventStartTime.minus(Duration.ofHours(4)));
        }

        // TESTING: 1 minute before
        if (minutesUntilEvent >= 1) {
            timestamps.add(eventStartTime.minus(Duration.ofMinutes(1)));
        }

        // Filter out any timestamps that are in the past (shouldn't happen, but safety
        // check)
        timestamps.removeIf(timestamp -> timestamp.isBefore(now));

        return timestamps;
    }
}
