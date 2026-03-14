package com.tickr.tickr.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findAllByIdIn(List<UUID> ids);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.phoneNumber = :phoneNumber
            """)
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}