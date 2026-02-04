package com.tickr.tickr.scheduler;

import com.tickr.tickr.domain.reminder.ReminderService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final ReminderService reminderService;

    @Value("${scheduler.reminder.poll-rate-ms}")
    private long pollRateMs;

    @PostConstruct
    public void init() {
        log.info("ReminderScheduler initialized with poll rate: {} ms ({} seconds)",
                pollRateMs, pollRateMs / 1000);
    }

    @Scheduled(fixedDelayString = "${scheduler.reminder.poll-rate-ms}")
    public void run() {
        try {
            reminderService.sendDueReminders();
        } catch (Exception e) {
            log.error("Error while sending due reminders", e);
        }
    }
}
