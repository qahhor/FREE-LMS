package com.freelms.course.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes", indexes = {
        @Index(name = "idx_quizzes_lesson", columnList = "lesson_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quiz extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "passing_score")
    @Builder.Default
    private Integer passingScore = 70;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "max_attempts")
    @Builder.Default
    private Integer maxAttempts = 3;

    @Column(name = "shuffle_questions")
    @Builder.Default
    private boolean shuffleQuestions = false;

    @Column(name = "show_correct_answers")
    @Builder.Default
    private boolean showCorrectAnswers = true;

    @Column(name = "lesson_id")
    private Long lessonId;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<QuizQuestion> questions = new ArrayList<>();

    public int getTotalPoints() {
        return questions.stream()
                .mapToInt(QuizQuestion::getPoints)
                .sum();
    }
}
