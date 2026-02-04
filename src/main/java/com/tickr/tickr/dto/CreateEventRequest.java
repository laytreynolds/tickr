package com.tickr.tickr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class CreateEventRequest {
    @JsonProperty("owner_id")
    private UUID ownerId;

    @JsonProperty("assigned_user_ids")
    private List<UUID> assignedUserIds;

    private String title;
    private String description;

    @JsonProperty("start_time")
    private Instant startTime;

    @JsonProperty("end_time")
    private Instant endTime;

    private String source;
}
