package com.tickr.tickr.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Record DTOs")
class RecordDtoTest {

    @Nested
    @DisplayName("AuthRequest")
    class AuthRequestTest {

        @Test
        @DisplayName("should create record with phone number and password")
        void shouldCreateRecord() {
            AuthRequest request = new AuthRequest("+15551234567", "password123");

            assertThat(request.phoneNumber()).isEqualTo("+15551234567");
            assertThat(request.password()).isEqualTo("password123");
        }

        @Test
        @DisplayName("should support equals and hashCode")
        void shouldSupportEquality() {
            AuthRequest r1 = new AuthRequest("+15551234567", "password");
            AuthRequest r2 = new AuthRequest("+15551234567", "password");

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }
    }

    @Nested
    @DisplayName("AuthResponse")
    class AuthResponseTest {

        @Test
        @DisplayName("should create record with token, type, and expiration")
        void shouldCreateRecord() {
            AuthResponse response = new AuthResponse("jwt-token", "Bearer", 3600000L);

            assertThat(response.accessToken()).isEqualTo("jwt-token");
            assertThat(response.tokenType()).isEqualTo("Bearer");
            assertThat(response.expiresInMs()).isEqualTo(3600000L);
        }
    }

    @Nested
    @DisplayName("EventResponse")
    class EventResponseTest {

        @Test
        @DisplayName("should create record with all event fields")
        void shouldCreateRecord() {
            UUID eventId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();

            EventResponse response = new EventResponse(
                    eventId, ownerId, List.of(userId),
                    "Meeting", "Sync", now, now.plusSeconds(3600),
                    "America/New_York", 0, now
            );

            assertThat(response.id()).isEqualTo(eventId);
            assertThat(response.ownerId()).isEqualTo(ownerId);
            assertThat(response.assignedUserIds()).containsExactly(userId);
            assertThat(response.title()).isEqualTo("Meeting");
            assertThat(response.description()).isEqualTo("Sync");
            assertThat(response.timezone()).isEqualTo("America/New_York");
            assertThat(response.source()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("SmsRequest")
    class SmsRequestTest {

        @Test
        @DisplayName("should create record with message list")
        void shouldCreateRecord() {
            SmsNotification sms = new SmsNotification("+1", "Hello");
            SmsRequest request = new SmsRequest(List.of(sms));

            assertThat(request.messages()).hasSize(1);
            assertThat(request.messages().getFirst().getTo()).isEqualTo("+1");
        }
    }

    @Nested
    @DisplayName("EmailRequest")
    class EmailRequestTest {

        @Test
        @DisplayName("should create record with email notification")
        void shouldCreateRecord() {
            EmailNotification email = new EmailNotification("a@b.com", "Subject", "Body");
            EmailRequest request = new EmailRequest(email);

            assertThat(request.email().getTo()).isEqualTo("a@b.com");
            assertThat(request.email().getSubject()).isEqualTo("Subject");
        }
    }

    @Nested
    @DisplayName("CreateEventRequest")
    class CreateEventRequestTest {

        @Test
        @DisplayName("should set and get all fields")
        void shouldSetAndGetAllFields() {
            UUID ownerId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Instant start = Instant.now();
            Instant end = start.plusSeconds(3600);

            CreateEventRequest request = new CreateEventRequest();
            request.setOwnerId(ownerId);
            request.setAssignedUserIds(List.of(userId));
            request.setTitle("Meeting");
            request.setDescription("Sync");
            request.setStartTime(start);
            request.setEndTime(end);
            request.setSource("0");
            request.setTimezone("UTC");

            assertThat(request.getOwnerId()).isEqualTo(ownerId);
            assertThat(request.getAssignedUserIds()).containsExactly(userId);
            assertThat(request.getTitle()).isEqualTo("Meeting");
            assertThat(request.getDescription()).isEqualTo("Sync");
            assertThat(request.getStartTime()).isEqualTo(start);
            assertThat(request.getEndTime()).isEqualTo(end);
            assertThat(request.getSource()).isEqualTo("0");
            assertThat(request.getTimezone()).isEqualTo("UTC");
        }
    }

    @Nested
    @DisplayName("CreateUserRequest")
    class CreateUserRequestTest {

        @Test
        @DisplayName("should set and get all fields")
        void shouldSetAndGetAllFields() {
            CreateUserRequest request = new CreateUserRequest();
            request.setPhoneNumber("+15551234567");
            request.setTimezone("America/New_York");
            request.setPassword("secure123");

            assertThat(request.getPhoneNumber()).isEqualTo("+15551234567");
            assertThat(request.getTimezone()).isEqualTo("America/New_York");
            assertThat(request.getPassword()).isEqualTo("secure123");
        }
    }
}
