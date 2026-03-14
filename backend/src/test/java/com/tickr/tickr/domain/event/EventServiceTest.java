package com.tickr.tickr.domain.event;

import com.tickr.tickr.domain.reminder.ReminderService;
import com.tickr.tickr.domain.user.User;
import com.tickr.tickr.domain.user.UserRepository;
import com.tickr.tickr.dto.CreateEventRequest;
import com.tickr.tickr.dto.EventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReminderService reminderService;

    @InjectMocks
    private EventService eventService;

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
                .startTime(Instant.now().plusSeconds(86400))
                .endTime(Instant.now().plusSeconds(90000))
                .source(0)
                .build();
    }

    @Nested
    @DisplayName("getEvents")
    class GetEvents {

        @Test
        @DisplayName("should return all events from repository")
        void shouldReturnAllEvents() {
            List<Event> events = List.of(event);
            given(eventRepository.findAll()).willReturn(events);

            List<Event> result = eventService.getEvents();

            assertThat(result).hasSize(1).containsExactly(event);
            then(eventRepository).should().findAll();
        }

        @Test
        @DisplayName("should return empty list when no events exist")
        void shouldReturnEmptyListWhenNoEvents() {
            given(eventRepository.findAll()).willReturn(Collections.emptyList());

            List<Event> result = eventService.getEvents();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("createEvent")
    class CreateEvent {

        private CreateEventRequest request;

        @BeforeEach
        void setUp() {
            request = new CreateEventRequest();
            request.setOwnerId(owner.getId());
            request.setTitle("Team Meeting");
            request.setDescription("Weekly sync");
            request.setStartTime(Instant.now().plusSeconds(86400));
            request.setEndTime(Instant.now().plusSeconds(90000));
            request.setSource("0");
            request.setTimezone("America/New_York");
        }

        @Test
        @DisplayName("should create event without assigned users")
        void shouldCreateEventWithoutAssignedUsers() {
            request.setAssignedUserIds(null);
            given(userRepository.findById(owner.getId())).willReturn(Optional.of(owner));
            given(eventRepository.save(any(Event.class))).willReturn(event);

            Event result = eventService.createEvent(request);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Team Meeting");
            then(eventRepository).should().save(any(Event.class));
            then(reminderService).should().createRemindersForEvent(event);
        }

        @Test
        @DisplayName("should create event with assigned users")
        void shouldCreateEventWithAssignedUsers() {
            request.setAssignedUserIds(List.of(assignedUser.getId()));
            given(userRepository.findById(owner.getId())).willReturn(Optional.of(owner));
            given(userRepository.findAllByIdIn(List.of(assignedUser.getId())))
                    .willReturn(List.of(assignedUser));
            given(eventRepository.save(any(Event.class))).willReturn(event);

            Event result = eventService.createEvent(request);

            assertThat(result).isNotNull();
            then(userRepository).should().findAllByIdIn(List.of(assignedUser.getId()));
            then(eventRepository).should().save(any(Event.class));
        }

        @Test
        @DisplayName("should throw when owner not found")
        void shouldThrowWhenOwnerNotFound() {
            given(userRepository.findById(owner.getId())).willReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.createEvent(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("owner not found");
        }

        @Test
        @DisplayName("should throw when assigned user not found")
        void shouldThrowWhenAssignedUserNotFound() {
            UUID missingUserId = UUID.randomUUID();
            request.setAssignedUserIds(List.of(missingUserId));
            given(userRepository.findById(owner.getId())).willReturn(Optional.of(owner));
            given(userRepository.findAllByIdIn(List.of(missingUserId)))
                    .willReturn(Collections.emptyList());

            assertThatThrownBy(() -> eventService.createEvent(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("One or more assigned users not found");
        }

        @Test
        @DisplayName("should create event with empty assigned users list")
        void shouldCreateEventWithEmptyAssignedUsersList() {
            request.setAssignedUserIds(Collections.emptyList());
            given(userRepository.findById(owner.getId())).willReturn(Optional.of(owner));
            given(eventRepository.save(any(Event.class))).willReturn(event);

            Event result = eventService.createEvent(request);

            assertThat(result).isNotNull();
            then(userRepository).should(never()).findAllByIdIn(any());
            then(eventRepository).should().save(any(Event.class));
        }

        @Test
        @DisplayName("should not fail event creation when reminder creation fails")
        void shouldNotFailWhenReminderCreationFails() {
            request.setAssignedUserIds(null);
            given(userRepository.findById(owner.getId())).willReturn(Optional.of(owner));
            given(eventRepository.save(any(Event.class))).willReturn(event);
            doThrow(new RuntimeException("Reminder creation failed"))
                    .when(reminderService).createRemindersForEvent(event);

            Event result = eventService.createEvent(request);

            assertThat(result).isNotNull();
            then(eventRepository).should().save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("deleteEvent")
    class DeleteEvent {

        @Test
        @DisplayName("should delete event by id")
        void shouldDeleteEventById() {
            UUID eventId = UUID.randomUUID();

            eventService.deleteEvent(eventId);

            then(eventRepository).should().deleteById(eventId);
        }
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("should map event to response with assigned users")
        void shouldMapEventToResponseWithAssignedUsers() {
            EventUser eventUser = EventUser.builder()
                    .id(new EventUser.EventUserId(event.getId(), assignedUser.getId()))
                    .event(event)
                    .user(assignedUser)
                    .build();
            event.setAssignedUsers(Set.of(eventUser));

            EventResponse response = eventService.toResponse(event);

            assertThat(response.id()).isEqualTo(event.getId());
            assertThat(response.ownerId()).isEqualTo(owner.getId());
            assertThat(response.title()).isEqualTo("Team Meeting");
            assertThat(response.description()).isEqualTo("Weekly sync");
            assertThat(response.assignedUserIds()).containsExactly(assignedUser.getId());
            assertThat(response.startTime()).isEqualTo(event.getStartTime());
            assertThat(response.endTime()).isEqualTo(event.getEndTime());
            assertThat(response.timezone()).isEqualTo("America/New_York");
            assertThat(response.source()).isEqualTo(0);
        }

        @Test
        @DisplayName("should map event to response without assigned users")
        void shouldMapEventToResponseWithoutAssignedUsers() {
            event.setAssignedUsers(new HashSet<>());

            EventResponse response = eventService.toResponse(event);

            assertThat(response.assignedUserIds()).isEmpty();
            assertThat(response.ownerId()).isEqualTo(owner.getId());
        }
    }
}
