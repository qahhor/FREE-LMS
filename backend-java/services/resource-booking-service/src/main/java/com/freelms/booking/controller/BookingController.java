package com.freelms.booking.controller;

import com.freelms.booking.entity.Booking;
import com.freelms.booking.service.AvailabilityService;
import com.freelms.booking.service.ResourceBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Resource booking management")
public class BookingController {

    private final ResourceBookingService bookingService;
    private final AvailabilityService availabilityService;

    public BookingController(ResourceBookingService bookingService, AvailabilityService availabilityService) {
        this.bookingService = bookingService;
        this.availabilityService = availabilityService;
    }

    @PostMapping
    @Operation(summary = "Create a booking")
    public ResponseEntity<Booking> createBooking(
            @RequestParam UUID resourceId,
            @RequestBody Booking booking,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(bookingService.createBooking(resourceId, booking, userId));
    }

    @PostMapping("/recurring")
    @Operation(summary = "Create recurring booking")
    public ResponseEntity<Booking> createRecurringBooking(
            @RequestParam UUID resourceId,
            @RequestBody Booking booking,
            @RequestParam Booking.RecurrencePattern pattern,
            @RequestParam(defaultValue = "1") int interval,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(bookingService.createRecurringBooking(
                resourceId, booking, userId, pattern, interval, endDate));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<Booking> getBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.getBooking(bookingId));
    }

    @PostMapping("/{bookingId}/approve")
    @Operation(summary = "Approve a pending booking")
    public ResponseEntity<Booking> approveBooking(
            @PathVariable UUID bookingId,
            @RequestHeader("X-User-Id") Long approverId) {
        return ResponseEntity.ok(bookingService.approveBooking(bookingId, approverId));
    }

    @PostMapping("/{bookingId}/reject")
    @Operation(summary = "Reject a pending booking")
    public ResponseEntity<Booking> rejectBooking(
            @PathVariable UUID bookingId,
            @RequestParam String reason,
            @RequestHeader("X-User-Id") Long approverId) {
        return ResponseEntity.ok(bookingService.rejectBooking(bookingId, approverId, reason));
    }

    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable UUID bookingId,
            @RequestParam(required = false) String reason,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId, userId, reason));
    }

    @PostMapping("/{bookingId}/check-in")
    @Operation(summary = "Check in to a booking")
    public ResponseEntity<Booking> checkIn(
            @PathVariable UUID bookingId,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(bookingService.checkIn(bookingId, userId));
    }

    @PostMapping("/{bookingId}/check-out")
    @Operation(summary = "Check out from a booking")
    public ResponseEntity<Booking> checkOut(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.checkOut(bookingId));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's bookings")
    public ResponseEntity<Page<Booking>> getMyBookings(
            @RequestHeader("X-User-Id") Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId, pageable));
    }

    @GetMapping("/pending-approvals")
    @Operation(summary = "Get pending approvals for manager")
    public ResponseEntity<List<Booking>> getPendingApprovals(
            @RequestHeader("X-User-Id") Long managerId) {
        return ResponseEntity.ok(bookingService.getPendingApprovals(managerId));
    }

    @GetMapping("/calendar/{resourceId}")
    @Operation(summary = "Get calendar view for resource")
    public ResponseEntity<Map<LocalDate, List<Booking>>> getCalendar(
            @PathVariable UUID resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(availabilityService.getCalendarView(resourceId, startDate, endDate));
    }
}
