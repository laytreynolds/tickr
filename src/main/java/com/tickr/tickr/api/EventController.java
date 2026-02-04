package com.tickr.tickr.api;

import com.tickr.tickr.domain.event.EventService;
import com.tickr.tickr.dto.CreateEventRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;

import java.util.List;
import com.tickr.tickr.domain.event.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;

@RestController
@RequestMapping("/tickr/api/v1/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping("/getevents")
    public ResponseEntity<List<Event>> getEvents() {
        List<Event> events = this.eventService.getEvents();
        return ResponseEntity.ok(events);
    }

    @DeleteMapping("/deleteevent/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        this.eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/addevent")
    public ResponseEntity<Event> addEvent(@RequestBody CreateEventRequest request) {
        Event createdEvent = eventService.createEvent(request);
        return ResponseEntity.ok(createdEvent);
    }
}
