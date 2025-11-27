package com.freelms.event.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a live learning event (webinar, workshop, training).
 */
@Entity
@Table(name = "events")
@EntityListeners(AuditingEntityListener.class)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    // Time
    @Column(name = "start_time", nullable = false)
    private ZonedDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private ZonedDateTime endTime;

    @Column(length = 50)
    private String timezone = "UTC";

    // Location
    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 50)
    private LocationType locationType;

    @Column(name = "physical_location", length = 500)
    private String physicalLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_platform", length = 50)
    private MeetingPlatform meetingPlatform;

    @Column(name = "meeting_url", length = 1000)
    private String meetingUrl;

    @Column(name = "meeting_id", length = 100)
    private String meetingId;

    @Column(name = "meeting_password", length = 100)
    private String meetingPassword;

    // Settings
    @Column(name = "max_attendees")
    private Integer maxAttendees;

    @Column(name = "registration_required")
    private Boolean registrationRequired = true;

    @Column(name = "registration_deadline")
    private ZonedDateTime registrationDeadline;

    @Column(name = "allow_waitlist")
    private Boolean allowWaitlist = true;

    @Column(name = "is_recorded")
    private Boolean isRecorded = false;

    @Column(name = "recording_url", length = 1000)
    private String recordingUrl;

    // Relations
    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "series_id")
    private UUID seriesId;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private EventStatus status = EventStatus.SCHEDULED;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventRegistration> registrations = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum EventType {
        WEBINAR,
        WORKSHOP,
        TRAINING,
        CONSULTATION,
        MEETING,
        CONFERENCE
    }

    public enum LocationType {
        ONLINE,
        OFFLINE,
        HYBRID
    }

    public enum MeetingPlatform {
        ZOOM,
        TEAMS,
        GOOGLE_MEET,
        BIGBLUEBUTTON,
        JITSI,
        CUSTOM
    }

    public enum EventStatus {
        DRAFT,
        SCHEDULED,
        LIVE,
        COMPLETED,
        CANCELLED
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public String getPhysicalLocation() {
        return physicalLocation;
    }

    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    public MeetingPlatform getMeetingPlatform() {
        return meetingPlatform;
    }

    public void setMeetingPlatform(MeetingPlatform meetingPlatform) {
        this.meetingPlatform = meetingPlatform;
    }

    public String getMeetingUrl() {
        return meetingUrl;
    }

    public void setMeetingUrl(String meetingUrl) {
        this.meetingUrl = meetingUrl;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getMeetingPassword() {
        return meetingPassword;
    }

    public void setMeetingPassword(String meetingPassword) {
        this.meetingPassword = meetingPassword;
    }

    public Integer getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(Integer maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public Boolean getRegistrationRequired() {
        return registrationRequired;
    }

    public void setRegistrationRequired(Boolean registrationRequired) {
        this.registrationRequired = registrationRequired;
    }

    public ZonedDateTime getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(ZonedDateTime registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public Boolean getAllowWaitlist() {
        return allowWaitlist;
    }

    public void setAllowWaitlist(Boolean allowWaitlist) {
        this.allowWaitlist = allowWaitlist;
    }

    public Boolean getIsRecorded() {
        return isRecorded;
    }

    public void setIsRecorded(Boolean isRecorded) {
        this.isRecorded = isRecorded;
    }

    public String getRecordingUrl() {
        return recordingUrl;
    }

    public void setRecordingUrl(String recordingUrl) {
        this.recordingUrl = recordingUrl;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public UUID getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(UUID seriesId) {
        this.seriesId = seriesId;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public List<EventRegistration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<EventRegistration> registrations) {
        this.registrations = registrations;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
