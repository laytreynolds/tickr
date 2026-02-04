package com.tickr.tickr.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInMs
) {
}
