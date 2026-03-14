package com.tickr.tickr.domain.reminder;

import com.tickr.tickr.domain.event.Event;
import com.tickr.tickr.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Reminder")
class ReminderTest {

    @Test
    @DisplayName("markSent should change status from PENDING to SENT")
    void markSentShouldChangeStatus() {
        Reminder reminder = Reminder.builder()
                .id(UUID.randomUUID())
                .event(Event.builder().id(UUID.randomUUID()).title("Test").timezone("UTC")
                        .startTime(Instant.now()).source(0)
                        .owner(User.builder().id(UUID.randomUUID()).phoneNumber("+1234")
                                .timezone("UTC").passwordHash("hash").build())
                        .build())
                .user(User.builder().id(UUID.randomUUID()).phoneNumber("+1234")
                        .timezone("UTC").passwordHash("hash").build())
                .remindAt(Instant.now())
                .status(Reminder.Status.PENDING)
                .channel(Reminder.Channel.EMAIL)
                .build();

        assertThat(reminder.getStatus()).isEqualTo(Reminder.Status.PENDING);

        reminder.markSent();

        assertThat(reminder.getStatus()).isEqualTo(Reminder.Status.SENT);
    }

    @Test
    @DisplayName("should have all Status enum values")
    void shouldHaveAllStatusValues() {
        assertThat(Reminder.Status.values())
                .containsExactly(Reminder.Status.PENDING, Reminder.Status.SENT, Reminder.Status.FAILED);
    }

    @Test
    @DisplayName("should have all Channel enum values")
    void shouldHaveAllChannelValues() {
        assertThat(Reminder.Channel.values())
                .containsExactly(Reminder.Channel.SMS, Reminder.Channel.PHONE, Reminder.Channel.EMAIL);
    }

    @Test
    @DisplayName("builder should set default createdAt")
    void builderShouldSetDefaultCreatedAt() {
        Reminder reminder = Reminder.builder()
                .status(Reminder.Status.PENDING)
                .channel(Reminder.Channel.EMAIL)
                .remindAt(Instant.now())
                .build();

        assertThat(reminder.getCreatedAt()).isNotNull();
    }
}
