package com.freelms.learningpath.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequiredSkillDto {
    private Long skillId;
    private String skillName;
    private Integer minLevel;
    private Boolean isMandatory;
}
