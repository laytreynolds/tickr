package com.tickr.tickr.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityErrorWriter securityErrorWriter;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtDecoder, userDetailsService, securityErrorWriter);
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("shouldNotFilter")
    class ShouldNotFilter {

        @Test
        @DisplayName("should skip auth endpoints")
        void shouldSkipAuthEndpoints() {
            given(request.getRequestURI()).willReturn("/tickr/api/v1/auth/**");
            assertThat(filter.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("should skip health endpoints")
        void shouldSkipHealthEndpoints() {
            given(request.getRequestURI()).willReturn("/tickr/health/**");
            assertThat(filter.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("should skip actuator endpoints")
        void shouldSkipActuatorEndpoints() {
            given(request.getRequestURI()).willReturn("/actuator/**");
            assertThat(filter.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("should not skip protected endpoints")
        void shouldNotSkipProtectedEndpoints() {
            given(request.getRequestURI()).willReturn("/tickr/api/v1/event/getevents");
            assertThat(filter.shouldNotFilter(request)).isFalse();
        }
    }

    @Nested
    @DisplayName("doFilterInternal")
    class DoFilterInternal {

        @Test
        @DisplayName("should pass through when no Authorization header")
        void shouldPassThroughWithNoAuthHeader() throws Exception {
            given(request.getHeader("Authorization")).willReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            then(filterChain).should().doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("should pass through when Authorization is not Bearer")
        void shouldPassThroughWhenNotBearer() throws Exception {
            given(request.getHeader("Authorization")).willReturn("Basic abc123");

            filter.doFilterInternal(request, response, filterChain);

            then(filterChain).should().doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("should set authentication when valid JWT token")
        void shouldSetAuthenticationWhenValidToken() throws Exception {
            String token = "valid-jwt-token";
            given(request.getHeader("Authorization")).willReturn("Bearer " + token);
            given(request.getRemoteAddr()).willReturn("127.0.0.1");

            Jwt jwt = Jwt.withTokenValue(token)
                    .header("alg", "HS256")
                    .subject("+15551234567")
                    .claim("userId", "some-uuid")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            given(jwtDecoder.decode(token)).willReturn(jwt);

            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername("+15551234567")
                    .password("password")
                    .authorities("ROLE_USER")
                    .build();

            given(userDetailsService.loadUserByUsername("+15551234567")).willReturn(userDetails);

            filter.doFilterInternal(request, response, filterChain);

            then(filterChain).should().doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                    .isEqualTo("+15551234567");
        }

        @Test
        @DisplayName("should return 401 when JWT is invalid")
        void shouldReturn401WhenJwtInvalid() throws Exception {
            String token = "invalid-jwt-token";
            given(request.getHeader("Authorization")).willReturn("Bearer " + token);
            given(request.getMethod()).willReturn("GET");
            given(request.getRequestURI()).willReturn("/tickr/api/v1/event/getevents");
            given(jwtDecoder.decode(token)).willThrow(new JwtException("Invalid token"));

            filter.doFilterInternal(request, response, filterChain);

            then(securityErrorWriter).should().write(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token.");
            then(filterChain).should(never()).doFilter(request, response);
        }

        @Test
        @DisplayName("should not override existing authentication")
        void shouldNotOverrideExistingAuthentication() throws Exception {
            // Set existing authentication
            UserDetails existingUser = org.springframework.security.core.userdetails.User
                    .withUsername("existing-user")
                    .password("pw")
                    .authorities("ROLE_USER")
                    .build();
            SecurityContextHolder.getContext().setAuthentication(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            existingUser, null, existingUser.getAuthorities()));

            String token = "valid-jwt-token";
            given(request.getHeader("Authorization")).willReturn("Bearer " + token);

            Jwt jwt = Jwt.withTokenValue(token)
                    .header("alg", "HS256")
                    .subject("+15551234567")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
            given(jwtDecoder.decode(token)).willReturn(jwt);

            filter.doFilterInternal(request, response, filterChain);

            then(filterChain).should().doFilter(request, response);
            // Original authentication should still be there
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                    .isEqualTo("existing-user");
        }
    }
}
