package com.freelms.skills.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillCoverageDto {
    private Long skillId;
    private String skillName;
    private Integer requiredLevel;
    private Integer membersWithSkill;
    private Integer membersMeetingRequirement;
    private Double averageLevel;
    private Boolean hasCriticalGap;
}
