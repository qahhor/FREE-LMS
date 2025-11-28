package com.freelms.event.service;

import com.freelms.event.dto.EventRequest;
import com.freelms.event.dto.EventResponse;
import com.freelms.event.dto.RegistrationResponse;
import com.freelms.event.entity.Event;
import com.freelms.event.entity.EventRegistration;
import com.freelms.event.repository.EventRegistrationRepository;
import com.freelms.event.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for event management operations.
 */
@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final MeetingIntegrationService meetingService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventService(
            EventRepository eventRepository,
            EventRegistrationRepository registrationRepository,
            MeetingIntegrationService meetingService,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.meetingService = meetingService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Create a new event.
     */
    @Transactional
    public EventResponse createEvent(EventRequest request, Long organizerId, Long organizationId) {
        log.info("Creating event: {} for organizer: {}", request.getTitle(), organizerId);

        Event event = new Event();
        mapRequestToEvent(request, event);
        event.setOrganizerId(organizerId);
        event.setOrganizationId(organizationId);
        event.setStatus(Event.EventStatus.SCHEDULED);

        // Create external meeting if needed
        if (request.getLocationType() == Event.LocationType.ONLINE ||
            request.getLocationType() == Event.LocationType.HYBRID) {
            if (request.getMeetingPlatform() != null && request.getMeetingUrl() == null) {
                MeetingIntegrationService.MeetingInfo meetingInfo = meetingService.createMeeting(
                        request.getMeetingPlatform(),
                        request.getTitle(),
                        request.getStartTime(),
                        request.getEndTime()
                );
                event.setMeetingUrl(meetingInfo.getJoinUrl());
                event.setExternalMeetingId(meetingInfo.getMeetingId());
                event.setHostMeetingUrl(meetingInfo.getHostUrl());
            }
        }

        Event saved = eventRepository.save(event);

        publishEvent("EVENT_CREATED", saved);
        log.info("Event created with ID: {}", saved.getId());

        return mapToResponse(saved, null);
    }

    /**
     * Get event by ID.
     */
    public EventResponse getEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        return mapToResponse(event, userId);
    }

    /**
     * Update event.
     */
    @Transactional
    public EventResponse updateEvent(Long eventId, EventRequest request, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        if (!event.getOrganizerId().equals(userId)) {
            throw new RuntimeException("Only organizer can update the event");
        }

        mapRequestToEvent(request, event);
        Event saved = eventRepository.save(event);

        publishEvent("EVENT_UPDATED", saved);

        return mapToResponse(saved, userId);
    }

    /**
     * Cancel event.
     */
    @Transactional
    public EventResponse cancelEvent(Long eventId, Long userId, String reason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        if (!event.getOrganizerId().equals(userId)) {
            throw new RuntimeException("Only organizer can cancel the event");
        }

        event.setStatus(Event.EventStatus.CANCELLED);
        event.setCancellationReason(reason);
        event.setCancelledAt(ZonedDateTime.now());
        Event saved = eventRepository.save(event);

        // Cancel external meeting if exists
        if (event.getExternalMeetingId() != null) {
            meetingService.cancelMeeting(event.getMeetingPlatform(), event.getExternalMeetingId());
        }

        // Notify all registered attendees
        publishEvent("EVENT_CANCELLED", saved);

        return mapToResponse(saved, userId);
    }

    /**
     * Register user for event.
     */
    @Transactional
    public RegistrationResponse registerForEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        // Check if already registered
        if (registrationRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new RuntimeException("User already registered for this event");
        }

        // Check registration deadline
        if (event.getRegistrationDeadline() != null &&
            ZonedDateTime.now().isAfter(event.getRegistrationDeadline())) {
            throw new RuntimeException("Registration deadline has passed");
        }

        // Check capacity
        Long currentCount = registrationRepository.countRegisteredByEventId(eventId);
        boolean isWaitlisted = event.getMaxAttendees() != null && currentCount >= event.getMaxAttendees();

        if (isWaitlisted && !event.isAllowWaitlist()) {
            throw new RuntimeException("Event is full and waitlist is not allowed");
        }

        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);
        registration.setUserId(userId);
        registration.setStatus(isWaitlisted ?
                EventRegistration.RegistrationStatus.WAITLISTED :
                EventRegistration.RegistrationStatus.REGISTERED);
        registration.setRegisteredAt(ZonedDateTime.now());

        EventRegistration saved = registrationRepository.save(registration);

        RegistrationResponse response = new RegistrationResponse();
        response.setRegistrationId(saved.getId());
        response.setEventId(eventId);
        response.setEventTitle(event.getTitle());
        response.setUserId(userId);
        response.setRegistrationStatus(saved.getStatus().name());
        response.setRegisteredAt(saved.getRegisteredAt());

        if (isWaitlisted) {
            Long waitlistPosition = registrationRepository.countWaitlistedByEventId(eventId);
            response.setWaitlistPosition(waitlistPosition.intValue());
            response.setMessage("You have been added to the waitlist");
        } else {
            response.setJoinUrl(event.getMeetingUrl());
            response.setMessage("Registration successful");
        }

        publishEvent("USER_REGISTERED", Map.of("eventId", eventId, "userId", userId, "status", saved.getStatus().name()));

        return response;
    }

    /**
     * Cancel registration.
     */
    @Transactional
    public void cancelRegistration(Long eventId, Long userId) {
        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        boolean wasRegistered = registration.getStatus() == EventRegistration.RegistrationStatus.REGISTERED;
        registrationRepository.delete(registration);

        // If someone was registered (not waitlisted), promote first from waitlist
        if (wasRegistered) {
            promoteFromWaitlist(eventId);
        }

        publishEvent("USER_CANCELLED_REGISTRATION", Map.of("eventId", eventId, "userId", userId));
    }

    /**
     * Get attendees for event.
     */
    public Page<EventRegistration> getAttendees(Long eventId, Pageable pageable) {
        return registrationRepository.findByEventId(eventId, pageable);
    }

    /**
     * Get events for calendar view.
     */
    public List<Event> getCalendarEvents(Long userId, ZonedDateTime start, ZonedDateTime end) {
        return eventRepository.findScheduledEventsBetween(start, end);
    }

    /**
     * Get user's registered events.
     */
    public Page<Event> getUserEvents(Long userId, Pageable pageable) {
        return eventRepository.findUserEvents(userId, pageable);
    }

    /**
     * Get join URL for meeting.
     */
    public Map<String, String> getJoinUrl(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        // Check if user is registered
        Optional<EventRegistration> registration = registrationRepository.findByEventIdAndUserId(eventId, userId);
        if (registration.isEmpty() || registration.get().getStatus() != EventRegistration.RegistrationStatus.REGISTERED) {
            if (event.isRegistrationRequired()) {
                throw new RuntimeException("User is not registered for this event");
            }
        }

        // Check if event is about to start (within 15 minutes) or ongoing
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime joinableFrom = event.getStartTime().minusMinutes(15);
        if (now.isBefore(joinableFrom)) {
            throw new RuntimeException("Meeting is not yet available for joining");
        }

        if (event.getOrganizerId().equals(userId)) {
            return Map.of("url", event.getHostMeetingUrl() != null ? event.getHostMeetingUrl() : event.getMeetingUrl());
        }

        return Map.of("url", event.getMeetingUrl());
    }

    /**
     * Generate iCal export.
     */
    public String exportToICal(Long userId) {
        List<Event> events = eventRepository.findUserEvents(userId, Pageable.unpaged()).getContent();

        StringBuilder ical = new StringBuilder();
        ical.append("BEGIN:VCALENDAR\n");
        ical.append("VERSION:2.0\n");
        ical.append("PRODID:-//FREE LMS//Events//EN\n");

        for (Event event : events) {
            ical.append("BEGIN:VEVENT\n");
            ical.append("UID:").append(event.getId()).append("@freelms.com\n");
            ical.append("DTSTART:").append(formatICalDate(event.getStartTime())).append("\n");
            ical.append("DTEND:").append(formatICalDate(event.getEndTime())).append("\n");
            ical.append("SUMMARY:").append(event.getTitle()).append("\n");
            if (event.getDescription() != null) {
                ical.append("DESCRIPTION:").append(event.getDescription().replace("\n", "\\n")).append("\n");
            }
            if (event.getMeetingUrl() != null) {
                ical.append("URL:").append(event.getMeetingUrl()).append("\n");
            }
            if (event.getPhysicalLocation() != null) {
                ical.append("LOCATION:").append(event.getPhysicalLocation()).append("\n");
            }
            ical.append("END:VEVENT\n");
        }

        ical.append("END:VCALENDAR\n");
        return ical.toString();
    }

    // Helper methods

    private void promoteFromWaitlist(Long eventId) {
        List<EventRegistration> waitlist = registrationRepository.findWaitlistByEventIdOrderedByTime(eventId);
        if (!waitlist.isEmpty()) {
            EventRegistration first = waitlist.get(0);
            first.setStatus(EventRegistration.RegistrationStatus.REGISTERED);
            registrationRepository.save(first);

            publishEvent("USER_PROMOTED_FROM_WAITLIST",
                    Map.of("eventId", eventId, "userId", first.getUserId()));
        }
    }

    private void mapRequestToEvent(EventRequest request, Event event) {
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventType());
        event.setLocationType(request.getLocationType());
        event.setPhysicalLocation(request.getPhysicalLocation());
        event.setMeetingPlatform(request.getMeetingPlatform());
        event.setMeetingUrl(request.getMeetingUrl());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setTimezone(request.getTimezone());
        event.setMaxAttendees(request.getMaxAttendees());
        event.setRegistrationRequired(request.isRegistrationRequired());
        event.setAllowWaitlist(request.isAllowWaitlist());
        event.setRegistrationDeadline(request.getRegistrationDeadline());
        event.setCourseId(request.getCourseId());
        event.setRecordingEnabled(request.isRecordingEnabled());
        event.setChatEnabled(request.isChatEnabled());
        event.setQnaEnabled(request.isQnaEnabled());
        event.setRecurringPattern(request.getRecurringPattern());
        event.setReminderMinutesBefore(request.getReminderMinutesBefore());
    }

    private EventResponse mapToResponse(Event event, Long userId) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setEventType(event.getEventType());
        response.setStatus(event.getStatus());
        response.setLocationType(event.getLocationType());
        response.setPhysicalLocation(event.getPhysicalLocation());
        response.setMeetingPlatform(event.getMeetingPlatform());
        response.setStartTime(event.getStartTime());
        response.setEndTime(event.getEndTime());
        response.setTimezone(event.getTimezone());
        response.setMaxAttendees(event.getMaxAttendees());
        response.setCurrentAttendees(registrationRepository.countRegisteredByEventId(event.getId()).intValue());
        response.setWaitlistCount(registrationRepository.countWaitlistedByEventId(event.getId()).intValue());
        response.setRegistrationRequired(event.isRegistrationRequired());
        response.setAllowWaitlist(event.isAllowWaitlist());
        response.setRegistrationDeadline(event.getRegistrationDeadline());
        response.setOrganizerId(event.getOrganizerId());
        response.setCourseId(event.getCourseId());
        response.setOrganizationId(event.getOrganizationId());
        response.setRecordingEnabled(event.isRecordingEnabled());
        response.setChatEnabled(event.isChatEnabled());
        response.setQnaEnabled(event.isQnaEnabled());
        response.setRecurringPattern(event.getRecurringPattern());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());

        // User-specific info
        if (userId != null) {
            Optional<EventRegistration> registration = registrationRepository.findByEventIdAndUserId(event.getId(), userId);
            if (registration.isPresent()) {
                response.setUserRegistrationStatus(registration.get().getStatus().name());
            }
            response.setCanRegister(canUserRegister(event, userId));
            response.setCanJoin(canUserJoin(event, userId));
        }

        return response;
    }

    private boolean canUserRegister(Event event, Long userId) {
        if (event.getStatus() != Event.EventStatus.SCHEDULED) return false;
        if (registrationRepository.existsByEventIdAndUserId(event.getId(), userId)) return false;
        if (event.getRegistrationDeadline() != null &&
            ZonedDateTime.now().isAfter(event.getRegistrationDeadline())) return false;

        Long currentCount = registrationRepository.countRegisteredByEventId(event.getId());
        return event.getMaxAttendees() == null || currentCount < event.getMaxAttendees() || event.isAllowWaitlist();
    }

    private boolean canUserJoin(Event event, Long userId) {
        if (event.getStatus() != Event.EventStatus.SCHEDULED &&
            event.getStatus() != Event.EventStatus.IN_PROGRESS) return false;

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime joinableFrom = event.getStartTime().minusMinutes(15);
        if (now.isBefore(joinableFrom) || now.isAfter(event.getEndTime())) return false;

        if (event.getOrganizerId().equals(userId)) return true;

        if (event.isRegistrationRequired()) {
            Optional<EventRegistration> registration = registrationRepository.findByEventIdAndUserId(event.getId(), userId);
            return registration.isPresent() &&
                   registration.get().getStatus() == EventRegistration.RegistrationStatus.REGISTERED;
        }

        return true;
    }

    private String formatICalDate(ZonedDateTime dateTime) {
        return dateTime.toInstant().toString().replace("-", "").replace(":", "").substring(0, 15) + "Z";
    }

    private void publishEvent(String eventType, Object data) {
        try {
            kafkaTemplate.send("event-events", Map.of("eventType", eventType, "data", data));
        } catch (Exception e) {
            log.error("Failed to publish event: {}", e.getMessage());
        }
    }
}
