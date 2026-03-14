package com.tickr.tickr.domain.reminder;

import com.tickr.tickr.domain.event.Event;
import com.tickr.tickr.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reminders",
        indexes = @Index(name = "idx_reminder_due", columnList = "remindAt,status"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Event event;

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private Instant remindAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public enum Status {
        PENDING, SENT, FAILED
    }

    public enum Channel {
        SMS, PHONE, EMAIL
    }

    public void markSent() {
        this.status = Status.SENT;
    }
}
