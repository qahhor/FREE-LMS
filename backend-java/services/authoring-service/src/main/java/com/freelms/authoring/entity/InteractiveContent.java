package com.freelms.authoring.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing interactive content (H5P, SCORM, xAPI).
 */
@Entity
@Table(name = "interactive_contents")
public class InteractiveContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentFormat format;

    @Column(name = "h5p_content_type")
    private String h5pContentType; // e.g., "InteractiveVideo", "CoursePresentation", "Quiz"

    @Column(name = "scorm_version")
    private String scormVersion; // "1.2", "2004_3rd", "2004_4th"

    @Column(name = "content_data", columnDefinition = "TEXT")
    private String contentData; // JSON structure of the content

    @Column(name = "storage_path")
    private String storagePath;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "lesson_id")
    private Long lessonId;

    @Enumerated(EnumType.STRING)
    private ContentStatus status = ContentStatus.DRAFT;

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "parent_content_id")
    private UUID parentContentId; // For versioning

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "passing_score")
    private Integer passingScore;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "attempt_limit")
    private Integer attemptLimit;

    @Column(name = "show_feedback")
    private boolean showFeedback = true;

    @Column(name = "shuffle_questions")
    private boolean shuffleQuestions = false;

    @Column(name = "allow_review")
    private boolean allowReview = true;

    @Column(name = "embed_code", length = 4000)
    private String embedCode;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public enum ContentType {
        H5P,
        SCORM,
        XAPI,
        CUSTOM_QUIZ,
        INTERACTIVE_VIDEO,
        PRESENTATION,
        SIMULATION
    }

    public enum ContentFormat {
        H5P_PACKAGE,
        SCORM_PACKAGE,
        XAPI_STATEMENT,
        JSON,
        HTML
    }

    public enum ContentStatus {
        DRAFT,
        REVIEW,
        PUBLISHED,
        ARCHIVED
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ContentType getContentType() { return contentType; }
    public void setContentType(ContentType contentType) { this.contentType = contentType; }
    public ContentFormat getFormat() { return format; }
    public void setFormat(ContentFormat format) { this.format = format; }
    public String getH5pContentType() { return h5pContentType; }
    public void setH5pContentType(String h5pContentType) { this.h5pContentType = h5pContentType; }
    public String getScormVersion() { return scormVersion; }
    public void setScormVersion(String scormVersion) { this.scormVersion = scormVersion; }
    public String getContentData() { return contentData; }
    public void setContentData(String contentData) { this.contentData = contentData; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public ContentStatus getStatus() { return status; }
    public void setStatus(ContentStatus status) { this.status = status; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public UUID getParentContentId() { return parentContentId; }
    public void setParentContentId(UUID parentContentId) { this.parentContentId = parentContentId; }
    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }
    public Integer getPassingScore() { return passingScore; }
    public void setPassingScore(Integer passingScore) { this.passingScore = passingScore; }
    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
    public Integer getAttemptLimit() { return attemptLimit; }
    public void setAttemptLimit(Integer attemptLimit) { this.attemptLimit = attemptLimit; }
    public boolean isShowFeedback() { return showFeedback; }
    public void setShowFeedback(boolean showFeedback) { this.showFeedback = showFeedback; }
    public boolean isShuffleQuestions() { return shuffleQuestions; }
    public void setShuffleQuestions(boolean shuffleQuestions) { this.shuffleQuestions = shuffleQuestions; }
    public boolean isAllowReview() { return allowReview; }
    public void setAllowReview(boolean allowReview) { this.allowReview = allowReview; }
    public String getEmbedCode() { return embedCode; }
    public void setEmbedCode(String embedCode) { this.embedCode = embedCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
