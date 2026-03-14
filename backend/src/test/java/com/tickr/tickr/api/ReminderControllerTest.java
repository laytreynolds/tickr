package com.tickr.tickr.api;

import com.tickr.tickr.domain.event.Event;
import com.tickr.tickr.domain.reminder.Reminder;
import com.tickr.tickr.domain.reminder.ReminderService;
import com.tickr.tickr.domain.user.User;
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

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReminderController.class)
@DisplayName("ReminderController")
class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReminderService reminderService;

    @MockitoBean
    private UserDetailsService userDetailsService;

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

        reminder = Reminder.builder()
                .id(UUID.randomUUID())
                .event(event)
                .user(user)
                .remindAt(Instant.now())
                .status(Reminder.Status.PENDING)
                .channel(Reminder.Channel.EMAIL)
                .build();
    }

    @Nested
    @DisplayName("GET /tickr/api/v1/reminder/reminders")
    class GetReminders {

        @Test
        @WithMockUser
        @DisplayName("should return 200 with list of reminders")
        void shouldReturnReminders() throws Exception {
            given(reminderService.getReminders()).willReturn(List.of(reminder));

            mockMvc.perform(get("/tickr/api/v1/reminder/reminders"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].status").value("PENDING"))
                    .andExpect(jsonPath("$[0].channel").value("EMAIL"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 200 with empty list when no reminders")
        void shouldReturnEmptyList() throws Exception {
            given(reminderService.getReminders()).willReturn(Collections.emptyList());

            mockMvc.perform(get("/tickr/api/v1/reminder/reminders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

    }

    @Nested
    @DisplayName("DELETE /tickr/api/v1/reminder/reminders/{id}")
    class DeleteReminder {

        @Test
        @WithMockUser
        @DisplayName("should return 204 no content on successful delete")
        void shouldDeleteReminder() throws Exception {
            UUID reminderId = UUID.randomUUID();

            mockMvc.perform(delete("/tickr/api/v1/reminder/reminders/{id}", reminderId))
                    .andExpect(status().isNoContent());

            then(reminderService).should().deleteReminder(reminderId);
        }
    }
}
