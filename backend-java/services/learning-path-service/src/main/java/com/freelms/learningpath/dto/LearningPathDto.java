package com.freelms.learningpath.dto;

import com.freelms.common.enums.CourseStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathDto {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private Long organizationId;
    private Long createdBy;
    private CourseStatus status;
    private Integer estimatedDurationHours;
    private String difficultyLevel;
    private Boolean isMandatory;
    private List<String> targetRoles;
    private List<String> targetDepartments;
    private List<LearningPathItemDto> items;
    private Integer pointsReward;
    private Long badgeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Statistics
    private Long enrollmentCount;
    private Long completionCount;
    private Double averageProgress;
}
