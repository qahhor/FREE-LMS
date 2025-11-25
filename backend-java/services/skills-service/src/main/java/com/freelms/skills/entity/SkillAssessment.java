package com.freelms.skills.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "skill_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillAssessment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_skill_id", nullable = false)
    private UserSkill userSkill;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type", nullable = false)
    private AssessmentType assessmentType;

    @Column(name = "assessor_id")
    private Long assessorId; // null for self-assessment

    @Column(name = "assessed_level", nullable = false)
    private Integer assessedLevel;

    @Column(name = "previous_level")
    private Integer previousLevel;

    @Column(name = "assessed_at", nullable = false)
    @Builder.Default
    private LocalDateTime assessedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "evidence_urls", columnDefinition = "TEXT")
    private String evidenceUrls; // JSON array of URLs

    @Column(name = "quiz_score")
    private Integer quizScore;

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = false;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public enum AssessmentType {
        SELF,
        MANAGER,
        PEER,
        QUIZ,
        CERTIFICATION,
        PROJECT
    }
}
