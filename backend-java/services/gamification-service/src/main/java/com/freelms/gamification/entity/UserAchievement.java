package com.freelms.gamification.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(name = "earned_at", nullable = false)
    @Builder.Default
    private LocalDateTime earnedAt = LocalDateTime.now();

    @Column(name = "progress")
    @Builder.Default
    private Integer progress = 100; // Percentage

    @Column(name = "is_notified")
    @Builder.Default
    private Boolean isNotified = false;

    @Column(name = "is_displayed")
    @Builder.Default
    private Boolean isDisplayed = true;
}
