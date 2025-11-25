package com.freelms.skills.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_skills",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkill extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "current_level", nullable = false)
    @Builder.Default
    private Integer currentLevel = 1;

    @Column(name = "target_level")
    private Integer targetLevel;

    @Column(name = "self_assessed_level")
    private Integer selfAssessedLevel;

    @Column(name = "manager_assessed_level")
    private Integer managerAssessedLevel;

    @Column(name = "peer_average_level")
    private Double peerAverageLevel;

    @Column(name = "last_assessment_date")
    private LocalDateTime lastAssessmentDate;

    @Column(name = "next_assessment_due")
    private LocalDateTime nextAssessmentDue;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "endorsement_count")
    @Builder.Default
    private Integer endorsementCount = 0;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "userSkill", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SkillEndorsement> endorsements = new ArrayList<>();

    @OneToMany(mappedBy = "userSkill", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("assessedAt DESC")
    @Builder.Default
    private List<SkillAssessment> assessments = new ArrayList<>();
}
