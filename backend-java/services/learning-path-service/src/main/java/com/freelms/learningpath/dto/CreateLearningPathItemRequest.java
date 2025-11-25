package com.freelms.learningpath.dto;

import com.freelms.learningpath.entity.LearningPathItem;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLearningPathItemRequest {

    @NotNull(message = "Item type is required")
    private LearningPathItem.ItemType itemType;

    private Long courseId;

    private Long quizId;

    private String externalResourceUrl;

    private String externalResourceTitle;

    @NotNull(message = "Order index is required")
    private Integer orderIndex;

    private Boolean isOptional;

    private Integer unlockAfterDays;

    private Integer deadlineDays;

    private List<Long> prerequisiteItemIds;

    private Integer minScoreToPass;

    private Integer pointsReward;
}
