package com.freelms.skills.dto;

import com.freelms.skills.entity.SkillGap;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillGapDto {
    private Long id;
    private Long userId;
    private Long skillId;
    private String skillName;
    private Integer currentLevel;
    private Integer requiredLevel;
    private Integer gapSize;
    private SkillGap.GapPriority priority;
    private String sourceType;
    private Long sourceId;
    private LocalDateTime targetDate;
    private List<Long> recommendedCourseIds;
    private LocalDateTime identifiedAt;
}
