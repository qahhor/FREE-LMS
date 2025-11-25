package com.freelms.gamification.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rewards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false)
    private RewardType rewardType;

    @Column(name = "coins_cost")
    @Builder.Default
    private Integer coinsCost = 0;

    @Column(name = "points_cost")
    @Builder.Default
    private Integer pointsCost = 0;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "quantity_available")
    private Integer quantityAvailable; // null = unlimited

    @Column(name = "quantity_claimed")
    @Builder.Default
    private Integer quantityClaimed = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value; // JSON: depends on type (e.g., coupon code, extra days off)

    public enum RewardType {
        PHYSICAL_ITEM,
        DIGITAL_ITEM,
        DISCOUNT_COUPON,
        EXTRA_LEAVE,
        CERTIFICATE,
        PRIVILEGE,
        CUSTOM
    }
}
