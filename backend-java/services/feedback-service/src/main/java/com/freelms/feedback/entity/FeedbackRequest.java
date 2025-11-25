package com.freelms.feedback.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feedback_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private FeedbackCycle cycle;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false)
    private RelationshipType relationshipType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "is_nominated")
    @Builder.Default
    private Boolean isNominated = false;

    @Column(name = "nominated_by")
    private Long nominatedBy;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FeedbackResponse> responses = new ArrayList<>();

    public enum RelationshipType {
        SELF,
        MANAGER,
        PEER,
        DIRECT_REPORT,
        CROSS_FUNCTIONAL
    }

    public enum RequestStatus {
        PENDING,
        IN_PROGRESS,
        SUBMITTED,
        DECLINED
    }
}
