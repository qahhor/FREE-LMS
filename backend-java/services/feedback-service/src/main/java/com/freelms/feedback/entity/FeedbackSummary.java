package com.freelms.feedback.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_summaries",
       uniqueConstraints = @UniqueConstraint(columnNames = {"cycle_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackSummary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private FeedbackCycle cycle;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "overall_rating")
    private Double overallRating;

    @Column(name = "self_rating")
    private Double selfRating;

    @Column(name = "manager_rating")
    private Double managerRating;

    @Column(name = "peer_rating")
    private Double peerRating;

    @Column(name = "direct_report_rating")
    private Double directReportRating;

    @Column(name = "total_reviewers")
    private Integer totalReviewers;

    @Column(name = "completed_reviews")
    private Integer completedReviews;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths; // JSON array

    @Column(name = "areas_for_improvement", columnDefinition = "TEXT")
    private String areasForImprovement; // JSON array

    @Column(name = "category_scores", columnDefinition = "TEXT")
    private String categoryScores; // JSON: {category: avgScore}

    @Column(name = "generated_at")
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Column(name = "is_shared_with_employee")
    @Builder.Default
    private Boolean isSharedWithEmployee = false;

    @Column(name = "shared_at")
    private LocalDateTime sharedAt;
}
