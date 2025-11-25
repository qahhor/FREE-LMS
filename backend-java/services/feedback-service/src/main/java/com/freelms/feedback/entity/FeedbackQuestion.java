package com.freelms.feedback.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feedback_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private FeedbackTemplate template;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    @Column(name = "category")
    private String category;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = true;

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(name = "rating_scale_min")
    private Integer ratingScaleMin;

    @Column(name = "rating_scale_max")
    private Integer ratingScaleMax;

    @Column(name = "options", columnDefinition = "TEXT")
    private String options; // JSON array for multiple choice

    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "competency_id")
    private Long competencyId;

    public enum QuestionType {
        RATING,
        TEXT,
        MULTIPLE_CHOICE,
        SCALE,
        YES_NO
    }
}
