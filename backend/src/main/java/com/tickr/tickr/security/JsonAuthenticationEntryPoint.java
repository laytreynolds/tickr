package com.tickr.tickr.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns 401 with a JSON body when the request is unauthenticated (no/invalid token).
 * Logs the failure server-side for auditing.
 */
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(JsonAuthenticationEntryPoint.class);
    private static final String MESSAGE = "Authentication required.";

    private final SecurityErrorWriter errorWriter;

    public JsonAuthenticationEntryPoint(SecurityErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("Unauthorized request to {} {}: {}", request.getMethod(), request.getRequestURI(), authException.getMessage());
        errorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, MESSAGE);
    }
}
