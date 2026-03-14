package com.tickr.tickr.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventResponse(
        UUID id,
        UUID ownerId,
        List<UUID> assignedUserIds,
        String title,
        String description,
        Instant startTime,
        Instant endTime,
        String timezone,
        Integer source,
        Instant createdAt
) {}
