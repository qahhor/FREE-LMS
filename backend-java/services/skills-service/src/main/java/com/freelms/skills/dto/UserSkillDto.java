package com.freelms.skills.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkillDto {
    private Long id;
    private Long userId;
    private Long skillId;
    private String skillName;
    private String categoryName;
    private Integer currentLevel;
    private String currentLevelName;
    private Integer targetLevel;
    private Integer selfAssessedLevel;
    private Integer managerAssessedLevel;
    private Double peerAverageLevel;
    private LocalDateTime lastAssessmentDate;
    private LocalDateTime nextAssessmentDue;
    private Boolean isVerified;
    private Integer endorsementCount;
    private List<EndorsementDto> endorsements;
    private List<AssessmentDto> recentAssessments;
}
