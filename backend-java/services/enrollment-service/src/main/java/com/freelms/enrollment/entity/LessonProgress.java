package com.freelms.enrollment.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_progress", indexes = {
        @Index(name = "idx_lesson_progress_enrollment", columnList = "enrollment_id"),
        @Index(name = "idx_lesson_progress_lesson", columnList = "lesson_id"),
        @Index(name = "idx_lesson_progress_enrollment_lesson", columnList = "enrollment_id, lesson_id", unique = true)
})
@Getter
@Setter
@Builder
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

    @Column(name = "progress_percent")
    @Builder.Default
    private Integer progressPercent = 0;

    @Column(name = "video_position_seconds")
    @Builder.Default
    private Integer videoPositionSeconds = 0;

    @Column(name = "time_spent_seconds")
    @Builder.Default
    private Integer timeSpentSeconds = 0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public void markCompleted() {
        this.isCompleted = true;
        this.progressPercent = 100;
        this.completedAt = LocalDateTime.now();
    }

    public void updateVideoProgress(int positionSeconds, int videoDuration) {
        this.videoPositionSeconds = positionSeconds;
        if (videoDuration > 0) {
            this.progressPercent = Math.min(100, (positionSeconds * 100) / videoDuration);
            if (this.progressPercent >= 90) {
                markCompleted();
            }
        }
    }
}
