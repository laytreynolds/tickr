package com.tickr.tickr.api;

import com.tickr.tickr.domain.reminder.ReminderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import com.tickr.tickr.domain.reminder.Reminder;

@RestController
@RequestMapping("/tickr/api/v1/reminder")
@RequiredArgsConstructor
public class ReminderController {
    private final ReminderService reminderService;

    @GetMapping("/reminders")
    public ResponseEntity<List<Reminder>> getReminders() {
        List<Reminder> reminders = reminderService.getReminders();
        return ResponseEntity.ok(reminders);
    }

    @DeleteMapping("/reminders/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable UUID id) {
        reminderService.deleteReminder(id);
        return ResponseEntity.noContent().build();
    }
}
