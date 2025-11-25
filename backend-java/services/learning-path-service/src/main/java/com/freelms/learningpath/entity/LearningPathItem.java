package com.freelms.learningpath.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_path_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_path_id", nullable = false)
    private LearningPath learningPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "external_resource_url")
    private String externalResourceUrl;

    @Column(name = "external_resource_title")
    private String externalResourceTitle;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "is_optional")
    @Builder.Default
    private Boolean isOptional = false;

    @Column(name = "unlock_after_days")
    private Integer unlockAfterDays; // Days after enrollment to unlock

    @Column(name = "deadline_days")
    private Integer deadlineDays; // Days to complete after unlock

    @ManyToMany
    @JoinTable(
        name = "learning_path_item_prerequisites",
        joinColumns = @JoinColumn(name = "item_id"),
        inverseJoinColumns = @JoinColumn(name = "prerequisite_item_id")
    )
    @Builder.Default
    private List<LearningPathItem> prerequisites = new ArrayList<>();

    @Column(name = "min_score_to_pass")
    private Integer minScoreToPass; // For quizzes

    @Column(name = "points_reward")
    @Builder.Default
    private Integer pointsReward = 0;

    public enum ItemType {
        COURSE,
        QUIZ,
        EXTERNAL_RESOURCE,
        MILESTONE,
        ASSESSMENT
    }
}
