package com.freelms.course.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_answers", indexes = {
        @Index(name = "idx_answers_question", columnList = "question_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswer extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "is_correct")
    @Builder.Default
    private boolean isCorrect = false;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;
}
