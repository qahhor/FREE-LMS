package com.freelms.skills.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillMatrixDto {
    private Long teamId;
    private String teamName;
    private List<SkillDto> skills;
    private List<TeamMemberSkillsDto> members;
    private Map<Long, SkillCoverageDto> skillCoverage;
}
