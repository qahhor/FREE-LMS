package com.freelms.compliance.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_compliance",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "training_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCompliance extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id", nullable = false)
    private ComplianceTraining training;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ComplianceStatus status = ComplianceStatus.PENDING;

    @Column(name = "assigned_date", nullable = false)
    @Builder.Default
    private LocalDate assignedDate = LocalDate.now();

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "reminder_sent_count")
    @Builder.Default
    private Integer reminderSentCount = 0;

    @Column(name = "last_reminder_at")
    private LocalDateTime lastReminderAt;

    @Column(name = "escalation_level")
    @Builder.Default
    private Integer escalationLevel = 0;

    @Column(name = "completion_score")
    private Integer completionScore;

    @Column(name = "certificate_id")
    private Long certificateId;

    public enum ComplianceStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        OVERDUE,
        EXPIRED,
        EXEMPTED
    }
}
