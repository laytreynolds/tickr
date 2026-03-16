package com.tickr.tickr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standard error body returned to the client. Message is kept simple for the frontend;
 * detailed information is logged server-side only.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String message) {
    public static final String GENERIC_MESSAGE = "Something went wrong.";

    public static ApiError generic() {
        return new ApiError(GENERIC_MESSAGE);
    }
}
