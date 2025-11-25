package com.freelms.course.dto;

import com.freelms.common.enums.CourseLevel;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseRequest {

    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    private String description;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    private String thumbnailUrl;

    private String previewVideoUrl;

    private CourseLevel level;

    private BigDecimal price;

    private Boolean isFree;

    private Long categoryId;

    private String language;

    private String whatYouWillLearn;

    private String requirements;

    private String targetAudience;

    private Set<String> tags;
}
