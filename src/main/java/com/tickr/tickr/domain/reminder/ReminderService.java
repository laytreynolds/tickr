package com.tickr.tickr.domain.reminder;

import com.tickr.tickr.dto.NotificationMessage;
import com.tickr.tickr.notification.NotificationSender;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final @Qualifier("smsNotificationSender") NotificationSender notificationSender;

    @Transactional
    public void sendDueReminders() {
        List<Reminder> reminders =
                reminderRepository.findDueReminders(Instant.now());

        for (Reminder reminder : reminders) {
            try {
                notificationSender.send(
                        NotificationMessage.from(reminder)
                );
                reminder.markSent();
            } catch (Exception e) {
                reminder.setStatus(Reminder.Status.FAILED);
            }
        }
    }
}

