package com.freelms.learningpath.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "required_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequiredSkill extends BaseEntity {

    @Column(name = "skill_id", nullable = false)
    private Long skillId; // Reference to skills-service

    @Column(name = "min_level", nullable = false)
    @Builder.Default
    private Integer minLevel = 1; // 1-5 scale

    @Column(name = "is_mandatory")
    @Builder.Default
    private Boolean isMandatory = true;
}
