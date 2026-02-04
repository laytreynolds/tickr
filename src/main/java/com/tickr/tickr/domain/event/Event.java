package com.tickr.tickr.domain.event;

import com.tickr.tickr.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Instant startTime;

    private Instant endTime;

    @Column(nullable = false)
    private String source; // MANUAL, GOOGLE, OUTLOOK

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
