package com.freelms.skills.dto;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberSkillsDto {
    private Long userId;
    private String userName;
    private String avatarUrl;
    private String role;
    private Map<Long, Integer> skillLevels; // skillId -> level
    private Integer totalSkills;
    private Double averageLevel;
}
