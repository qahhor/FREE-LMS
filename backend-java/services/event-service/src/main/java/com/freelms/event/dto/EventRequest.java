package com.freelms.event.dto;

import com.freelms.event.entity.Event;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO for creating/updating events.
 */
public class EventRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    @NotNull(message = "Event type is required")
    private Event.EventType eventType;

    @NotNull(message = "Location type is required")
    private Event.LocationType locationType;

    private String physicalLocation;

    private Event.MeetingPlatform meetingPlatform;

    private String meetingUrl;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private ZonedDateTime startTime;

    @NotNull(message = "End time is required")
    private ZonedDateTime endTime;

    private String timezone;

    private Integer maxAttendees;

    private boolean registrationRequired = true;

    private boolean allowWaitlist = true;

    private ZonedDateTime registrationDeadline;

    private Long courseId;

    private List<Long> coHostIds;

    private boolean recordingEnabled = false;

    private boolean chatEnabled = true;

    private boolean qnaEnabled = true;

    private String recurringPattern; // NONE, DAILY, WEEKLY, MONTHLY

    private Integer reminderMinutesBefore = 30;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Event.EventType getEventType() { return eventType; }
    public void setEventType(Event.EventType eventType) { this.eventType = eventType; }
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
    public boolean isRegistrationRequired() { return registrationRequired; }
    public void setRegistrationRequired(boolean registrationRequired) { this.registrationRequired = registrationRequired; }
    public boolean isAllowWaitlist() { return allowWaitlist; }
    public void setAllowWaitlist(boolean allowWaitlist) { this.allowWaitlist = allowWaitlist; }
    public ZonedDateTime getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(ZonedDateTime registrationDeadline) { this.registrationDeadline = registrationDeadline; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public List<Long> getCoHostIds() { return coHostIds; }
    public void setCoHostIds(List<Long> coHostIds) { this.coHostIds = coHostIds; }
    public boolean isRecordingEnabled() { return recordingEnabled; }
    public void setRecordingEnabled(boolean recordingEnabled) { this.recordingEnabled = recordingEnabled; }
    public boolean isChatEnabled() { return chatEnabled; }
    public void setChatEnabled(boolean chatEnabled) { this.chatEnabled = chatEnabled; }
    public boolean isQnaEnabled() { return qnaEnabled; }
    public void setQnaEnabled(boolean qnaEnabled) { this.qnaEnabled = qnaEnabled; }
    public String getRecurringPattern() { return recurringPattern; }
    public void setRecurringPattern(String recurringPattern) { this.recurringPattern = recurringPattern; }
    public Integer getReminderMinutesBefore() { return reminderMinutesBefore; }
    public void setReminderMinutesBefore(Integer reminderMinutesBefore) { this.reminderMinutesBefore = reminderMinutesBefore; }
}
