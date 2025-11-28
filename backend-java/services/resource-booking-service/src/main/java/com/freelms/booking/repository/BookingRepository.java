package com.freelms.booking.repository;

import com.freelms.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * Find conflicting bookings for a resource in a time range.
     */
    @Query("SELECT b FROM Booking b WHERE b.resource.id = :resourceId " +
           "AND b.status NOT IN ('CANCELLED', 'REJECTED') " +
           "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findConflictingBookings(
            @Param("resourceId") UUID resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Check if a time slot is available.
     */
    @Query("SELECT CASE WHEN COUNT(b) = 0 THEN true ELSE false END FROM Booking b " +
           "WHERE b.resource.id = :resourceId " +
           "AND b.status NOT IN ('CANCELLED', 'REJECTED') " +
           "AND b.startTime < :endTime AND b.endTime > :startTime")
    boolean isTimeSlotAvailable(
            @Param("resourceId") UUID resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    Page<Booking> findByBookedBy(Long userId, Pageable pageable);

    Page<Booking> findByResourceId(UUID resourceId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.resource.id = :resourceId " +
           "AND b.startTime >= :start AND b.startTime < :end " +
           "AND b.status NOT IN ('CANCELLED', 'REJECTED') " +
           "ORDER BY b.startTime")
    List<Booking> findByResourceAndDateRange(
            @Param("resourceId") UUID resourceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.bookedBy = :userId " +
           "AND b.startTime >= :start AND b.startTime < :end " +
           "ORDER BY b.startTime")
    List<Booking> findUserBookingsInRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    Page<Booking> findByStatus(Booking.BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.resource.managerId = :managerId " +
           "AND b.status = 'PENDING' ORDER BY b.createdAt")
    List<Booking> findPendingApprovalsByManager(@Param("managerId") Long managerId);

    @Query("SELECT b FROM Booking b WHERE b.startTime BETWEEN :start AND :end " +
           "AND b.status = 'APPROVED' AND b.checkedInAt IS NULL")
    List<Booking> findUpcomingBookingsWithoutCheckIn(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<Booking> findByParentBookingId(UUID parentBookingId);

    @Query("SELECT b FROM Booking b WHERE b.courseId = :courseId ORDER BY b.startTime")
    List<Booking> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT b FROM Booking b WHERE b.eventId = :eventId")
    List<Booking> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.resource.id = :resourceId " +
           "AND b.status = 'COMPLETED' " +
           "AND b.startTime >= :start")
    Long countCompletedBookings(
            @Param("resourceId") UUID resourceId,
            @Param("start") LocalDateTime start);

    @Query("SELECT SUM(TIMESTAMPDIFF(HOUR, b.startTime, b.endTime)) FROM Booking b " +
           "WHERE b.resource.id = :resourceId " +
           "AND b.status = 'COMPLETED' " +
           "AND b.startTime >= :start")
    Long sumBookedHours(
            @Param("resourceId") UUID resourceId,
            @Param("start") LocalDateTime start);
}
