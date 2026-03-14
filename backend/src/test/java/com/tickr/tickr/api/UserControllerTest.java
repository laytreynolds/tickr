package com.tickr.tickr.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickr.tickr.domain.user.User;
import com.tickr.tickr.domain.user.UserService;
import com.tickr.tickr.dto.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsService userDetailsService;

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
    @DisplayName("GET /tickr/api/v1/user/getusers")
    class GetUsers {

        @Test
        @WithMockUser
        @DisplayName("should return 200 with list of users")
        void shouldReturnUsers() throws Exception {
            given(userService.getUsers()).willReturn(List.of(user));

            mockMvc.perform(get("/tickr/api/v1/user/getusers"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].phoneNumber").value("+15551234567"))
                    .andExpect(jsonPath("$[0].timezone").value("America/New_York"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 200 with empty list when no users")
        void shouldReturnEmptyList() throws Exception {
            given(userService.getUsers()).willReturn(Collections.emptyList());

            mockMvc.perform(get("/tickr/api/v1/user/getusers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

    }

    @Nested
    @DisplayName("GET /tickr/api/v1/user/getuser/{id}")
    class GetUserById {

        @Test
        @WithMockUser
        @DisplayName("should return 200 with user when found")
        void shouldReturnUser() throws Exception {
            given(userService.getUser(user.getId())).willReturn(user);

            mockMvc.perform(get("/tickr/api/v1/user/getuser/{id}", user.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.phoneNumber").value("+15551234567"))
                    .andExpect(jsonPath("$.timezone").value("America/New_York"));
        }
    }

    @Nested
    @DisplayName("POST /tickr/api/v1/user/adduser")
    class AddUser {

        @Test
        @WithMockUser
        @DisplayName("should return 200 with created user")
        void shouldCreateUser() throws Exception {
            CreateUserRequest request = new CreateUserRequest();
            request.setPhoneNumber("+15551234567");
            request.setTimezone("America/New_York");
            request.setPassword("securePassword123");

            given(userService.createUser(any(CreateUserRequest.class))).willReturn(user);

            mockMvc.perform(post("/tickr/api/v1/user/adduser")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.phoneNumber").value("+15551234567"))
                    .andExpect(jsonPath("$.timezone").value("America/New_York"));

            then(userService).should().createUser(any(CreateUserRequest.class));
        }
    }
}
