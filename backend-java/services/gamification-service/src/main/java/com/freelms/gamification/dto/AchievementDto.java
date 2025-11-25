package com.freelms.gamification.dto;

import com.freelms.gamification.entity.Achievement;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementDto {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private Achievement.AchievementType type;
    private Achievement.AchievementRarity rarity;
    private Integer pointsReward;
    private Integer coinsReward;
    private Boolean isHidden;
    private Boolean isEarned;
    private LocalDateTime earnedAt;
    private Integer progress;
}
