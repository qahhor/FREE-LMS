package com.freelms.learningpath.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerLevelDto {
    private Long id;
    private String title;
    private String description;
    private Integer levelOrder;
    private Integer minExperienceMonths;
    private Long salaryRangeMin;
    private Long salaryRangeMax;
    private List<RequiredSkillDto> requiredSkills;
    private List<Long> requiredLearningPathIds;
    private Long badgeId;
    private Integer pointsRequired;
}
