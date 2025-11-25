package com.freelms.course.dto;

import com.freelms.common.enums.CourseLevel;
import com.freelms.common.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {

    private Long id;
    private String title;
    private String slug;
    private String description;
    private String shortDescription;
    private String thumbnailUrl;
    private String previewVideoUrl;
    private CourseStatus status;
    private CourseLevel level;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private boolean isFree;
    private Integer durationMinutes;
    private Integer studentCount;
    private BigDecimal rating;
    private Integer ratingCount;
    private Long instructorId;
    private String instructorName;
    private CategoryDto category;
    private LocalDateTime publishedAt;
    private String language;
    private String whatYouWillLearn;
    private String requirements;
    private String targetAudience;
    private List<ModuleDto> modules;
    private Set<TagDto> tags;
    private Integer totalLessons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
