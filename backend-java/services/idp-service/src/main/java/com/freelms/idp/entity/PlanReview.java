package com.freelms.idp.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "plan_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private IndividualDevelopmentPlan plan;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;

    @Column(name = "overall_progress")
    private Integer overallProgress;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(name = "is_on_track")
    private Boolean isOnTrack;

    public enum ReviewType {
        INITIAL,
        MONTHLY,
        QUARTERLY,
        MIDTERM,
        FINAL,
        AD_HOC
    }
}
