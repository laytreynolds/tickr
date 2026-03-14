package com.tickr.tickr.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User")
class UserTest {

    @Test
    @DisplayName("builder should create user with defaults")
    void builderShouldCreateUserWithDefaults() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .phoneNumber("+15551234567")
                .timezone("America/New_York")
                .passwordHash("encoded-password")
                .build();

        assertThat(user.getPhoneNumber()).isEqualTo("+15551234567");
        assertThat(user.getTimezone()).isEqualTo("America/New_York");
        assertThat(user.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(user.getAssignedEvents()).isNotNull().isEmpty();
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("setters should update fields")
    void settersShouldUpdateFields() {
        User user = User.builder()
                .phoneNumber("+1")
                .timezone("UTC")
                .passwordHash("h")
                .build();

        user.setPhoneNumber("+15559999999");
        user.setTimezone("Europe/London");
        user.setPasswordHash("new-hash");
        user.setAssignedEvents(new HashSet<>());

        assertThat(user.getPhoneNumber()).isEqualTo("+15559999999");
        assertThat(user.getTimezone()).isEqualTo("Europe/London");
        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.getAssignedEvents()).isEmpty();
    }

    @Test
    @DisplayName("no-args constructor should create user")
    void noArgsConstructorShouldCreateUser() {
        User user = new User();
        assertThat(user.getId()).isNull();
        assertThat(user.getPhoneNumber()).isNull();
    }
}
