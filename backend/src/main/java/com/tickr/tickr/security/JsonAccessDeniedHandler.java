package com.tickr.tickr.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns 403 with a JSON body when the user is authenticated but not allowed to access the resource.
 * Logs the failure server-side for auditing.
 */
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonAccessDeniedHandler.class);
    private static final String MESSAGE = "Access denied.";

    private final SecurityErrorWriter errorWriter;

    public JsonAccessDeniedHandler(SecurityErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Access denied to {} {}: {}", request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());
        errorWriter.write(response, HttpServletResponse.SC_FORBIDDEN, MESSAGE);
    }
}
