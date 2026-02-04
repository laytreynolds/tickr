package com.tickr.tickr.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users",
        indexes = @Index(name = "idx_user_phone_number", columnList = "phoneNumber"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String timezone;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
