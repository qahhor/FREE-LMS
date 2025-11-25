package com.freelms.learningpath.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_career_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "career_track_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCareerProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_track_id", nullable = false)
    private CareerTrack careerTrack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_level_id")
    private CareerLevel currentLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_level_id")
    private CareerLevel targetLevel;

    @Column(name = "started_at")
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "current_level_achieved_at")
    private LocalDateTime currentLevelAchievedAt;

    @Column(name = "progress_percentage")
    @Builder.Default
    private Integer progressPercentage = 0;

    @Column(name = "skills_completed")
    @Builder.Default
    private Integer skillsCompleted = 0;

    @Column(name = "skills_total")
    @Builder.Default
    private Integer skillsTotal = 0;

    @Column(name = "paths_completed")
    @Builder.Default
    private Integer pathsCompleted = 0;

    @Column(name = "paths_total")
    @Builder.Default
    private Integer pathsTotal = 0;

    @Column(name = "mentor_id")
    private Long mentorId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
