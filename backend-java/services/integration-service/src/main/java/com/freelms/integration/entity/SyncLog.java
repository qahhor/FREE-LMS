package com.freelms.integration.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sync_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_id", nullable = false)
    private Integration integration;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_type", nullable = false)
    private SyncType syncType;

    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SyncStatus status = SyncStatus.RUNNING;

    @Column(name = "records_processed")
    @Builder.Default
    private Integer recordsProcessed = 0;

    @Column(name = "records_created")
    @Builder.Default
    private Integer recordsCreated = 0;

    @Column(name = "records_updated")
    @Builder.Default
    private Integer recordsUpdated = 0;

    @Column(name = "records_failed")
    @Builder.Default
    private Integer recordsFailed = 0;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails; // JSON array

    @Column(name = "triggered_by")
    private String triggeredBy; // "manual", "scheduled", "webhook"

    public enum SyncType {
        FULL,
        INCREMENTAL,
        USERS_ONLY,
        DEPARTMENTS_ONLY
    }

    public enum SyncStatus {
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
