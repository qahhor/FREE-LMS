package com.freelms.learningpath.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_path_enrollments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"learning_path_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathEnrollment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_path_id", nullable = false)
    private LearningPath learningPath;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "enrolled_at", nullable = false)
    @Builder.Default
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "progress_percentage")
    @Builder.Default
    private Integer progressPercentage = 0;

    @Column(name = "current_item_index")
    @Builder.Default
    private Integer currentItemIndex = 0;

    @Column(name = "assigned_by")
    private Long assignedBy; // Manager who assigned

    @Column(name = "assignment_note")
    private String assignmentNote;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LearningPathItemProgress> itemProgress = new ArrayList<>();

    public enum EnrollmentStatus {
        ACTIVE,
        COMPLETED,
        PAUSED,
        EXPIRED,
        CANCELLED
    }
}
