package com.freelms.course.dto;

import com.freelms.common.enums.LessonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {

    private Long id;
    private String title;
    private String description;
    private String content;
    private LessonType type;
    private String videoUrl;
    private Integer videoDurationSeconds;
    private Integer durationMinutes;
    private Integer sortOrder;
    private boolean isFreePreview;
    private boolean isPublished;
    private Long moduleId;
    private Long quizId;
    private String attachmentUrl;
    private String attachmentName;
}
