package com.freelms.lms.enrollment.entity;

import com.freelms.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempts", indexes = {
        @Index(name = "idx_quiz_attempts_enrollment", columnList = "enrollment_id"),
        @Index(name = "idx_quiz_attempts_quiz", columnList = "quiz_id"),
        @Index(name = "idx_quiz_attempts_user", columnList = "user_id")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttempt extends BaseEntity {

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "score")
    private Integer score;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "percentage")
    private Integer percentage;

    @Column(name = "is_passed")
    @Builder.Default
    private boolean isPassed = false;

    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "time_spent_seconds")
    @Builder.Default
    private Integer timeSpentSeconds = 0;

    @Column(name = "attempt_number")
    @Builder.Default
    private Integer attemptNumber = 1;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuizAnswer> answers = new ArrayList<>();

    public void complete(int score, int maxScore, int passingScore) {
        this.score = score;
        this.maxScore = maxScore;
        this.percentage = maxScore > 0 ? (score * 100) / maxScore : 0;
        this.isPassed = this.percentage >= passingScore;
        this.completedAt = LocalDateTime.now();
    }
}
