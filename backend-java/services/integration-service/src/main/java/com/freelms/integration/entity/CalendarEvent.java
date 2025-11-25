package com.freelms.integration.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEvent extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "external_event_id")
    private String externalEventId;

    @Column(name = "external_calendar_id")
    private String externalCalendarId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "meeting_link")
    private String meetingLink;

    @Column(name = "location")
    private String location;

    @Column(name = "source_type")
    private String sourceType; // "course", "webinar", "mentoring_session"

    @Column(name = "source_id")
    private Long sourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    public enum EventType {
        COURSE_SESSION,
        WEBINAR,
        MENTORING_SESSION,
        DEADLINE,
        REMINDER
    }

    public enum SyncStatus {
        PENDING,
        SYNCED,
        FAILED,
        DELETED
    }
}
