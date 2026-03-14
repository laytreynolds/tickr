package com.tickr.tickr.security;

import com.tickr.tickr.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService")
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtEncoder, 3600000L);
    }

    @Test
    @DisplayName("should generate token with user claims")
    void shouldGenerateTokenWithUserClaims() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .phoneNumber("+15551234567")
                .timezone("UTC")
                .passwordHash("hash")
                .build();

        Jwt mockJwt = Jwt.withTokenValue("generated-jwt-token")
                .header("alg", "HS256")
                .subject(user.getPhoneNumber())
                .claim("userId", user.getId().toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(3600000))
                .build();

        given(jwtEncoder.encode(any(JwtEncoderParameters.class))).willReturn(mockJwt);

        String token = jwtService.generateToken(user);

        assertThat(token).isEqualTo("generated-jwt-token");
        then(jwtEncoder).should().encode(any(JwtEncoderParameters.class));
    }

    @Test
    @DisplayName("should return configured expiration")
    void shouldReturnConfiguredExpiration() {
        assertThat(jwtService.getExpirationMs()).isEqualTo(3600000L);
    }
}
