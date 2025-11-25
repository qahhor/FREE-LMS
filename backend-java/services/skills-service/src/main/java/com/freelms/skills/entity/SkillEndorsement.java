package com.freelms.skills.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "skill_endorsements",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_skill_id", "endorser_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillEndorsement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_skill_id", nullable = false)
    private UserSkill userSkill;

    @Column(name = "endorser_id", nullable = false)
    private Long endorserId;

    @Column(name = "endorsed_level")
    private Integer endorsedLevel;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "endorsed_at", nullable = false)
    @Builder.Default
    private LocalDateTime endorsedAt = LocalDateTime.now();

    @Column(name = "relationship")
    private String relationship; // "peer", "manager", "direct_report", "external"
}
