package com.tickr.tickr.domain.reminder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    @Query("""
            select r
            from Reminder r
            where r.status = com.tickr.tickr.domain.reminder.Reminder$Status.PENDING
              and r.remindAt <= :now
            """)
    List<Reminder> findDueReminders(@Param("now") Instant now);

    @Modifying(clearAutomatically = true)
    @Query("delete from Reminder r where r.event.id = :eventId")
    void deleteByEventId(@Param("eventId") UUID eventId);
}
