package com.freelms.gamification.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPointsDto {
    private Long userId;
    private Long totalPoints;
    private Long weeklyPoints;
    private Long monthlyPoints;
    private Long quarterlyPoints;
    private Integer level;
    private Long experiencePoints;
    private Long coinsBalance;
    private Long pointsToNextLevel;
    private Integer percentageToNextLevel;
}
