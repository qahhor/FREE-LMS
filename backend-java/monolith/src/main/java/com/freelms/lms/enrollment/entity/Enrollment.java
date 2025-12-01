package com.freelms.lms.enrollment.entity;

import com.freelms.lms.common.entity.BaseEntity;
import com.freelms.lms.common.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enrollments", indexes = {
        @Index(name = "idx_enrollments_user", columnList = "user_id"),
        @Index(name = "idx_enrollments_course", columnList = "course_id"),
        @Index(name = "idx_enrollments_user_course", columnList = "user_id, course_id", unique = true),
        @Index(name = "idx_enrollments_status", columnList = "status")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private Integer progress = 0;

    @Column(name = "enrolled_at", nullable = false)
    @Builder.Default
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "current_lesson_id")
    private Long currentLessonId;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LessonProgress> lessonProgresses = new ArrayList<>();

    @OneToOne(mappedBy = "enrollment", cascade = CascadeType.ALL)
    private Certificate certificate;

    public void updateProgress(int newProgress) {
        this.progress = Math.min(100, Math.max(0, newProgress));
        this.lastAccessedAt = LocalDateTime.now();
        if (this.progress >= 100 && this.status == EnrollmentStatus.ACTIVE) {
            this.complete();
        }
    }

    public void complete() {
        this.status = EnrollmentStatus.COMPLETED;
        this.progress = 100;
        this.completedAt = LocalDateTime.now();
    }

    public void drop() {
        this.status = EnrollmentStatus.DROPPED;
    }

    public void suspend() {
        this.status = EnrollmentStatus.SUSPENDED;
    }

    public void reactivate() {
        this.status = EnrollmentStatus.ACTIVE;
    }
}
