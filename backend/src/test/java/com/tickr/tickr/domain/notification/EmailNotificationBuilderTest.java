package com.tickr.tickr.domain.notification;

import com.tickr.tickr.domain.event.Event;
import com.tickr.tickr.domain.reminder.Reminder;
import com.tickr.tickr.domain.user.User;
import com.tickr.tickr.dto.EmailNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EmailNotificationBuilder")
class EmailNotificationBuilderTest {

    private EmailNotificationBuilder builder;
    private User user;
    private Event event;

    @BeforeEach
    void setUp() {
        builder = new EmailNotificationBuilder();

        user = User.builder()
                .id(UUID.randomUUID())
                .phoneNumber("+15551234567")
                .timezone("America/New_York")
                .passwordHash("hashed")
                .build();

        event = Event.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .title("Team Meeting")
                .timezone("America/New_York")
                .startTime(Instant.now().plus(Duration.ofDays(1)))
                .source(0)
                .build();
    }

    @Nested
    @DisplayName("supports")
    class Supports {

        @Test
        @DisplayName("should return true for EMAIL channel")
        void shouldSupportEmailChannel() {
            Reminder reminder = buildReminder(Reminder.Channel.EMAIL);

            assertThat(builder.supports(reminder)).isTrue();
        }

        @Test
        @DisplayName("should return false for SMS channel")
        void shouldNotSupportSmsChannel() {
            Reminder reminder = buildReminder(Reminder.Channel.SMS);

            assertThat(builder.supports(reminder)).isFalse();
        }

        @Test
        @DisplayName("should return false for PHONE channel")
        void shouldNotSupportPhoneChannel() {
            Reminder reminder = buildReminder(Reminder.Channel.PHONE);

            assertThat(builder.supports(reminder)).isFalse();
        }

        @Test
        @DisplayName("should return false for null reminder")
        void shouldReturnFalseForNull() {
            assertThat(builder.supports(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("build")
    class Build {

        @Test
        @DisplayName("should build email notification with event title in subject and body")
        void shouldBuildEmailNotification() {
            Reminder reminder = buildReminder(Reminder.Channel.EMAIL);

            Notification notification = builder.build(reminder);

            assertThat(notification).isInstanceOf(EmailNotification.class);
            EmailNotification email = (EmailNotification) notification;
            assertThat(email.getChannel()).isEqualTo(Reminder.Channel.EMAIL);
            assertThat(email.getSubject()).contains("Team Meeting");
            assertThat(email.getBody()).contains("Team Meeting");
        }

        @Test
        @DisplayName("should throw for unsupported channel")
        void shouldThrowForUnsupportedChannel() {
            Reminder reminder = buildReminder(Reminder.Channel.SMS);

            assertThatThrownBy(() -> builder.build(reminder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not support");
        }
    }

    private Reminder buildReminder(Reminder.Channel channel) {
        return Reminder.builder()
                .id(UUID.randomUUID())
                .event(event)
                .user(user)
                .remindAt(Instant.now())
                .status(Reminder.Status.PENDING)
                .channel(channel)
                .build();
    }
}
