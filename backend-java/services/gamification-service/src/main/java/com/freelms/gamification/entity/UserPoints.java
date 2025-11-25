package com.freelms.gamification.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPoints extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "total_points")
    @Builder.Default
    private Long totalPoints = 0L;

    @Column(name = "weekly_points")
    @Builder.Default
    private Long weeklyPoints = 0L;

    @Column(name = "monthly_points")
    @Builder.Default
    private Long monthlyPoints = 0L;

    @Column(name = "quarterly_points")
    @Builder.Default
    private Long quarterlyPoints = 0L;

    @Column(name = "yearly_points")
    @Builder.Default
    private Long yearlyPoints = 0L;

    @Column(name = "level")
    @Builder.Default
    private Integer level = 1;

    @Column(name = "experience_points")
    @Builder.Default
    private Long experiencePoints = 0L;

    @Column(name = "coins_balance")
    @Builder.Default
    private Long coinsBalance = 0L;

    @Column(name = "organization_id")
    private Long organizationId;
}
