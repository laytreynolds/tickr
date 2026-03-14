package com.tickr.tickr.util;

import java.util.Arrays;
import java.util.TimeZone;

public class Timezone {
    public static void validateTimezone(String timezone) {

        if (timezone == null || timezone.isBlank()) {
            throw new IllegalArgumentException("Timezone is required");
        }

        if (!Arrays.stream(TimeZone.getAvailableIDs()).anyMatch(tz -> tz.equals(timezone))) {
            throw new IllegalArgumentException("Invalid timezone: " + timezone);
        }
    }
}
