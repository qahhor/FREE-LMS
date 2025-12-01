package com.freelms.lms.enrollment.entity;

import com.freelms.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_progress", indexes = {
        @Index(name = "idx_lesson_progress_enrollment", columnList = "enrollment_id"),
        @Index(name = "idx_lesson_progress_lesson", columnList = "lesson_id"),
        @Index(name = "idx_lesson_progress_enrollment_lesson", columnList = "enrollment_id, lesson_id", unique = true)
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "is_completed")
    @Builder.Default
    private boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "time_spent_seconds")
    @Builder.Default
    private Integer timeSpentSeconds = 0;

    @Column(name = "video_position_seconds")
    @Builder.Default
    private Integer videoPositionSeconds = 0;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    public void markCompleted() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }

    public void updateTimeSpent(int seconds) {
        this.timeSpentSeconds += seconds;
        this.lastAccessedAt = LocalDateTime.now();
    }

    public void updateVideoPosition(int position) {
        this.videoPositionSeconds = position;
        this.lastAccessedAt = LocalDateTime.now();
    }
}
