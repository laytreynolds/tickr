package com.tickr.tickr.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DomainController.class)
@DisplayName("DomainController")
class DomainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Nested
    @DisplayName("GET /tickr/health")
    class Ping {

        @Test
        @WithMockUser
        @DisplayName("should return 200 with 'OK'")
        void shouldReturnPong() throws Exception {
            mockMvc.perform(get("/tickr/health"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("OK"));
        }
    }
}
