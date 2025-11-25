package com.freelms.gamification.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_streaks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStreak extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "current_streak")
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(name = "longest_streak")
    @Builder.Default
    private Integer longestStreak = 0;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(name = "streak_start_date")
    private LocalDate streakStartDate;

    @Column(name = "freeze_count")
    @Builder.Default
    private Integer freezeCount = 0; // Number of streak freezes available

    @Column(name = "freeze_used_today")
    @Builder.Default
    private Boolean freezeUsedToday = false;

    @Column(name = "weekly_goal")
    @Builder.Default
    private Integer weeklyGoal = 5; // Days per week

    @Column(name = "weekly_progress")
    @Builder.Default
    private Integer weeklyProgress = 0;

    @Column(name = "total_active_days")
    @Builder.Default
    private Integer totalActiveDays = 0;
}
