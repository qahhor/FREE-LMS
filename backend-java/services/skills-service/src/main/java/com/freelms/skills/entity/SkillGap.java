package com.freelms.skills.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "skill_gaps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillGap extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "current_level", nullable = false)
    private Integer currentLevel;

    @Column(name = "required_level", nullable = false)
    private Integer requiredLevel;

    @Column(name = "gap_size", nullable = false)
    private Integer gapSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GapPriority priority;

    @Column(name = "source_type")
    private String sourceType; // "role_requirement", "career_track", "project", "custom"

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "target_date")
    private LocalDateTime targetDate;

    @Column(name = "recommended_courses", columnDefinition = "TEXT")
    private String recommendedCourses; // JSON array of course IDs

    @Column(name = "identified_at", nullable = false)
    @Builder.Default
    private LocalDateTime identifiedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public enum GapPriority {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }
}
