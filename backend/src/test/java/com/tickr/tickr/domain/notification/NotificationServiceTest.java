package com.tickr.tickr.domain.notification;

import com.tickr.tickr.domain.event.Event;
import com.tickr.tickr.domain.reminder.Reminder;
import com.tickr.tickr.domain.reminder.ReminderChannel;
import com.tickr.tickr.domain.user.User;
import com.tickr.tickr.dto.EmailNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService")
class NotificationServiceTest {

    private NotificationService notificationService;
    private Reminder reminder;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .phoneNumber("+15551234567")
                .timezone("America/New_York")
                .passwordHash("hashed")
                .build();

        Event event = Event.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .title("Team Meeting")
                .timezone("America/New_York")
                .startTime(Instant.now().plus(Duration.ofDays(1)))
                .source(0)
                .build();

        Reminder r = Reminder.builder()
                .id(UUID.randomUUID())
                .event(event)
                .user(user)
                .remindAt(Instant.now())
                .status(Reminder.Status.PENDING)
                .channel(Reminder.Channel.EMAIL)
                .build();
        r.getChannels().add(ReminderChannel.builder().reminder(r).channel(Reminder.Channel.EMAIL).build());
        reminder = r;
    }

    @Test
    @DisplayName("should delegate to the correct builder for the reminder channel")
    void shouldDelegateToCorrectBuilder() {
        NotificationBuilder emailBuilder = mock(NotificationBuilder.class);
        EmailNotification expectedNotification = new EmailNotification("test@test.com", "Subject", "Body");

        given(emailBuilder.supports(Reminder.Channel.EMAIL)).willReturn(true);
        given(emailBuilder.build(reminder, Reminder.Channel.EMAIL)).willReturn(expectedNotification);

        notificationService = new NotificationService(List.of(emailBuilder));

        List<Notification> result = notificationService.createAll(reminder);

        assertThat(result).hasSize(1).first().isEqualTo(expectedNotification);
        assertThat(result.get(0).getChannel()).isEqualTo(Reminder.Channel.EMAIL);
    }

    @Test
    @DisplayName("should throw when no builder supports the reminder channel")
    void shouldThrowWhenNoBuilderSupportsChannel() {
        NotificationBuilder emailBuilder = mock(NotificationBuilder.class);
        given(emailBuilder.supports(Reminder.Channel.EMAIL)).willReturn(false);

        notificationService = new NotificationService(List.of(emailBuilder));

        assertThatThrownBy(() -> notificationService.createAll(reminder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No builder found for reminder channel");
    }

    @Test
    @DisplayName("should throw when builder list is empty")
    void shouldThrowWhenNoBuilders() {
        notificationService = new NotificationService(Collections.emptyList());

        assertThatThrownBy(() -> notificationService.createAll(reminder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No builder found for reminder channel");
    }

    @Test
    @DisplayName("should use first matching builder when multiple builders exist")
    void shouldUseFirstMatchingBuilder() {
        NotificationBuilder firstBuilder = mock(NotificationBuilder.class);
        NotificationBuilder secondBuilder = mock(NotificationBuilder.class);
        EmailNotification firstNotification = new EmailNotification("first@test.com", "First", "Body");

        given(firstBuilder.supports(Reminder.Channel.EMAIL)).willReturn(true);
        given(firstBuilder.build(reminder, Reminder.Channel.EMAIL)).willReturn(firstNotification);

        notificationService = new NotificationService(List.of(firstBuilder, secondBuilder));

        List<Notification> result = notificationService.createAll(reminder);

        assertThat(result).hasSize(1).first().isEqualTo(firstNotification);
    }
}
