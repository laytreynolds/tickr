package com.tickr.tickr.api;

import com.tickr.tickr.domain.event.EventRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;

import java.util.List;
import com.tickr.tickr.domain.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/tickr/api/v1")
public class EventController {

    private final EventRepository eventRepository;

    @Autowired
    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository; 
    }

    @GetMapping("/events")
    public ResponseEntity<List<Event>> getEvents() {
        List<Event> events = eventRepository.findAll();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
