package com.freelms.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDto {

    private Long id;
    private String title;
    private String description;
    private Integer sortOrder;
    private boolean isPublished;
    private Long courseId;
    private List<LessonDto> lessons;
    private Integer totalDuration;
    private Integer lessonCount;
}
