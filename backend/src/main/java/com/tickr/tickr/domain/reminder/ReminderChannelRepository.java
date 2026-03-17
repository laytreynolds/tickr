package com.tickr.tickr.domain.reminder;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReminderChannelRepository extends JpaRepository<ReminderChannel, UUID> {
}
