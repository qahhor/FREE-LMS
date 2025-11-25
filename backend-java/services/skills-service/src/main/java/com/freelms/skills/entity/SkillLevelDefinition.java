package com.freelms.skills.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skill_level_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillLevelDefinition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private String name; // e.g., "Beginner", "Intermediate", "Advanced", "Expert", "Master"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String criteria; // What is expected at this level

    @Column(name = "min_assessments_required")
    @Builder.Default
    private Integer minAssessmentsRequired = 1;

    @Column(name = "min_peer_endorsements")
    @Builder.Default
    private Integer minPeerEndorsements = 0;
}
