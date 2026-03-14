package com.tickr.tickr.dto;

import com.tickr.tickr.domain.reminder.Reminder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SmsNotification")
class SmsNotificationTest {

    @Test
    @DisplayName("should create with correct fields and SMS channel")
    void shouldCreateWithCorrectFieldsAndChannel() {
        SmsNotification notification = new SmsNotification("+15551234567", "Reminder: Meeting");

        assertThat(notification.getTo()).isEqualTo("+15551234567");
        assertThat(notification.getBody()).isEqualTo("Reminder: Meeting");
        assertThat(notification.getChannel()).isEqualTo(Reminder.Channel.SMS);
    }
}
