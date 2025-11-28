package com.freelms.booking.controller;

import com.freelms.booking.entity.Resource;
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
@RequestMapping("/api/v1/resources")
@Tag(name = "Resources", description = "Bookable resource management")
public class ResourceController {

    private final ResourceBookingService bookingService;
    private final AvailabilityService availabilityService;

    public ResourceController(ResourceBookingService bookingService, AvailabilityService availabilityService) {
        this.bookingService = bookingService;
        this.availabilityService = availabilityService;
    }

    @PostMapping
    @Operation(summary = "Create a new resource")
    public ResponseEntity<Resource> createResource(@RequestBody Resource resource) {
        return ResponseEntity.ok(bookingService.createResource(resource));
    }

    @GetMapping("/{resourceId}")
    @Operation(summary = "Get resource by ID")
    public ResponseEntity<Resource> getResource(@PathVariable UUID resourceId) {
        return ResponseEntity.ok(bookingService.getResource(resourceId));
    }

    @PutMapping("/{resourceId}")
    @Operation(summary = "Update resource")
    public ResponseEntity<Resource> updateResource(
            @PathVariable UUID resourceId,
            @RequestBody Resource updates) {
        return ResponseEntity.ok(bookingService.updateResource(resourceId, updates));
    }

    @PatchMapping("/{resourceId}/status")
    @Operation(summary = "Update resource status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID resourceId,
            @RequestParam Resource.ResourceStatus status) {
        bookingService.updateResourceStatus(resourceId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "List resources")
    public ResponseEntity<Page<Resource>> listResources(
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) Resource.ResourceType type,
            Pageable pageable) {
        return ResponseEntity.ok(bookingService.getResources(organizationId, type, pageable));
    }

    @GetMapping("/{resourceId}/availability")
    @Operation(summary = "Check resource availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable UUID resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        boolean available = availabilityService.isAvailable(resourceId, start, end);
        return ResponseEntity.ok(Map.of(
                "resourceId", resourceId,
                "start", start,
                "end", end,
                "available", available
        ));
    }

    @GetMapping("/{resourceId}/slots")
    @Operation(summary = "Get available time slots")
    public ResponseEntity<List<AvailabilityService.TimeSlot>> getAvailableSlots(
            @PathVariable UUID resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "60") int durationMinutes) {
        return ResponseEntity.ok(availabilityService.getAvailableSlots(resourceId, date, durationMinutes));
    }

    @GetMapping("/search/available")
    @Operation(summary = "Find available resources")
    public ResponseEntity<List<Resource>> findAvailableResources(
            @RequestParam Long organizationId,
            @RequestParam(required = false) Resource.ResourceType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) Integer minCapacity) {
        return ResponseEntity.ok(availabilityService.findAvailableResources(
                organizationId, type, start, end, minCapacity));
    }

    @GetMapping("/{resourceId}/stats")
    @Operation(summary = "Get resource statistics")
    public ResponseEntity<Map<String, Object>> getResourceStats(
            @PathVariable UUID resourceId,
            @RequestParam(defaultValue = "30") int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return ResponseEntity.ok(bookingService.getResourceStats(resourceId, since));
    }
}
