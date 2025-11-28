package com.freelms.booking.service;

import com.freelms.booking.entity.Booking;
import com.freelms.booking.entity.Resource;
import com.freelms.booking.repository.BookingRepository;
import com.freelms.booking.repository.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Core service for resource booking operations.
 */
@Service
public class ResourceBookingService {

    private static final Logger log = LoggerFactory.getLogger(ResourceBookingService.class);

    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository;
    private final AvailabilityService availabilityService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ResourceBookingService(
            ResourceRepository resourceRepository,
            BookingRepository bookingRepository,
            AvailabilityService availabilityService,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.resourceRepository = resourceRepository;
        this.bookingRepository = bookingRepository;
        this.availabilityService = availabilityService;
        this.kafkaTemplate = kafkaTemplate;
    }

    // ==================== Resource Management ====================

    @Transactional
    public Resource createResource(Resource resource) {
        Resource saved = resourceRepository.save(resource);
        publishEvent("RESOURCE_CREATED", saved);
        return saved;
    }

    @Transactional
    public Resource updateResource(UUID resourceId, Resource updates) {
        Resource resource = getResource(resourceId);

        if (updates.getName() != null) resource.setName(updates.getName());
        if (updates.getDescription() != null) resource.setDescription(updates.getDescription());
        if (updates.getCapacity() != null) resource.setCapacity(updates.getCapacity());
        if (updates.getFeatures() != null) resource.setFeatures(updates.getFeatures());
        if (updates.getAvailableFrom() != null) resource.setAvailableFrom(updates.getAvailableFrom());
        if (updates.getAvailableUntil() != null) resource.setAvailableUntil(updates.getAvailableUntil());
        if (updates.getAvailableDays() != null) resource.setAvailableDays(updates.getAvailableDays());
        if (updates.getHourlyRate() != null) resource.setHourlyRate(updates.getHourlyRate());
        if (updates.getRequiresApproval() != null) resource.setRequiresApproval(updates.getRequiresApproval());

        Resource saved = resourceRepository.save(resource);
        publishEvent("RESOURCE_UPDATED", saved);
        return saved;
    }

    @Transactional
    public void updateResourceStatus(UUID resourceId, Resource.ResourceStatus status) {
        Resource resource = getResource(resourceId);
        resource.setStatus(status);
        resourceRepository.save(resource);
        publishEvent("RESOURCE_STATUS_CHANGED", Map.of("resourceId", resourceId, "status", status));
    }

    public Resource getResource(UUID resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found: " + resourceId));
    }

    public Page<Resource> getResources(Long organizationId, Resource.ResourceType type, Pageable pageable) {
        if (type != null) {
            return resourceRepository.findByType(type, pageable);
        }
        if (organizationId != null) {
            return resourceRepository.findByOrganizationId(organizationId, pageable);
        }
        return resourceRepository.findAll(pageable);
    }

    // ==================== Booking Management ====================

    @Transactional
    public Booking createBooking(UUID resourceId, Booking booking, Long userId) {
        Resource resource = getResource(resourceId);

        // Validate booking
        AvailabilityService.ValidationResult validation =
                availabilityService.validateBookingRequest(resource, booking.getStartTime(), booking.getEndTime(), userId);

        if (!validation.valid()) {
            throw new RuntimeException("Invalid booking: " + String.join(", ", validation.errors()));
        }

        // Check availability
        if (!availabilityService.isAvailable(resourceId, booking.getStartTime(), booking.getEndTime())) {
            throw new RuntimeException("Time slot is not available");
        }

        booking.setResource(resource);
        booking.setBookedBy(userId);

        // Set status based on approval requirement
        if (resource.getRequiresApproval()) {
            booking.setStatus(Booking.BookingStatus.PENDING);
        } else {
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
        }

        // Calculate cost
        if (resource.getHourlyRate() != null) {
            long hours = ChronoUnit.HOURS.between(booking.getStartTime(), booking.getEndTime());
            booking.setTotalCost(resource.getHourlyRate() * Math.max(1, hours));
        }

        Booking saved = bookingRepository.save(booking);

        publishEvent("BOOKING_CREATED", Map.of(
                "bookingId", saved.getId(),
                "resourceId", resourceId,
                "userId", userId,
                "status", saved.getStatus()
        ));

        return saved;
    }

    @Transactional
    public Booking createRecurringBooking(UUID resourceId, Booking baseBooking, Long userId,
                                           Booking.RecurrencePattern pattern, int interval,
                                           LocalDateTime endDate) {
        // Create parent booking
        baseBooking.setIsRecurring(true);
        baseBooking.setRecurrencePattern(pattern);
        baseBooking.setRecurrenceInterval(interval);
        baseBooking.setRecurrenceEndDate(endDate);

        Booking parent = createBooking(resourceId, baseBooking, userId);

        // Generate recurring instances
        LocalDateTime currentStart = baseBooking.getStartTime();
        LocalDateTime currentEnd = baseBooking.getEndTime();
        long duration = ChronoUnit.MINUTES.between(currentStart, currentEnd);

        while (currentStart.isBefore(endDate)) {
            currentStart = getNextOccurrence(currentStart, pattern, interval);
            currentEnd = currentStart.plusMinutes(duration);

            if (currentStart.isAfter(endDate)) break;

            // Check availability for this instance
            if (availabilityService.isAvailable(resourceId, currentStart, currentEnd)) {
                Booking instance = new Booking();
                instance.setResource(parent.getResource());
                instance.setTitle(parent.getTitle());
                instance.setDescription(parent.getDescription());
                instance.setPurpose(parent.getPurpose());
                instance.setStartTime(currentStart);
                instance.setEndTime(currentEnd);
                instance.setBookedBy(userId);
                instance.setParentBookingId(parent.getId());
                instance.setStatus(parent.getStatus());
                instance.setExpectedAttendees(parent.getExpectedAttendees());

                bookingRepository.save(instance);
            }
        }

        return parent;
    }

    @Transactional
    public Booking approveBooking(UUID bookingId, Long approverId) {
        Booking booking = getBooking(bookingId);

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Booking is not pending approval");
        }

        booking.setStatus(Booking.BookingStatus.APPROVED);
        booking.setApprovedBy(approverId);
        booking.setApprovedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        publishEvent("BOOKING_APPROVED", Map.of(
                "bookingId", bookingId,
                "approverId", approverId
        ));

        return saved;
    }

    @Transactional
    public Booking rejectBooking(UUID bookingId, Long approverId, String reason) {
        Booking booking = getBooking(bookingId);

        booking.setStatus(Booking.BookingStatus.REJECTED);
        booking.setCancelledBy(approverId);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);

        Booking saved = bookingRepository.save(booking);

        publishEvent("BOOKING_REJECTED", Map.of(
                "bookingId", bookingId,
                "reason", reason
        ));

        return saved;
    }

    @Transactional
    public Booking cancelBooking(UUID bookingId, Long userId, String reason) {
        Booking booking = getBooking(bookingId);
        Resource resource = booking.getResource();

        // Check cancellation notice requirement
        if (resource.getCancellationNoticeHours() != null) {
            long hoursUntilStart = ChronoUnit.HOURS.between(LocalDateTime.now(), booking.getStartTime());
            if (hoursUntilStart < resource.getCancellationNoticeHours()) {
                log.warn("Late cancellation for booking {}: {} hours notice, required {}",
                        bookingId, hoursUntilStart, resource.getCancellationNoticeHours());
            }
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledBy(userId);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);

        Booking saved = bookingRepository.save(booking);

        publishEvent("BOOKING_CANCELLED", Map.of(
                "bookingId", bookingId,
                "userId", userId,
                "reason", reason
        ));

        return saved;
    }

    @Transactional
    public Booking checkIn(UUID bookingId, Long userId) {
        Booking booking = getBooking(bookingId);

        if (booking.getStatus() != Booking.BookingStatus.APPROVED &&
            booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking is not ready for check-in");
        }

        booking.setStatus(Booking.BookingStatus.CHECKED_IN);
        booking.setCheckedInAt(LocalDateTime.now());
        booking.setCheckedInBy(userId);

        Booking saved = bookingRepository.save(booking);

        publishEvent("BOOKING_CHECKED_IN", Map.of("bookingId", bookingId));

        return saved;
    }

    @Transactional
    public Booking checkOut(UUID bookingId) {
        Booking booking = getBooking(bookingId);

        booking.setStatus(Booking.BookingStatus.COMPLETED);
        booking.setCheckedOutAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        publishEvent("BOOKING_COMPLETED", Map.of("bookingId", bookingId));

        return saved;
    }

    public Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
    }

    public Page<Booking> getUserBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByBookedBy(userId, pageable);
    }

    public List<Booking> getPendingApprovals(Long managerId) {
        return bookingRepository.findPendingApprovalsByManager(managerId);
    }

    // ==================== Statistics ====================

    public Map<String, Object> getResourceStats(UUID resourceId, LocalDateTime since) {
        Map<String, Object> stats = new HashMap<>();

        Long completedBookings = bookingRepository.countCompletedBookings(resourceId, since);
        Long totalHours = bookingRepository.sumBookedHours(resourceId, since);

        stats.put("completedBookings", completedBookings);
        stats.put("totalBookedHours", totalHours != null ? totalHours : 0);
        stats.put("resourceId", resourceId);
        stats.put("period", Map.of("since", since));

        return stats;
    }

    // ==================== Helpers ====================

    private LocalDateTime getNextOccurrence(LocalDateTime current, Booking.RecurrencePattern pattern, int interval) {
        return switch (pattern) {
            case DAILY -> current.plusDays(interval);
            case WEEKLY -> current.plusWeeks(interval);
            case BIWEEKLY -> current.plusWeeks(2 * interval);
            case MONTHLY -> current.plusMonths(interval);
            case CUSTOM -> current.plusDays(interval);
        };
    }

    private void publishEvent(String eventType, Object data) {
        try {
            kafkaTemplate.send("booking-events", Map.of("eventType", eventType, "data", data));
        } catch (Exception e) {
            log.error("Failed to publish event: {}", e.getMessage());
        }
    }
}
