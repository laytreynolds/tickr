package com.tickr.tickr.domain.event;

import com.tickr.tickr.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Event")
class EventTest {

    @Test
    @DisplayName("builder should create event with defaults")
    void builderShouldCreateEventWithDefaults() {
        User owner = User.builder()
                .id(UUID.randomUUID())
                .phoneNumber("+15551234567")
                .timezone("UTC")
                .passwordHash("hash")
                .build();

        Event event = Event.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .title("Meeting")
                .description("Team sync")
                .timezone("America/New_York")
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(3600))
                .source(0)
                .build();

        assertThat(event.getOwner()).isEqualTo(owner);
        assertThat(event.getTitle()).isEqualTo("Meeting");
        assertThat(event.getAssignedUsers()).isNotNull().isEmpty();
        assertThat(event.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("setOwner should update the owner")
    void setOwnerShouldUpdateOwner() {
        User owner1 = User.builder().id(UUID.randomUUID()).phoneNumber("+1")
                .timezone("UTC").passwordHash("h").build();
        User owner2 = User.builder().id(UUID.randomUUID()).phoneNumber("+2")
                .timezone("UTC").passwordHash("h").build();

        Event event = Event.builder()
                .owner(owner1)
                .title("Test").timezone("UTC")
                .startTime(Instant.now()).source(0)
                .build();

        event.setOwner(owner2);
        assertThat(event.getOwner()).isEqualTo(owner2);
    }

    @Test
    @DisplayName("Source enum should have expected values")
    void sourceEnumShouldHaveExpectedValues() {
        assertThat(Event.Source.values())
                .containsExactly(Event.Source.API, Event.Source.GOOGLE, Event.Source.EMAIL);
    }

    @Test
    @DisplayName("should manage assigned users set")
    void shouldManageAssignedUsersSet() {
        Event event = Event.builder()
                .title("Test").timezone("UTC")
                .startTime(Instant.now()).source(0)
                .owner(User.builder().id(UUID.randomUUID()).phoneNumber("+1")
                        .timezone("UTC").passwordHash("h").build())
                .build();

        assertThat(event.getAssignedUsers()).isEmpty();

        HashSet<EventUser> users = new HashSet<>();
        event.setAssignedUsers(users);
        assertThat(event.getAssignedUsers()).isSameAs(users);
    }
}
