package com.tickr.tickr.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tickr.tickr.domain.event.Event;
import com.tickr.tickr.domain.event.EventService;
import com.tickr.tickr.domain.user.User;
import com.tickr.tickr.dto.CreateEventRequest;
import com.tickr.tickr.dto.EventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@DisplayName("EventController")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Event event;
    private EventResponse eventResponse;
    private User owner;

    @BeforeEach
    void setUp() {
        UUID eventId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        owner = User.builder()
                .id(ownerId)
                .phoneNumber("+15551234567")
                .timezone("America/New_York")
                .passwordHash("hashed")
                .build();

        event = Event.builder()
                .id(eventId)
                .owner(owner)
                .title("Team Meeting")
                .description("Weekly sync")
                .timezone("America/New_York")
                .startTime(Instant.parse("2026-03-01T10:00:00Z"))
                .endTime(Instant.parse("2026-03-01T11:00:00Z"))
                .source(0)
                .build();

        eventResponse = new EventResponse(
                eventId, ownerId, List.of(), "Team Meeting", "Weekly sync",
                Instant.parse("2026-03-01T10:00:00Z"),
                Instant.parse("2026-03-01T11:00:00Z"),
                "America/New_York", 0, Instant.now()
        );
    }

    @Nested
    @DisplayName("GET /tickr/api/v1/event/getevents")
    class GetEvents {

        @Test
        @WithMockUser
        @DisplayName("should return 200 with list of events")
        void shouldReturnEvents() throws Exception {
            given(eventService.getEvents()).willReturn(List.of(event));
            given(eventService.toResponse(event)).willReturn(eventResponse);

            mockMvc.perform(get("/tickr/api/v1/event/getevents"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].title").value("Team Meeting"))
                    .andExpect(jsonPath("$[0].description").value("Weekly sync"))
                    .andExpect(jsonPath("$[0].timezone").value("America/New_York"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 200 with empty list when no events")
        void shouldReturnEmptyList() throws Exception {
            given(eventService.getEvents()).willReturn(Collections.emptyList());

            mockMvc.perform(get("/tickr/api/v1/event/getevents"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

    }

    @Nested
    @DisplayName("POST /tickr/api/v1/event/addevent")
    class AddEvent {

        @Test
        @WithMockUser
        @DisplayName("should return 200 with created event")
        void shouldCreateEvent() throws Exception {
            CreateEventRequest request = new CreateEventRequest();
            request.setOwnerId(owner.getId());
            request.setTitle("Team Meeting");
            request.setDescription("Weekly sync");
            request.setStartTime(Instant.parse("2026-03-01T10:00:00Z"));
            request.setEndTime(Instant.parse("2026-03-01T11:00:00Z"));
            request.setSource("0");
            request.setTimezone("America/New_York");

            given(eventService.createEvent(any(CreateEventRequest.class))).willReturn(event);
            given(eventService.toResponse(event)).willReturn(eventResponse);

            mockMvc.perform(post("/tickr/api/v1/event/addevent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Team Meeting"))
                    .andExpect(jsonPath("$.description").value("Weekly sync"));

            then(eventService).should().createEvent(any(CreateEventRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /tickr/api/v1/event/deleteevent/{id}")
    class DeleteEvent {

        @Test
        @WithMockUser
        @DisplayName("should return 204 no content on successful delete")
        void shouldDeleteEvent() throws Exception {
            UUID eventId = UUID.randomUUID();

            mockMvc.perform(delete("/tickr/api/v1/event/deleteevent/{id}", eventId))
                    .andExpect(status().isNoContent());

            then(eventService).should().deleteEvent(eventId);
        }
    }
}
