package com.freelms.gamification.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_participants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"challenge_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private ChallengeTeam team;

    @Column(name = "current_progress")
    @Builder.Default
    private Integer currentProgress = 0;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "is_winner")
    @Builder.Default
    private Boolean isWinner = false;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "reward_claimed")
    @Builder.Default
    private Boolean rewardClaimed = false;
}
