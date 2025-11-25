package com.freelms.idp.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "development_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevelopmentGoal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private IndividualDevelopmentPlan plan;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false)
    private GoalType goalType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GoalStatus status = GoalStatus.NOT_STARTED;

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "progress_percentage")
    @Builder.Default
    private Integer progressPercentage = 0;

    @Column(name = "success_criteria", columnDefinition = "TEXT")
    private String successCriteria;

    @Column(name = "resources_needed", columnDefinition = "TEXT")
    private String resourcesNeeded;

    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "learning_path_id")
    private Long learningPathId;

    @Column(name = "course_id")
    private Long courseId;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GoalAction> actions = new ArrayList<>();

    public enum GoalType {
        SKILL_DEVELOPMENT,
        COURSE_COMPLETION,
        CERTIFICATION,
        PROJECT,
        MENTORING,
        NETWORKING,
        LEADERSHIP,
        CUSTOM
    }

    public enum GoalStatus {
        NOT_STARTED,
        IN_PROGRESS,
        ON_HOLD,
        COMPLETED,
        CANCELLED
    }
}
