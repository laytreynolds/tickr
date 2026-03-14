package com.tickr.tickr.dto;

import java.util.List;

public record SmsRequest(
        List<SmsNotification> messages
) {
}

