package com.freelms.feedback.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feedback_cycles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackCycle extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cycle_type", nullable = false)
    private CycleType cycleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CycleStatus status = CycleStatus.DRAFT;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "self_review_deadline")
    private LocalDate selfReviewDeadline;

    @Column(name = "peer_nomination_deadline")
    private LocalDate peerNominationDeadline;

    @Column(name = "peer_review_deadline")
    private LocalDate peerReviewDeadline;

    @Column(name = "manager_review_deadline")
    private LocalDate managerReviewDeadline;

    @Column(name = "is_anonymous")
    @Builder.Default
    private Boolean isAnonymous = false;

    @Column(name = "min_peer_reviewers")
    @Builder.Default
    private Integer minPeerReviewers = 3;

    @Column(name = "max_peer_reviewers")
    @Builder.Default
    private Integer maxPeerReviewers = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private FeedbackTemplate template;

    @OneToMany(mappedBy = "cycle", cascade = CascadeType.ALL)
    @Builder.Default
    private List<FeedbackRequest> requests = new ArrayList<>();

    @Column(name = "created_by")
    private Long createdBy;

    public enum CycleType {
        DEGREE_360,
        PERFORMANCE_REVIEW,
        PEER_FEEDBACK,
        UPWARD_FEEDBACK,
        PULSE_SURVEY
    }

    public enum CycleStatus {
        DRAFT,
        ACTIVE,
        SELF_REVIEW,
        PEER_NOMINATION,
        PEER_REVIEW,
        MANAGER_REVIEW,
        COMPLETED,
        CANCELLED
    }
}
