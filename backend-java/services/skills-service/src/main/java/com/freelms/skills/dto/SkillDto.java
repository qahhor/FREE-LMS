package com.freelms.skills.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDto {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long organizationId;
    private Boolean isGlobal;
    private Integer maxLevel;
    private String iconUrl;
    private Boolean isActive;
    private List<SkillLevelDefinitionDto> levelDefinitions;
    private List<Long> relatedCourseIds;
}
