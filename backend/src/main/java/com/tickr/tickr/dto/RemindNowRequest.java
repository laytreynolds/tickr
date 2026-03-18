package com.tickr.tickr.dto;

import java.util.List;
import java.util.UUID;

import com.tickr.tickr.domain.reminder.Reminder;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class RemindNowRequest {
    @JsonProperty("event_id")
    private UUID eventId;

    @JsonProperty("channels")
    private List<Reminder.Channel> channels;
}
