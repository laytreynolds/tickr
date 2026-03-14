package com.tickr.tickr.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Timezone")
class TimezoneTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "America/New_York",
            "America/Chicago",
            "America/Los_Angeles",
            "Europe/London",
            "Asia/Tokyo",
            "UTC"
    })
    @DisplayName("should accept valid timezone IDs")
    void shouldAcceptValidTimezones(String timezone) {
        assertThatCode(() -> Timezone.validateTimezone(timezone))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Invalid/Timezone",
            "Not_A_Zone",
            "America/FakeCity",
            "foo"
    })
    @DisplayName("should reject invalid timezone IDs")
    void shouldRejectInvalidTimezones(String timezone) {
        assertThatThrownBy(() -> Timezone.validateTimezone(timezone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid timezone");
    }

    @Test
    @DisplayName("should throw when timezone is null")
    void shouldThrowWhenTimezoneIsNull() {
        assertThatThrownBy(() -> Timezone.validateTimezone(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Timezone is required");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("should throw when timezone is null, empty, or blank")
    void shouldThrowWhenTimezoneIsNullOrBlank(String timezone) {
        assertThatThrownBy(() -> Timezone.validateTimezone(timezone))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
