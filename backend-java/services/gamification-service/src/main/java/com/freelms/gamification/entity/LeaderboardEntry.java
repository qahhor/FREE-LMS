package com.freelms.gamification.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leaderboard_entries",
       uniqueConstraints = @UniqueConstraint(columnNames = {"leaderboard_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leaderboard_id", nullable = false)
    private Leaderboard leaderboard;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long score;

    @Column(nullable = false)
    private Integer rank;

    @Column(name = "previous_rank")
    private Integer previousRank;

    @Column(name = "rank_change")
    private Integer rankChange; // Positive = moved up, negative = moved down

    @Column(name = "is_top_performer")
    @Builder.Default
    private Boolean isTopPerformer = false; // Top 3
}
