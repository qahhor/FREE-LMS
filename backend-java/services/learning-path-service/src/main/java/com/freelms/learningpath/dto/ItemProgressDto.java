package com.freelms.learningpath.dto;

import com.freelms.learningpath.entity.LearningPathItemProgress;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemProgressDto {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private LearningPathItemProgress.ProgressStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime unlockDate;
    private LocalDateTime deadline;
    private Integer score;
    private Integer attempts;
    private Integer timeSpentMinutes;
}
