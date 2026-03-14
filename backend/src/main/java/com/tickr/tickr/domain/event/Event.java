package com.tickr.tickr.domain.event;

import com.tickr.tickr.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
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
    @JoinColumn(nullable = false, name = "user_id")
    private User owner; // Owner/creator of the event

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EventUser> assignedUsers = new HashSet<>(); // Users assigned to the event

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String timezone;

    @Column(nullable = false)
    private Instant startTime;

    private Instant endTime;

    @Column(nullable = false)
    private Integer source; // MANUAL, GOOGLE, OUTLOOK

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public enum Source {
        API, GOOGLE, EMAIL
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

}
