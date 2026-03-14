package com.tickr.tickr.dto;

import com.tickr.tickr.domain.reminder.Reminder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailNotification")
class EmailNotificationTest {

    @Test
    @DisplayName("should create with correct fields and EMAIL channel")
    void shouldCreateWithCorrectFieldsAndChannel() {
        EmailNotification notification = new EmailNotification("test@example.com", "Subject Line", "Body text");

        assertThat(notification.getTo()).isEqualTo("test@example.com");
        assertThat(notification.getSubject()).isEqualTo("Subject Line");
        assertThat(notification.getBody()).isEqualTo("Body text");
        assertThat(notification.getChannel()).isEqualTo(Reminder.Channel.EMAIL);
    }
}
