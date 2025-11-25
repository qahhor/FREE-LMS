package com.freelms.learningpath.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLearningPathRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    private String description;

    private String thumbnailUrl;

    private Integer estimatedDurationHours;

    private String difficultyLevel;

    private Boolean isMandatory;

    private List<String> targetRoles;

    private List<String> targetDepartments;

    private List<CreateLearningPathItemRequest> items;

    private Integer pointsReward;

    private Long badgeId;

    private Long completionCertificateTemplateId;
}
