package com.tickr.tickr.domain.notification;

import com.tickr.tickr.domain.event.Event;
import com.tickr.tickr.domain.reminder.Reminder;
import com.tickr.tickr.domain.user.User;
import com.tickr.tickr.dto.SmsNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SmsNotificationBuilder")
class SmsNotificationBuilderTest {

    private SmsNotificationBuilder builder;
    private User user;
    private Event event;

    @BeforeEach
    void setUp() {
        builder = new SmsNotificationBuilder();

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
        @DisplayName("should return true for SMS channel")
        void shouldSupportSmsChannel() {
            assertThat(builder.supports(Reminder.Channel.SMS)).isTrue();
        }

        @Test
        @DisplayName("should return false for EMAIL channel")
        void shouldNotSupportEmailChannel() {
            assertThat(builder.supports(Reminder.Channel.EMAIL)).isFalse();
        }

        @Test
        @DisplayName("should return false for PHONE channel")
        void shouldNotSupportPhoneChannel() {
            assertThat(builder.supports(Reminder.Channel.PHONE)).isFalse();
        }

        @Test
        @DisplayName("should return false for null channel")
        void shouldReturnFalseForNull() {
            assertThat(builder.supports(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("build")
    class Build {

        @Test
        @DisplayName("should build SMS notification with user phone and event title")
        void shouldBuildSmsNotification() {
            Reminder reminder = buildReminder(Reminder.Channel.SMS);

            Notification notification = builder.build(reminder, Reminder.Channel.SMS);

            assertThat(notification).isInstanceOf(SmsNotification.class);
            SmsNotification sms = (SmsNotification) notification;
            assertThat(sms.getChannel()).isEqualTo(Reminder.Channel.SMS);
            assertThat(sms.getTo()).isEqualTo("+15551234567");
            assertThat(sms.getBody()).contains("Team Meeting");
        }

        @Test
        @DisplayName("should throw for unsupported channel")
        void shouldThrowForUnsupportedChannel() {
            Reminder reminder = buildReminder(Reminder.Channel.EMAIL);

            assertThatThrownBy(() -> builder.build(reminder, Reminder.Channel.EMAIL))
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
