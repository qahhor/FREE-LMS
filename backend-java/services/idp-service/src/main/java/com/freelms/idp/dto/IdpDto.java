package com.freelms.idp.dto;

import com.freelms.idp.entity.IndividualDevelopmentPlan;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdpDto {
    private Long id;
    private Long userId;
    private String userName;
    private String title;
    private String description;
    private IndividualDevelopmentPlan.PlanStatus status;
    private LocalDate startDate;
    private LocalDate targetDate;
    private Long managerId;
    private String managerName;
    private Long mentorId;
    private String mentorName;
    private String careerGoal;
    private String currentRole;
    private String targetRole;
    private Integer progressPercentage;
    private List<GoalDto> goals;
    private List<ReviewDto> reviews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
