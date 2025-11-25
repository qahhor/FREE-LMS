package com.freelms.skills.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_skill_matrices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamSkillMatrix extends BaseEntity {

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills; // JSON: [{skillId, minLevel, isRequired}]

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "last_updated_at")
    @Builder.Default
    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
