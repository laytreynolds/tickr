package com.tickr.tickr.domain.event;

import com.tickr.tickr.domain.user.User;
import com.tickr.tickr.domain.user.UserRepository;
import com.tickr.tickr.dto.CreateEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Cacheable(value = "events", key = "#events")
    public List<Event> getEvents() {
        return this.eventRepository.findAll();
    }

    public Event createEvent(CreateEventRequest request) {
        // Fetch owner
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("owner not found with id: " + request.getOwnerId()));

        // Fetch assigned users
        Set<User> assignedUsers = new HashSet<>();
        
        if (request.getAssignedUserIds() != null && !request.getAssignedUserIds().isEmpty()) {
            List<User> users = userRepository.findAllByIdIn(request.getAssignedUserIds());
            if (users.size() != request.getAssignedUserIds().size()) {
                throw new IllegalArgumentException("One or more assigned users not found");
            }
            assignedUsers = new HashSet<>(users);
        }

        // Build and save event
        Event event = Event.builder()
                .owner(owner)
                .assignedUsers(assignedUsers)
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .source(Integer.parseInt(request.getSource()))
                .build();
        
        return eventRepository.save(event);
    }
    
    public void deleteEvent(UUID id) {
        this.eventRepository.deleteById(id);
    }
}
