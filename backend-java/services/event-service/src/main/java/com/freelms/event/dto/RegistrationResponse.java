package com.freelms.event.dto;

import java.time.ZonedDateTime;

/**
 * DTO for registration responses.
 */
public class RegistrationResponse {

    private Long registrationId;
    private Long eventId;
    private String eventTitle;
    private Long userId;
    private String registrationStatus;
    private Integer waitlistPosition;
    private ZonedDateTime registeredAt;
    private String joinUrl;
    private String message;

    // Getters and Setters
    public Long getRegistrationId() { return registrationId; }
    public void setRegistrationId(Long registrationId) { this.registrationId = registrationId; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRegistrationStatus() { return registrationStatus; }
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }
    public Integer getWaitlistPosition() { return waitlistPosition; }
    public void setWaitlistPosition(Integer waitlistPosition) { this.waitlistPosition = waitlistPosition; }
    public ZonedDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(ZonedDateTime registeredAt) { this.registeredAt = registeredAt; }
    public String getJoinUrl() { return joinUrl; }
    public void setJoinUrl(String joinUrl) { this.joinUrl = joinUrl; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
