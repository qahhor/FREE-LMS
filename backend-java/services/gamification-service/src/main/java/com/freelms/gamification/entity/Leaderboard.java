package com.freelms.gamification.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leaderboard extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private PeriodType periodType;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "department_id")
    private Long departmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "leaderboard_type", nullable = false)
    @Builder.Default
    private LeaderboardType leaderboardType = LeaderboardType.POINTS;

    @Column(name = "is_finalized")
    @Builder.Default
    private Boolean isFinalized = false;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    public enum PeriodType {
        WEEKLY,
        MONTHLY,
        QUARTERLY,
        YEARLY,
        ALL_TIME
    }

    public enum LeaderboardType {
        POINTS,
        COURSES_COMPLETED,
        STREAK,
        QUIZ_SCORES,
        TIME_SPENT
    }
}
