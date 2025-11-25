package com.freelms.idp.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "individual_development_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualDevelopmentPlan extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PlanStatus status = PlanStatus.DRAFT;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "mentor_id")
    private Long mentorId;

    @Column(name = "career_goal", columnDefinition = "TEXT")
    private String careerGoal;

    @Column(name = "current_role")
    private String currentRole;

    @Column(name = "target_role")
    private String targetRole;

    @Column(name = "progress_percentage")
    @Builder.Default
    private Integer progressPercentage = 0;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<DevelopmentGoal> goals = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
    @OrderBy("reviewDate DESC")
    @Builder.Default
    private List<PlanReview> reviews = new ArrayList<>();

    public enum PlanStatus {
        DRAFT,
        PENDING_APPROVAL,
        APPROVED,
        ACTIVE,
        ON_HOLD,
        COMPLETED,
        CANCELLED
    }
}
