package com.freelms.organization.entity;

import com.freelms.common.entity.BaseEntity;
import com.freelms.common.enums.WebinarPlatform;
import com.freelms.common.enums.WebinarStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "webinars", indexes = {
    @Index(name = "idx_webinars_instructor", columnList = "instructor_id"),
    @Index(name = "idx_webinars_course", columnList = "course_id"),
    @Index(name = "idx_webinars_status", columnList = "status"),
    @Index(name = "idx_webinars_scheduled", columnList = "scheduled_at")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Webinar extends BaseEntity {
    @Column(nullable = false, length = 255) private String title;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "instructor_id", nullable = false) private Long instructorId;
    @Column(name = "course_id") private Long courseId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private WebinarStatus status = WebinarStatus.SCHEDULED;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private WebinarPlatform platform = WebinarPlatform.JITSI;
    @Column(name = "scheduled_at", nullable = false) private LocalDateTime scheduledAt;
    @Column(name = "duration_minutes") @Builder.Default private Integer durationMinutes = 60;
    @Column(name = "meeting_url", length = 500) private String meetingUrl;
    @Column(name = "meeting_id", length = 100) private String meetingId;
    @Column(name = "max_participants") @Builder.Default private Integer maxParticipants = 100;
    @Column(name = "recording_url", length = 500) private String recordingUrl;

    public void start() { this.status = WebinarStatus.LIVE; }
    public void end() { this.status = WebinarStatus.COMPLETED; }
    public void cancel() { this.status = WebinarStatus.CANCELLED; }
}
