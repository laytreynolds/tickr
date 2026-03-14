package com.tickr.tickr.domain.reminder;

import com.tickr.tickr.domain.event.Event;
import com.tickr.tickr.domain.event.EventUser;
import com.tickr.tickr.domain.notification.Notification;
import com.tickr.tickr.domain.notification.NotificationService;
import com.tickr.tickr.domain.user.User;
import com.tickr.tickr.notification.NotificationDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReminderService")
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private ReminderService reminderService;

    private User owner;
    private User assignedUser;
    private Event event;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(UUID.randomUUID())
                .phoneNumber("+15551234567")
                .timezone("America/New_York")
                .passwordHash("hashed")
                .build();

        assignedUser = User.builder()
                .id(UUID.randomUUID())
                .phoneNumber("+15559876543")
                .timezone("America/Chicago")
                .passwordHash("hashed")
                .build();

        event = Event.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .title("Team Meeting")
                .description("Weekly sync")
                .timezone("America/New_York")
                .startTime(Instant.now().plus(Duration.ofDays(10)))
                .endTime(Instant.now().plus(Duration.ofDays(10)).plusSeconds(3600))
                .source(0)
                .build();
    }

    @Nested
    @DisplayName("getReminders")
    class GetReminders {

        @Test
        @DisplayName("should return all reminders")
        void shouldReturnAllReminders() {
            Reminder reminder = buildReminder(Reminder.Status.PENDING);
            given(reminderRepository.findAll()).willReturn(List.of(reminder));

            List<Reminder> result = reminderService.getReminders();

            assertThat(result).hasSize(1);
            then(reminderRepository).should().findAll();
        }

        @Test
        @DisplayName("should return empty list when no reminders exist")
        void shouldReturnEmptyList() {
            given(reminderRepository.findAll()).willReturn(Collections.emptyList());

            List<Reminder> result = reminderService.getReminders();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteReminder")
    class DeleteReminder {

        @Test
        @DisplayName("should delete reminder by id")
        void shouldDeleteReminderById() {
            UUID id = UUID.randomUUID();

            reminderService.deleteReminder(id);

            then(reminderRepository).should().deleteById(id);
        }
    }

    @Nested
    @DisplayName("sendDueReminders")
    class SendDueReminders {

        @Test
        @DisplayName("should send notifications for due reminders")
        void shouldSendNotificationsForDueReminders() {
            Reminder reminder = buildReminder(Reminder.Status.PENDING);
            Notification mockNotification = mock(Notification.class);

            given(reminderRepository.findDueReminders(any(Instant.class)))
                    .willReturn(List.of(reminder));
            given(notificationService.create(reminder)).willReturn(mockNotification);

            reminderService.sendDueReminders();

            then(notificationService).should().create(reminder);
            then(notificationDispatcher).should().send(mockNotification);
            assertThat(reminder.getStatus()).isEqualTo(Reminder.Status.SENT);
        }

        @Test
        @DisplayName("should handle empty due reminders list")
        void shouldHandleEmptyDueReminders() {
            given(reminderRepository.findDueReminders(any(Instant.class)))
                    .willReturn(Collections.emptyList());

            reminderService.sendDueReminders();

            then(notificationService).should(never()).create(any());
            then(notificationDispatcher).should(never()).send(any());
        }

        @Test
        @DisplayName("should mark reminder as failed when notification fails")
        void shouldMarkReminderAsFailedWhenNotificationFails() {
            Reminder reminder = buildReminder(Reminder.Status.PENDING);

            given(reminderRepository.findDueReminders(any(Instant.class)))
                    .willReturn(List.of(reminder));
            given(notificationService.create(reminder))
                    .willThrow(new RuntimeException("Notification failed"));

            reminderService.sendDueReminders();

            assertThat(reminder.getStatus()).isEqualTo(Reminder.Status.FAILED);
        }

        @Test
        @DisplayName("should process mixed success and failure reminders")
        void shouldProcessMixedSuccessAndFailure() {
            Reminder successReminder = buildReminder(Reminder.Status.PENDING);
            Reminder failReminder = buildReminder(Reminder.Status.PENDING);
            Notification mockNotification = mock(Notification.class);

            given(reminderRepository.findDueReminders(any(Instant.class)))
                    .willReturn(List.of(successReminder, failReminder));
            given(notificationService.create(successReminder)).willReturn(mockNotification);
            given(notificationService.create(failReminder))
                    .willThrow(new RuntimeException("Failed"));

            reminderService.sendDueReminders();

            assertThat(successReminder.getStatus()).isEqualTo(Reminder.Status.SENT);
            assertThat(failReminder.getStatus()).isEqualTo(Reminder.Status.FAILED);
        }
    }

    @Nested
    @DisplayName("createRemindersForEvent")
    class CreateRemindersForEvent {

        @Test
        @DisplayName("should skip reminder creation for past events")
        void shouldSkipPastEvents() {
            event.setStartTime(Instant.now().minus(Duration.ofDays(1)));

            reminderService.createRemindersForEvent(event);

            then(reminderRepository).should(never()).saveAll(anyList());
        }

        @Test
        @DisplayName("should create reminders for future event with owner only")
        void shouldCreateRemindersForFutureEventWithOwnerOnly() {
            event.setAssignedUsers(new HashSet<>());

            reminderService.createRemindersForEvent(event);

            then(reminderRepository).should().saveAll(anyList());
        }

        @Test
        @DisplayName("should create reminders for event with owner and assigned users")
        void shouldCreateRemindersForEventWithAssignedUsers() {
            EventUser eventUser = EventUser.builder()
                    .id(new EventUser.EventUserId(event.getId(), assignedUser.getId()))
                    .event(event)
                    .user(assignedUser)
                    .build();
            event.setAssignedUsers(Set.of(eventUser));

            reminderService.createRemindersForEvent(event);

            then(reminderRepository).should().saveAll(anyList());
        }

        @Test
        @DisplayName("should create correct reminder timestamps for event 10 days away")
        void shouldCreateCorrectTimestampsForFarFutureEvent() {
            // Event 10 days away: should get 7-day, 3-day, 2-day, 1-day, 4-hour, 1-min reminders
            event.setStartTime(Instant.now().plus(Duration.ofDays(10)));
            event.setAssignedUsers(new HashSet<>());

            reminderService.createRemindersForEvent(event);

            then(reminderRepository).should().saveAll(argThat(reminders -> {
                List<Reminder> list = new ArrayList<>();
                reminders.forEach(list::add);
                // 1 user (owner) * 6 reminder timestamps = 6 reminders
                return list.size() == 6;
            }));
        }

        @Test
        @DisplayName("should create fewer reminders for event happening soon")
        void shouldCreateFewerRemindersForSoonEvent() {
            // Event 2 hours from now: only 1-minute reminder
            event.setStartTime(Instant.now().plus(Duration.ofHours(2)));
            event.setAssignedUsers(new HashSet<>());

            reminderService.createRemindersForEvent(event);

            then(reminderRepository).should().saveAll(argThat(reminders -> {
                List<Reminder> list = new ArrayList<>();
                reminders.forEach(list::add);
                // 1 user (owner) * 1 reminder timestamp (1-min before) = 1 reminder
                return list.size() == 1;
            }));
        }

        @Test
        @DisplayName("should set all reminders to PENDING status with EMAIL channel")
        void shouldSetCorrectStatusAndChannel() {
            event.setAssignedUsers(new HashSet<>());

            reminderService.createRemindersForEvent(event);

            then(reminderRepository).should().saveAll(argThat(reminders -> {
                for (Reminder r : reminders) {
                    if (r.getStatus() != Reminder.Status.PENDING) return false;
                    if (r.getChannel() != Reminder.Channel.EMAIL) return false;
                }
                return true;
            }));
        }

        @Test
        @DisplayName("should wrap and rethrow exceptions during reminder creation")
        void shouldWrapAndRethrowExceptions() {
            event.setAssignedUsers(new HashSet<>());
            doThrow(new RuntimeException("DB error"))
                    .when(reminderRepository).saveAll(anyList());

            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> reminderService.createRemindersForEvent(event));
        }

        @Test
        @DisplayName("should create 5 reminders for event 5 days away")
        void shouldCreate5RemindersFor5DaysAway() {
            // 5 days away: 3-day, 2-day, 1-day, 4-hour, 1-min = 5
            event.setStartTime(Instant.now().plus(Duration.ofDays(5)));
            event.setAssignedUsers(new HashSet<>());

            reminderService.createRemindersForEvent(event);

            then(reminderRepository).should().saveAll(argThat(reminders -> {
                List<Reminder> list = new ArrayList<>();
                reminders.forEach(list::add);
                return list.size() == 5;
            }));
        }

        @Test
        @DisplayName("should create 2 reminders for event 5 hours away")
        void shouldCreate2RemindersFor5HoursAway() {
            // 5 hours away: 4-hour, 1-min = 2
            event.setStartTime(Instant.now().plus(Duration.ofHours(5)));
            event.setAssignedUsers(new HashSet<>());

            reminderService.createRemindersForEvent(event);

            then(reminderRepository).should().saveAll(argThat(reminders -> {
                List<Reminder> list = new ArrayList<>();
                reminders.forEach(list::add);
                return list.size() == 2;
            }));
        }

        @Test
        @DisplayName("should create reminders for multiple users")
        void shouldCreateRemindersForMultipleUsers() {
            // 2 hours away with 2 users (owner + assigned): 1 timestamp * 2 users = 2
            event.setStartTime(Instant.now().plus(Duration.ofHours(2)));
            EventUser eventUser = EventUser.builder()
                    .id(new EventUser.EventUserId(event.getId(), assignedUser.getId()))
                    .event(event)
                    .user(assignedUser)
                    .build();
            event.setAssignedUsers(Set.of(eventUser));

            reminderService.createRemindersForEvent(event);

            then(reminderRepository).should().saveAll(argThat(reminders -> {
                List<Reminder> list = new ArrayList<>();
                reminders.forEach(list::add);
                return list.size() == 2;
            }));
        }

        @Test
        @DisplayName("should handle event with null assigned users set")
        void shouldHandleNullAssignedUsers() {
            event.setStartTime(Instant.now().plus(Duration.ofHours(2)));
            event.setAssignedUsers(null);

            reminderService.createRemindersForEvent(event);

            then(reminderRepository).should().saveAll(anyList());
        }

        @Test
        @DisplayName("should create reminders for event about 3 days away")
        void shouldCreateRemindersFor3DaysAway() {
            // 3.5 days away: 3-day, 2-day, 1-day, 4-hour, 1-min = 5
            event.setStartTime(Instant.now().plus(Duration.ofDays(3)).plus(Duration.ofHours(12)));
            event.setAssignedUsers(new HashSet<>());

            reminderService.createRemindersForEvent(event);

            then(reminderRepository).should().saveAll(argThat(reminders -> {
                List<Reminder> list = new ArrayList<>();
                reminders.forEach(list::add);
                return list.size() == 5;
            }));
        }

        @Test
        @DisplayName("should create 2 reminders for event about 1 day away")
        void shouldCreate2RemindersFor1DayAway() {
            // 1 day away: daysUntilEvent=1, hoursUntilEvent=24, 4-hour + 1-min = 2
            event.setStartTime(Instant.now().plus(Duration.ofDays(1)));
            event.setAssignedUsers(new HashSet<>());

            reminderService.createRemindersForEvent(event);

            then(reminderRepository).should().saveAll(argThat(reminders -> {
                List<Reminder> list = new ArrayList<>();
                reminders.forEach(list::add);
                return list.size() == 2;
            }));
        }

        @Test
        @DisplayName("should create 1 reminder for event 30 minutes away")
        void shouldCreate1ReminderFor30MinAway() {
            // 30 min away: only 1-min reminder
            event.setStartTime(Instant.now().plus(Duration.ofMinutes(30)));
            event.setAssignedUsers(new HashSet<>());

            reminderService.createRemindersForEvent(event);

            then(reminderRepository).should().saveAll(argThat(reminders -> {
                List<Reminder> list = new ArrayList<>();
                reminders.forEach(list::add);
                return list.size() == 1;
            }));
        }
    }

    private Reminder buildReminder(Reminder.Status status) {
        return Reminder.builder()
                .id(UUID.randomUUID())
                .event(event)
                .user(owner)
                .remindAt(Instant.now().minus(Duration.ofMinutes(1)))
                .status(status)
                .channel(Reminder.Channel.EMAIL)
                .build();
    }
}
