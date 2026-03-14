package com.tickr.tickr.domain.user.auth;

import com.tickr.tickr.domain.user.User;
import com.tickr.tickr.domain.user.UserRepository;
import com.tickr.tickr.dto.AuthRequest;
import com.tickr.tickr.dto.AuthResponse;
import com.tickr.tickr.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private AuthRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .phoneNumber("+15551234567")
                .timezone("America/New_York")
                .passwordHash("encoded-password")
                .build();

        request = new AuthRequest("+15551234567", "password123");
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("should return auth response on successful login")
        void shouldReturnAuthResponseOnSuccess() {
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(new UsernamePasswordAuthenticationToken(user.getPhoneNumber(), null));
            given(userRepository.findByPhoneNumber("+15551234567"))
                    .willReturn(Optional.of(user));
            given(jwtService.generateToken(user)).willReturn("jwt-token");
            given(jwtService.getExpirationMs()).willReturn(3600000L);

            AuthResponse response = authService.login(request);

            assertThat(response.accessToken()).isEqualTo("jwt-token");
            assertThat(response.tokenType()).isEqualTo("Bearer");
            assertThat(response.expiresInMs()).isEqualTo(3600000L);
            then(authenticationManager).should().authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("should throw BadCredentialsException when authentication fails")
        void shouldThrowWhenAuthenticationFails() {
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid credentials");
        }

        @Test
        @DisplayName("should throw BadCredentialsException when user not found after auth")
        void shouldThrowWhenUserNotFoundAfterAuth() {
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(new UsernamePasswordAuthenticationToken(user.getPhoneNumber(), null));
            given(userRepository.findByPhoneNumber("+15551234567"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid credentials");
        }
    }
}
