package com.freelms.gamification.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward_claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardClaim extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @Column(name = "claimed_at", nullable = false)
    @Builder.Default
    private LocalDateTime claimedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.PENDING;

    @Column(name = "coins_spent")
    @Builder.Default
    private Integer coinsSpent = 0;

    @Column(name = "points_spent")
    @Builder.Default
    private Integer pointsSpent = 0;

    @Column(name = "fulfilled_at")
    private LocalDateTime fulfilledAt;

    @Column(name = "fulfilled_by")
    private Long fulfilledBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum ClaimStatus {
        PENDING,
        APPROVED,
        FULFILLED,
        REJECTED,
        CANCELLED
    }
}
