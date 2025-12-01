package com.freelms.lms.enrollment.entity;

import com.freelms.lms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "quiz_user_answers", indexes = {
        @Index(name = "idx_quiz_user_answers_attempt", columnList = "attempt_id"),
        @Index(name = "idx_quiz_user_answers_question", columnList = "question_id")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "selected_answer_ids")
    private String selectedAnswerIds;

    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer;

    @Column(name = "is_correct")
    @Builder.Default
    private boolean isCorrect = false;

    @Column(name = "points_earned")
    @Builder.Default
    private Integer pointsEarned = 0;
}
