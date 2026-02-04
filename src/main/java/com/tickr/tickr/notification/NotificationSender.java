package com.tickr.tickr.notification;

import com.tickr.tickr.dto.NotificationMessage;

public interface NotificationSender {
    void send(NotificationMessage message);
}
