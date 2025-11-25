package com.freelms.idp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateIdpRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate targetDate;
    private Long managerId;
    private Long mentorId;
    private String careerGoal;
    private String currentRole;
    private String targetRole;
    private List<CreateGoalRequest> goals;
}
