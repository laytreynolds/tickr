package com.tickr.tickr.domain.user;

import com.tickr.tickr.dto.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .phoneNumber("+15551234567")
                .timezone("America/New_York")
                .passwordHash("encoded-password")
                .build();
    }

    @Nested
    @DisplayName("getUsers")
    class GetUsers {

        @Test
        @DisplayName("should return all users")
        void shouldReturnAllUsers() {
            given(userRepository.findAll()).willReturn(List.of(user));

            List<User> result = userService.getUsers();

            assertThat(result).hasSize(1).containsExactly(user);
            then(userRepository).should().findAll();
        }

        @Test
        @DisplayName("should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsers() {
            given(userRepository.findAll()).willReturn(Collections.emptyList());

            List<User> result = userService.getUsers();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUser")
    class GetUser {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUserWhenFound() {
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

            User result = userService.getUser(user.getId());

            assertThat(result).isEqualTo(user);
            assertThat(result.getPhoneNumber()).isEqualTo("+15551234567");
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            UUID id = UUID.randomUUID();
            given(userRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUser(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found with id:");
        }
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        private CreateUserRequest request;

        @BeforeEach
        void setUp() {
            request = new CreateUserRequest();
            request.setPhoneNumber("+15551234567");
            request.setTimezone("America/New_York");
            request.setPassword("securePassword123");
        }

        @Test
        @DisplayName("should create user with encoded password")
        void shouldCreateUserWithEncodedPassword() {
            given(passwordEncoder.encode("securePassword123")).willReturn("encoded-password");
            given(userRepository.save(any(User.class))).willReturn(user);

            User result = userService.createUser(request);

            assertThat(result).isNotNull();
            assertThat(result.getPhoneNumber()).isEqualTo("+15551234567");
            then(passwordEncoder).should().encode("securePassword123");
            then(userRepository).should().save(any(User.class));
        }

        @Test
        @DisplayName("should throw when password is null")
        void shouldThrowWhenPasswordIsNull() {
            request.setPassword(null);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Password is required");
        }

        @Test
        @DisplayName("should throw when password is blank")
        void shouldThrowWhenPasswordIsBlank() {
            request.setPassword("   ");

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Password is required");
        }

        @Test
        @DisplayName("should throw when timezone is invalid")
        void shouldThrowWhenTimezoneIsInvalid() {
            request.setTimezone("Invalid/Timezone");

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid timezone");
        }

        @Test
        @DisplayName("should throw when timezone is null")
        void shouldThrowWhenTimezoneIsNull() {
            request.setTimezone(null);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Timezone is required");
        }
    }
}
