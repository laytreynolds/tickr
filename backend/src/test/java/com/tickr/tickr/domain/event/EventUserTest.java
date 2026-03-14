package com.tickr.tickr.domain.event;

import com.tickr.tickr.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventUser")
class EventUserTest {

    @Test
    @DisplayName("should build EventUser with composite key")
    void shouldBuildEventUserWithCompositeKey() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = User.builder().id(userId).phoneNumber("+15551234567")
                .timezone("UTC").passwordHash("hash").build();

        Event event = Event.builder().id(eventId)
                .owner(user).title("Test").timezone("UTC")
                .startTime(Instant.now()).source(0).build();

        EventUser.EventUserId compositeId = new EventUser.EventUserId(eventId, userId);
        EventUser eventUser = EventUser.builder()
                .id(compositeId)
                .event(event)
                .user(user)
                .build();

        assertThat(eventUser.getId().getEventId()).isEqualTo(eventId);
        assertThat(eventUser.getId().getUserId()).isEqualTo(userId);
        assertThat(eventUser.getEvent()).isEqualTo(event);
        assertThat(eventUser.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("EventUserId should support no-args constructor")
    void eventUserIdShouldSupportNoArgsConstructor() {
        EventUser.EventUserId id = new EventUser.EventUserId();
        assertThat(id.getEventId()).isNull();
        assertThat(id.getUserId()).isNull();
    }

    @Test
    @DisplayName("EventUserId setters should work")
    void eventUserIdSettersShouldWork() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        EventUser.EventUserId id = new EventUser.EventUserId();
        id.setEventId(eventId);
        id.setUserId(userId);

        assertThat(id.getEventId()).isEqualTo(eventId);
        assertThat(id.getUserId()).isEqualTo(userId);
    }
}
