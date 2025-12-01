package com.freelms.lms.course.entity;

import com.freelms.lms.common.entity.BaseEntity;
import com.freelms.lms.common.enums.CourseLevel;
import com.freelms.lms.common.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "courses", indexes = {
        @Index(name = "idx_courses_slug", columnList = "slug", unique = true),
        @Index(name = "idx_courses_status", columnList = "status"),
        @Index(name = "idx_courses_instructor", columnList = "instructor_id"),
        @Index(name = "idx_courses_category", columnList = "category_id"),
        @Index(name = "idx_courses_is_free", columnList = "is_free"),
        @Index(name = "idx_courses_published_at", columnList = "published_at"),
        @Index(name = "idx_courses_organization", columnList = "organization_id")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Course extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 300)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "preview_video_url", length = 500)
    private String previewVideoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CourseStatus status = CourseStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CourseLevel level = CourseLevel.BEGINNER;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "is_free")
    @Builder.Default
    private boolean isFree = true;

    @Column(name = "duration_minutes")
    @Builder.Default
    private Integer durationMinutes = 0;

    @Column(name = "student_count")
    @Builder.Default
    private Integer studentCount = 0;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;

    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;

    @Column(name = "organization_id")
    private Long organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(length = 50)
    @Builder.Default
    private String language = "en";

    @Column(name = "what_you_will_learn", columnDefinition = "TEXT")
    private String whatYouWillLearn;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "target_audience", columnDefinition = "TEXT")
    private String targetAudience;

    @Column(name = "is_featured")
    @Builder.Default
    private boolean isFeatured = false;

    @Column(name = "completion_certificate")
    @Builder.Default
    private boolean completionCertificate = true;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<CourseModule> modules = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "course_tags",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    public void publish() {
        this.status = CourseStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void archive() {
        this.status = CourseStatus.ARCHIVED;
    }

    public void incrementStudentCount() {
        this.studentCount++;
    }

    public void updateRating(BigDecimal newRating) {
        BigDecimal totalRating = this.rating.multiply(BigDecimal.valueOf(this.ratingCount));
        this.ratingCount++;
        this.rating = totalRating.add(newRating)
                .divide(BigDecimal.valueOf(this.ratingCount), 2, java.math.RoundingMode.HALF_UP);
    }

    public void calculateDuration() {
        this.durationMinutes = modules.stream()
                .flatMap(m -> m.getLessons().stream())
                .mapToInt(Lesson::getDurationMinutes)
                .sum();
    }
}
