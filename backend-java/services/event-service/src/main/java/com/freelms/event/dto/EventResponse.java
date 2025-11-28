package com.freelms.event.dto;

import com.freelms.event.entity.Event;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO for event responses.
 */
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private Event.EventType eventType;
    private Event.EventStatus status;
    private Event.LocationType locationType;
    private String physicalLocation;
    private Event.MeetingPlatform meetingPlatform;
    private String meetingUrl;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private String timezone;
    private Integer maxAttendees;
    private Integer currentAttendees;
    private Integer waitlistCount;
    private boolean registrationRequired;
    private boolean allowWaitlist;
    private ZonedDateTime registrationDeadline;
    private Long organizerId;
    private String organizerName;
    private Long courseId;
    private String courseName;
    private Long organizationId;
    private List<CoHostInfo> coHosts;
    private boolean recordingEnabled;
    private boolean chatEnabled;
    private boolean qnaEnabled;
    private String recurringPattern;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    // Registration info for current user
    private String userRegistrationStatus;
    private boolean canRegister;
    private boolean canJoin;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Event.EventType getEventType() { return eventType; }
    public void setEventType(Event.EventType eventType) { this.eventType = eventType; }
    public Event.EventStatus getStatus() { return status; }
    public void setStatus(Event.EventStatus status) { this.status = status; }
    public Event.LocationType getLocationType() { return locationType; }
    public void setLocationType(Event.LocationType locationType) { this.locationType = locationType; }
    public String getPhysicalLocation() { return physicalLocation; }
    public void setPhysicalLocation(String physicalLocation) { this.physicalLocation = physicalLocation; }
    public Event.MeetingPlatform getMeetingPlatform() { return meetingPlatform; }
    public void setMeetingPlatform(Event.MeetingPlatform meetingPlatform) { this.meetingPlatform = meetingPlatform; }
    public String getMeetingUrl() { return meetingUrl; }
    public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }
    public ZonedDateTime getStartTime() { return startTime; }
    public void setStartTime(ZonedDateTime startTime) { this.startTime = startTime; }
    public ZonedDateTime getEndTime() { return endTime; }
    public void setEndTime(ZonedDateTime endTime) { this.endTime = endTime; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public Integer getMaxAttendees() { return maxAttendees; }
    public void setMaxAttendees(Integer maxAttendees) { this.maxAttendees = maxAttendees; }
    public Integer getCurrentAttendees() { return currentAttendees; }
    public void setCurrentAttendees(Integer currentAttendees) { this.currentAttendees = currentAttendees; }
    public Integer getWaitlistCount() { return waitlistCount; }
    public void setWaitlistCount(Integer waitlistCount) { this.waitlistCount = waitlistCount; }
    public boolean isRegistrationRequired() { return registrationRequired; }
    public void setRegistrationRequired(boolean registrationRequired) { this.registrationRequired = registrationRequired; }
    public boolean isAllowWaitlist() { return allowWaitlist; }
    public void setAllowWaitlist(boolean allowWaitlist) { this.allowWaitlist = allowWaitlist; }
    public ZonedDateTime getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(ZonedDateTime registrationDeadline) { this.registrationDeadline = registrationDeadline; }
    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }
    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    public List<CoHostInfo> getCoHosts() { return coHosts; }
    public void setCoHosts(List<CoHostInfo> coHosts) { this.coHosts = coHosts; }
    public boolean isRecordingEnabled() { return recordingEnabled; }
    public void setRecordingEnabled(boolean recordingEnabled) { this.recordingEnabled = recordingEnabled; }
    public boolean isChatEnabled() { return chatEnabled; }
    public void setChatEnabled(boolean chatEnabled) { this.chatEnabled = chatEnabled; }
    public boolean isQnaEnabled() { return qnaEnabled; }
    public void setQnaEnabled(boolean qnaEnabled) { this.qnaEnabled = qnaEnabled; }
    public String getRecurringPattern() { return recurringPattern; }
    public void setRecurringPattern(String recurringPattern) { this.recurringPattern = recurringPattern; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUserRegistrationStatus() { return userRegistrationStatus; }
    public void setUserRegistrationStatus(String userRegistrationStatus) { this.userRegistrationStatus = userRegistrationStatus; }
    public boolean isCanRegister() { return canRegister; }
    public void setCanRegister(boolean canRegister) { this.canRegister = canRegister; }
    public boolean isCanJoin() { return canJoin; }
    public void setCanJoin(boolean canJoin) { this.canJoin = canJoin; }

    public static class CoHostInfo {
        private Long userId;
        private String name;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
