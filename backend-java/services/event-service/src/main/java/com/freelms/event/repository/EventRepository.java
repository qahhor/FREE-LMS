package com.freelms.event.repository;

import com.freelms.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Event entities.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.startTime BETWEEN :start AND :end AND e.status = 'SCHEDULED'")
    List<Event> findScheduledEventsBetween(
            @Param("start") ZonedDateTime start,
            @Param("end") ZonedDateTime end);

    @Query("SELECT e FROM Event e WHERE e.organizerId = :userId OR EXISTS " +
           "(SELECT r FROM EventRegistration r WHERE r.event = e AND r.userId = :userId AND r.status = 'REGISTERED')")
    Page<Event> findUserEvents(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.eventType = :type AND e.status = 'SCHEDULED' " +
           "AND e.startTime > :now ORDER BY e.startTime ASC")
    Page<Event> findUpcomingByType(
            @Param("type") Event.EventType type,
            @Param("now") ZonedDateTime now,
            Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.courseId = :courseId AND e.status = 'SCHEDULED'")
    List<Event> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT e FROM Event e WHERE e.status = 'SCHEDULED' AND e.startTime BETWEEN :now AND :reminder")
    List<Event> findEventsNeedingReminder(
            @Param("now") ZonedDateTime now,
            @Param("reminder") ZonedDateTime reminder);

    Optional<Event> findByExternalMeetingId(String externalMeetingId);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizerId = :organizerId AND e.status = 'SCHEDULED'")
    Long countActiveEventsByOrganizer(@Param("organizerId") Long organizerId);
}
