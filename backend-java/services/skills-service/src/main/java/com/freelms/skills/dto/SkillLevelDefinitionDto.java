package com.freelms.skills.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillLevelDefinitionDto {
    private Long id;
    private Integer level;
    private String name;
    private String description;
    private String criteria;
    private Integer minAssessmentsRequired;
    private Integer minPeerEndorsements;
}
