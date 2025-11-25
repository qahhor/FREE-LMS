package com.freelms.gamification.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStreakDto {
    private Long userId;
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastActivityDate;
    private LocalDate streakStartDate;
    private Integer freezeCount;
    private Integer weeklyGoal;
    private Integer weeklyProgress;
    private Integer totalActiveDays;
    private Boolean isAtRisk; // If user hasn't been active today
}
