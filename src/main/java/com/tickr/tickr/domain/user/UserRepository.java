package com.tickr.tickr.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findAllByIdIn(List<UUID> ids);
}