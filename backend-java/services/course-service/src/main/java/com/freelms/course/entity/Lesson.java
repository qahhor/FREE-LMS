package com.freelms.course.entity;

import com.freelms.common.entity.BaseEntity;
import com.freelms.common.enums.LessonType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lessons", indexes = {
        @Index(name = "idx_lessons_module", columnList = "module_id"),
        @Index(name = "idx_lessons_sort_order", columnList = "sort_order"),
        @Index(name = "idx_lessons_type", columnList = "type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LessonType type = LessonType.VIDEO;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "video_duration_seconds")
    @Builder.Default
    private Integer videoDurationSeconds = 0;

    @Column(name = "duration_minutes")
    @Builder.Default
    private Integer durationMinutes = 0;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_free_preview")
    @Builder.Default
    private boolean isFreePreview = false;

    @Column(name = "is_published")
    @Builder.Default
    private boolean isPublished = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "attachment_name", length = 255)
    private String attachmentName;
}
