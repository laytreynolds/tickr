package com.tickr.tickr.domain.event;

import com.tickr.tickr.domain.reminder.ReminderService;
import com.tickr.tickr.domain.reminder.Reminder;
import com.tickr.tickr.domain.user.User;
import com.tickr.tickr.domain.user.UserRepository;
import com.tickr.tickr.dto.CreateEventRequest;
import com.tickr.tickr.dto.EventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ReminderService reminderService;

    @Cacheable(value = "events", key = "#events")
    public List<Event> getEvents() {
        return this.eventRepository.findAll();
    }

    public Event createEvent(CreateEventRequest request) {
        // Fetch owner
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("owner not found with id: " + request.getOwnerId()));

        // Fetch assigned users
        Set<EventUser> assignedUsers = new HashSet<>();

        if (request.getAssignedUserIds() != null && !request.getAssignedUserIds().isEmpty()) {
            List<User> users = userRepository.findAllByIdIn(request.getAssignedUserIds());
            if (users.size() != request.getAssignedUserIds().size()) {
                throw new IllegalArgumentException("One or more assigned users not found");
            }
            for (User user : users) {
                assignedUsers.add(EventUser.builder()
                        .id(new EventUser.EventUserId())
                        .user(user)
                        .build());
            }
        }

        // Build and save event
        Event event = Event.builder()
                .owner(owner)
                .assignedUsers(assignedUsers)
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .timezone(request.getTimezone())
                .source(Integer.parseInt(request.getSource()))
                .build();

        for (EventUser assignment : assignedUsers) {
            assignment.setEvent(event);
        }

        Event savedEvent = eventRepository.save(event);

        // Create reminders for the event after it's persisted
        // If reminder creation fails, log the error but don't fail event creation
        try {
            reminderService.createRemindersForEvent(savedEvent, request.getChannels());
        } catch (Exception e) {
            log.error("Failed to create reminders for event {}: {}", savedEvent.getId(), e.getMessage(), e);
        }

        return savedEvent;
    }

    @Transactional
    public void deleteEvent(UUID id) {
        reminderService.deleteRemindersForEvent(id);
        this.eventRepository.deleteById(id);
    }

    public EventResponse toResponse(Event event) {
        List<UUID> assignedUserIds = event.getAssignedUsers().stream()
                .map(EventUser::getUser)
                .map(User::getId)
                .collect(Collectors.toList());

        return new EventResponse(
                event.getId(),
                event.getOwner().getId(),
                assignedUserIds,
                event.getTitle(),
                event.getDescription(),
                event.getStartTime(),
                event.getEndTime(),
                event.getTimezone(),
                event.getSource(),
                event.getCreatedAt()
        );
    }

    public void remindNow(UUID eventId, List<Reminder.Channel> channels) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("event not found with id: " + eventId));
        reminderService.remindNow(event, channels);
    }
}
