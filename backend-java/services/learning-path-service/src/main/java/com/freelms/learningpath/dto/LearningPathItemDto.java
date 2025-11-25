package com.freelms.learningpath.dto;

import com.freelms.learningpath.entity.LearningPathItem;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathItemDto {
    private Long id;
    private LearningPathItem.ItemType itemType;
    private Long courseId;
    private Long quizId;
    private String externalResourceUrl;
    private String externalResourceTitle;
    private Integer orderIndex;
    private Boolean isOptional;
    private Integer unlockAfterDays;
    private Integer deadlineDays;
    private List<Long> prerequisiteIds;
    private Integer minScoreToPass;
    private Integer pointsReward;

    // Enriched data
    private String courseTitle;
    private String quizTitle;
}
