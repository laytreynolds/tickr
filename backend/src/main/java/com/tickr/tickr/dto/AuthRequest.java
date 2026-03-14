package com.tickr.tickr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthRequest(
        @JsonProperty("phoneNumber") String phoneNumber,
        @JsonProperty("password") String password
) {
}
