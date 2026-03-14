package com.tickr.tickr.security;

import com.tickr.tickr.domain.user.User;
import com.tickr.tickr.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

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
    @DisplayName("loadUserByUsername")
    class LoadUserByUsername {

        @Test
        @DisplayName("should return UserDetails when user exists")
        void shouldReturnUserDetailsWhenUserExists() {
            given(userRepository.findByPhoneNumber("+15551234567"))
                    .willReturn(Optional.of(user));

            UserDetails result = userDetailsService.loadUserByUsername("+15551234567");

            assertThat(result.getUsername()).isEqualTo("+15551234567");
            assertThat(result.getPassword()).isEqualTo("encoded-password");
            assertThat(result.getAuthorities()).hasSize(1);
            assertThat(result.getAuthorities().iterator().next().getAuthority())
                    .isEqualTo("ROLE_USER");
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            given(userRepository.findByPhoneNumber("+15559999999"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> userDetailsService.loadUserByUsername("+15559999999"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("User not found with phoneNumber");
        }
    }
}
