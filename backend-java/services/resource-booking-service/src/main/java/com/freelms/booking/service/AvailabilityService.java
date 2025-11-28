package com.freelms.booking.service;

import com.freelms.booking.entity.Booking;
import com.freelms.booking.entity.Resource;
import com.freelms.booking.repository.BookingRepository;
import com.freelms.booking.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Service for checking resource availability.
 */
@Service
public class AvailabilityService {

    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository;

    public AvailabilityService(ResourceRepository resourceRepository, BookingRepository bookingRepository) {
        this.resourceRepository = resourceRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Check if a resource is available for a given time slot.
     */
    public boolean isAvailable(UUID resourceId, LocalDateTime start, LocalDateTime end) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        // Check resource status
        if (resource.getStatus() != Resource.ResourceStatus.AVAILABLE) {
            return false;
        }

        // Check operating hours
        if (!isWithinOperatingHours(resource, start, end)) {
            return false;
        }

        // Check available days
        if (!isOnAvailableDay(resource, start)) {
            return false;
        }

        // Check for conflicting bookings
        return bookingRepository.isTimeSlotAvailable(resourceId, start, end);
    }

    /**
     * Find available time slots for a resource on a given date.
     */
    public List<TimeSlot> getAvailableSlots(UUID resourceId, LocalDate date, int slotDurationMinutes) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        List<TimeSlot> availableSlots = new ArrayList<>();

        // Check if the day is available
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (resource.getAvailableDays() != null && !resource.getAvailableDays().contains(dayOfWeek)) {
            return availableSlots;
        }

        LocalTime startTime = resource.getAvailableFrom() != null ? resource.getAvailableFrom() : LocalTime.of(8, 0);
        LocalTime endTime = resource.getAvailableUntil() != null ? resource.getAvailableUntil() : LocalTime.of(18, 0);

        // Get existing bookings for the day
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<Booking> existingBookings = bookingRepository.findByResourceAndDateRange(resourceId, dayStart, dayEnd);

        // Generate potential slots
        LocalTime currentSlotStart = startTime;
        while (currentSlotStart.plusMinutes(slotDurationMinutes).isBefore(endTime) ||
               currentSlotStart.plusMinutes(slotDurationMinutes).equals(endTime)) {

            LocalDateTime slotStart = date.atTime(currentSlotStart);
            LocalDateTime slotEnd = date.atTime(currentSlotStart.plusMinutes(slotDurationMinutes));

            boolean hasConflict = existingBookings.stream()
                    .anyMatch(b -> b.getStartTime().isBefore(slotEnd) && b.getEndTime().isAfter(slotStart));

            if (!hasConflict) {
                availableSlots.add(new TimeSlot(slotStart, slotEnd));
            }

            currentSlotStart = currentSlotStart.plusMinutes(slotDurationMinutes);
        }

        return availableSlots;
    }

    /**
     * Find resources available for a given time slot.
     */
    public List<Resource> findAvailableResources(
            Long organizationId,
            Resource.ResourceType type,
            LocalDateTime start,
            LocalDateTime end,
            Integer minCapacity) {

        List<Resource> candidates = resourceRepository.findAll().stream()
                .filter(r -> r.getOrganizationId().equals(organizationId))
                .filter(r -> r.getStatus() == Resource.ResourceStatus.AVAILABLE)
                .filter(r -> type == null || r.getType() == type)
                .filter(r -> minCapacity == null || (r.getCapacity() != null && r.getCapacity() >= minCapacity))
                .toList();

        return candidates.stream()
                .filter(r -> isWithinOperatingHours(r, start, end))
                .filter(r -> isOnAvailableDay(r, start))
                .filter(r -> bookingRepository.isTimeSlotAvailable(r.getId(), start, end))
                .toList();
    }

    /**
     * Get calendar view of bookings for a resource.
     */
    public Map<LocalDate, List<Booking>> getCalendarView(UUID resourceId, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, List<Booking>> calendar = new LinkedHashMap<>();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Booking> bookings = bookingRepository.findByResourceAndDateRange(resourceId, start, end);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate currentDate = date;
            List<Booking> dayBookings = bookings.stream()
                    .filter(b -> b.getStartTime().toLocalDate().equals(currentDate))
                    .toList();
            calendar.put(date, dayBookings);
        }

        return calendar;
    }

    /**
     * Check booking rules for a resource.
     */
    public ValidationResult validateBookingRequest(Resource resource, LocalDateTime start, LocalDateTime end, Long userId) {
        List<String> errors = new ArrayList<>();

        // Check minimum duration
        long durationMinutes = java.time.Duration.between(start, end).toMinutes();
        if (resource.getMinBookingMinutes() != null && durationMinutes < resource.getMinBookingMinutes()) {
            errors.add("Booking duration is less than minimum allowed: " + resource.getMinBookingMinutes() + " minutes");
        }

        // Check maximum duration
        if (resource.getMaxBookingMinutes() != null && durationMinutes > resource.getMaxBookingMinutes()) {
            errors.add("Booking duration exceeds maximum allowed: " + resource.getMaxBookingMinutes() + " minutes");
        }

        // Check advance booking limit
        if (resource.getAdvanceBookingDays() != null) {
            LocalDateTime maxAdvanceDate = LocalDateTime.now().plusDays(resource.getAdvanceBookingDays());
            if (start.isAfter(maxAdvanceDate)) {
                errors.add("Cannot book more than " + resource.getAdvanceBookingDays() + " days in advance");
            }
        }

        // Check if booking is in the past
        if (start.isBefore(LocalDateTime.now())) {
            errors.add("Cannot create booking in the past");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private boolean isWithinOperatingHours(Resource resource, LocalDateTime start, LocalDateTime end) {
        if (resource.getAvailableFrom() == null || resource.getAvailableUntil() == null) {
            return true;
        }

        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        return !startTime.isBefore(resource.getAvailableFrom()) &&
               !endTime.isAfter(resource.getAvailableUntil());
    }

    private boolean isOnAvailableDay(Resource resource, LocalDateTime dateTime) {
        if (resource.getAvailableDays() == null || resource.getAvailableDays().isEmpty()) {
            return true;
        }
        return resource.getAvailableDays().contains(dateTime.getDayOfWeek());
    }

    public record TimeSlot(LocalDateTime start, LocalDateTime end) {}

    public record ValidationResult(boolean valid, List<String> errors) {}
}
