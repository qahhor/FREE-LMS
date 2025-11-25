package com.freelms.reporting.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "query_config", columnDefinition = "TEXT")
    private String queryConfig; // JSON

    @Column(name = "filters", columnDefinition = "TEXT")
    private String filters; // JSON

    @Column(name = "columns", columnDefinition = "TEXT")
    private String columns; // JSON array

    @Column(name = "is_scheduled")
    @Builder.Default
    private Boolean isScheduled = false;

    @Column(name = "schedule_cron")
    private String scheduleCron;

    @Column(name = "recipients", columnDefinition = "TEXT")
    private String recipients; // JSON array of emails

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    public enum ReportType {
        COURSE_COMPLETION,
        USER_PROGRESS,
        COMPLIANCE_STATUS,
        SKILL_GAP,
        TRAINING_ROI,
        ACTIVITY_HEATMAP,
        LEADERBOARD,
        CUSTOM
    }
}
