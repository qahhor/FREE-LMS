package com.freelms.event.repository;

import com.freelms.event.entity.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for EventRegistration entities.
 */
@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    Page<EventRegistration> findByEventId(Long eventId, Pageable pageable);

    List<EventRegistration> findByEventIdAndStatus(Long eventId, EventRegistration.RegistrationStatus status);

    Page<EventRegistration> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId AND r.status = 'REGISTERED'")
    Long countRegisteredByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId AND r.status = 'WAITLISTED'")
    Long countWaitlistedByEventId(@Param("eventId") Long eventId);

    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId AND r.status = 'WAITLISTED' " +
           "ORDER BY r.registeredAt ASC")
    List<EventRegistration> findWaitlistByEventIdOrderedByTime(@Param("eventId") Long eventId);

    void deleteByEventIdAndUserId(Long eventId, Long userId);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);
}
