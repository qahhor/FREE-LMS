package com.freelms.idp.dto;

import com.freelms.idp.entity.DevelopmentGoal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGoalRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    @NotNull(message = "Goal type is required")
    private DevelopmentGoal.GoalType goalType;
    private LocalDate targetDate;
    private String successCriteria;
    private String resourcesNeeded;
    private Long skillId;
    private Long learningPathId;
    private Long courseId;
}
