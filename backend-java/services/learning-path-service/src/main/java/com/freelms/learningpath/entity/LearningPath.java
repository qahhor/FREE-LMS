package com.freelms.learningpath.entity;

import com.freelms.common.entity.BaseEntity;
import com.freelms.common.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_paths")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPath extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "created_by")
    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CourseStatus status = CourseStatus.DRAFT;

    @Column(name = "estimated_duration_hours")
    private Integer estimatedDurationHours;

    @Column(name = "difficulty_level")
    private String difficultyLevel;

    @Column(name = "is_mandatory")
    @Builder.Default
    private Boolean isMandatory = false;

    @Column(name = "target_roles")
    private String targetRoles; // JSON array of role IDs

    @Column(name = "target_departments")
    private String targetDepartments; // JSON array of department IDs

    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<LearningPathItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL)
    @Builder.Default
    private List<LearningPathEnrollment> enrollments = new ArrayList<>();

    @Column(name = "completion_certificate_template_id")
    private Long completionCertificateTemplateId;

    @Column(name = "points_reward")
    @Builder.Default
    private Integer pointsReward = 0;

    @Column(name = "badge_id")
    private Long badgeId;
}
