package com.freelms.idp.dto;

import com.freelms.idp.entity.DevelopmentGoal;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalDto {
    private Long id;
    private String title;
    private String description;
    private DevelopmentGoal.GoalType goalType;
    private DevelopmentGoal.GoalStatus status;
    private LocalDate targetDate;
    private LocalDate completedDate;
    private Integer progressPercentage;
    private String successCriteria;
    private String resourcesNeeded;
    private Long skillId;
    private String skillName;
    private Long learningPathId;
    private String learningPathTitle;
    private Long courseId;
    private String courseTitle;
    private List<ActionDto> actions;
}
