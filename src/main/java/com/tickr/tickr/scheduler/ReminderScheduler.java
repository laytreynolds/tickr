package com.tickr.tickr.scheduler;

import com.tickr.tickr.domain.reminder.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final ReminderService reminderService;

    @Scheduled(fixedDelayString = "${scheduler.reminder.poll-rate-ms}")
    public void run() {
        reminderService.sendDueReminders();
    }
}
