package com.freelms.event.controller;

import com.freelms.event.dto.*;
import com.freelms.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for event operations.
 */
@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Webinar and live event management")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Create a new event.
     */
    @PostMapping
    @Operation(summary = "Create event", description = "Create a new webinar or live event")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(eventService.createEvent(request, userId));
    }

    /**
     * Get all events with pagination.
     */
    @GetMapping
    @Operation(summary = "List events", description = "Get all events with filtering and pagination")
    public ResponseEntity<Page<EventResponse>> getEvents(
            @Parameter(description = "Event type filter") @RequestParam(required = false) String type,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Organization ID") @RequestParam(required = false) Long organizationId,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.getEvents(type, status, organizationId, pageable));
    }

    /**
     * Get event by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get event", description = "Get event details by ID")
    public ResponseEntity<EventResponse> getEvent(
            @Parameter(description = "Event ID") @PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    /**
     * Update an event.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update event", description = "Update event details")
    public ResponseEntity<EventResponse> updateEvent(
            @Parameter(description = "Event ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    /**
     * Cancel an event.
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel event", description = "Cancel an event")
    public ResponseEntity<EventResponse> cancelEvent(
            @Parameter(description = "Event ID") @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> reason) {
        return ResponseEntity.ok(eventService.cancelEvent(id, reason != null ? reason.get("reason") : null));
    }

    /**
     * Register for an event.
     */
    @PostMapping("/{id}/register")
    @Operation(summary = "Register", description = "Register for an event")
    public ResponseEntity<RegistrationResponse> register(
            @Parameter(description = "Event ID") @PathVariable UUID id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(eventService.register(id, userId));
    }

    /**
     * Cancel registration.
     */
    @DeleteMapping("/{id}/register")
    @Operation(summary = "Cancel registration", description = "Cancel event registration")
    public ResponseEntity<Void> cancelRegistration(
            @Parameter(description = "Event ID") @PathVariable UUID id,
            @RequestHeader("X-User-Id") Long userId) {
        eventService.cancelRegistration(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get attendees list.
     */
    @GetMapping("/{id}/attendees")
    @Operation(summary = "Get attendees", description = "Get list of registered attendees")
    public ResponseEntity<Page<AttendeeResponse>> getAttendees(
            @Parameter(description = "Event ID") @PathVariable UUID id,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.getAttendees(id, pageable));
    }

    /**
     * Get meeting join URL.
     */
    @GetMapping("/{id}/meeting/join")
    @Operation(summary = "Get join URL", description = "Get meeting join URL for registered user")
    public ResponseEntity<Map<String, String>> getJoinUrl(
            @Parameter(description = "Event ID") @PathVariable UUID id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(eventService.getJoinUrl(id, userId));
    }

    /**
     * Get user's calendar.
     */
    @GetMapping("/calendar")
    @Operation(summary = "Get calendar", description = "Get user's event calendar")
    public ResponseEntity<CalendarResponse> getCalendar(
            @Parameter(description = "Start date") @RequestParam(required = false) String from,
            @Parameter(description = "End date") @RequestParam(required = false) String to,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(eventService.getCalendar(userId, from, to));
    }

    /**
     * Export calendar as iCal.
     */
    @GetMapping("/calendar/ical")
    @Operation(summary = "Export iCal", description = "Export calendar as iCal format")
    public ResponseEntity<String> exportIcal(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok()
                .header("Content-Type", "text/calendar")
                .header("Content-Disposition", "attachment; filename=calendar.ics")
                .body(eventService.exportIcal(userId));
    }
}
