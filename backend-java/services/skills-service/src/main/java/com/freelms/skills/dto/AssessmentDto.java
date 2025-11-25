package com.freelms.skills.dto;

import com.freelms.skills.entity.SkillAssessment;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentDto {
    private Long id;
    private SkillAssessment.AssessmentType assessmentType;
    private Long assessorId;
    private String assessorName;
    private Integer assessedLevel;
    private Integer previousLevel;
    private LocalDateTime assessedAt;
    private String feedback;
    private Integer quizScore;
    private Boolean isApproved;
}
