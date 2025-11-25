package com.freelms.learningpath.dto;

import com.freelms.learningpath.entity.LearningPathEnrollment;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentDto {
    private Long id;
    private Long learningPathId;
    private String learningPathTitle;
    private Long userId;
    private String userName;
    private LearningPathEnrollment.EnrollmentStatus status;
    private LocalDateTime enrolledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime deadline;
    private Integer progressPercentage;
    private Integer currentItemIndex;
    private Long assignedBy;
    private String assignedByName;
    private String assignmentNote;
    private List<ItemProgressDto> itemProgress;
}
