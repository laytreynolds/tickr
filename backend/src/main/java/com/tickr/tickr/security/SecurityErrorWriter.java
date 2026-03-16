package com.tickr.tickr.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickr.tickr.dto.ApiError;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

/**
 * Writes a consistent JSON error body for security-related responses (401, 403, etc.)
 * so the client always receives {@link ApiError} format instead of empty or HTML.
 */
@Component
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(new ApiError(message)));
    }
}
