package com.freelms.learningpath.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "career_levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerLevel extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_track_id", nullable = false)
    private CareerTrack careerTrack;

    @Column(nullable = false)
    private String title; // e.g., "Junior Developer", "Middle Developer"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "level_order", nullable = false)
    private Integer levelOrder;

    @Column(name = "min_experience_months")
    private Integer minExperienceMonths;

    @Column(name = "salary_range_min")
    private Long salaryRangeMin;

    @Column(name = "salary_range_max")
    private Long salaryRangeMax;

    @ManyToMany
    @JoinTable(
        name = "career_level_required_skills",
        joinColumns = @JoinColumn(name = "level_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private List<RequiredSkill> requiredSkills = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "career_level_learning_paths",
        joinColumns = @JoinColumn(name = "level_id"),
        inverseJoinColumns = @JoinColumn(name = "learning_path_id")
    )
    @Builder.Default
    private List<LearningPath> requiredLearningPaths = new ArrayList<>();

    @Column(name = "badge_id")
    private Long badgeId;

    @Column(name = "points_required")
    @Builder.Default
    private Integer pointsRequired = 0;
}
