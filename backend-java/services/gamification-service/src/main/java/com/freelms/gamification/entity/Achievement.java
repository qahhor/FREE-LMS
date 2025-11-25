package com.freelms.gamification.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AchievementRarity rarity = AchievementRarity.COMMON;

    @Column(name = "points_reward")
    @Builder.Default
    private Integer pointsReward = 0;

    @Column(name = "coins_reward")
    @Builder.Default
    private Integer coinsReward = 0;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "is_global")
    @Builder.Default
    private Boolean isGlobal = false;

    @Column(name = "is_hidden")
    @Builder.Default
    private Boolean isHidden = false;

    @Column(name = "trigger_condition", columnDefinition = "TEXT")
    private String triggerCondition; // JSON: {"type": "course_complete", "count": 5}

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    public enum AchievementType {
        COURSE_COMPLETION,
        LEARNING_PATH_COMPLETION,
        STREAK,
        QUIZ_SCORE,
        FIRST_ACTION,
        MILESTONE,
        SOCIAL,
        SPECIAL
    }

    public enum AchievementRarity {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY
    }
}
