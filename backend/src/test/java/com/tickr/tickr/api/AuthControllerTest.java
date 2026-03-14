package com.tickr.tickr.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickr.tickr.domain.user.auth.AuthService;
import com.tickr.tickr.dto.AuthRequest;
import com.tickr.tickr.dto.AuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Nested
    @DisplayName("POST /tickr/api/v1/auth/login")
    class Login {

        @Test
        @WithMockUser
        @DisplayName("should return 200 with auth response on successful login")
        void shouldReturnAuthResponse() throws Exception {
            AuthRequest request = new AuthRequest("+15551234567", "password123");
            AuthResponse response = new AuthResponse("jwt-token", "Bearer", 3600000L);

            given(authService.login(any(AuthRequest.class))).willReturn(response);

            mockMvc.perform(post("/tickr/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresInMs").value(3600000));

            then(authService).should().login(any(AuthRequest.class));
        }

        @Test
        @WithMockUser
        @DisplayName("should propagate BadCredentialsException when credentials are invalid")
        void shouldPropagateExceptionWhenCredentialsInvalid() {
            AuthRequest request = new AuthRequest("+15551234567", "wrongpassword");

            given(authService.login(any(AuthRequest.class)))
                    .willThrow(new BadCredentialsException("Invalid credentials"));

            org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                    mockMvc.perform(post("/tickr/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            );

            then(authService).should().login(any(AuthRequest.class));
        }
    }
}
