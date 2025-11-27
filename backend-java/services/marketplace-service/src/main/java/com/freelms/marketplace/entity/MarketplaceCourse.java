package com.freelms.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

/**
 * Smartup LMS - Marketplace Course Entity
 *
 * Represents a ready-made educational course available for purchase/subscription.
 */
@Entity
@Table(name = "marketplace_courses")
@DiscriminatorValue("COURSE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceCourse extends MarketplaceItem {

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type")
    private CourseType courseType = CourseType.SELF_PACED;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;

    // Course structure
    @Column(name = "module_count")
    private Integer moduleCount = 0;

    @Column(name = "lesson_count")
    private Integer lessonCount = 0;

    @Column(name = "quiz_count")
    private Integer quizCount = 0;

    @Column(name = "assignment_count")
    private Integer assignmentCount = 0;

    @Column(name = "video_count")
    private Integer videoCount = 0;

    @Column(name = "total_duration_minutes")
    private Integer totalDurationMinutes = 0;

    // Content details
    @ElementCollection
    @CollectionTable(name = "course_languages", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "language_code")
    private Set<String> languages = new HashSet<>();

    @Column(name = "has_subtitles")
    private boolean hasSubtitles;

    @ElementCollection
    @CollectionTable(name = "course_subtitle_languages", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "language_code")
    private Set<String> subtitleLanguages = new HashSet<>();

    @Column(name = "has_downloadable_resources")
    private boolean hasDownloadableResources;

    @Column(name = "resource_count")
    private Integer resourceCount = 0;

    // Learning outcomes
    @ElementCollection
    @CollectionTable(name = "course_outcomes", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "outcome", columnDefinition = "TEXT")
    private List<String> learningOutcomes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "course_prerequisites", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "prerequisite")
    private List<String> prerequisites = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "course_target_audience", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "audience")
    private List<String> targetAudience = new ArrayList<>();

    // Skills and certifications
    @ElementCollection
    @CollectionTable(name = "course_skills", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();

    @Column(name = "has_certificate")
    private boolean hasCertificate = true;

    @Column(name = "certificate_template_id")
    private Long certificateTemplateId;

    @Column(name = "cpe_credits")
    private Double cpeCredits;

    @Column(name = "accreditation_body")
    private String accreditationBody;

    // Instructor info
    @Column(name = "instructor_id")
    private Long instructorId;

    @Column(name = "instructor_name")
    private String instructorName;

    @Column(name = "instructor_title")
    private String instructorTitle;

    @Column(name = "instructor_bio", columnDefinition = "TEXT")
    private String instructorBio;

    @Column(name = "instructor_avatar_url")
    private String instructorAvatarUrl;

    // Course content package
    @Column(name = "scorm_package_url")
    private String scormPackageUrl;

    @Column(name = "xapi_package_url")
    private String xapiPackageUrl;

    @Column(name = "content_package_url")
    private String contentPackageUrl;

    @Column(name = "content_package_size")
    private Long contentPackageSize;

    // Licensing
    @Enumerated(EnumType.STRING)
    @Column(name = "license_type")
    private LicenseType licenseType = LicenseType.SINGLE_ORGANIZATION;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "validity_days")
    private Integer validityDays;

    @Column(name = "allow_modifications")
    private boolean allowModifications;

    @Column(name = "allow_resale")
    private boolean allowResale;

    // Statistics
    @Column(name = "enrollment_count")
    private Long enrollmentCount = 0L;

    @Column(name = "completion_count")
    private Long completionCount = 0L;

    @Column(name = "average_completion_rate")
    private Double averageCompletionRate = 0.0;

    @Column(name = "average_quiz_score")
    private Double averageQuizScore = 0.0;

    // Course types
    public enum CourseType {
        SELF_PACED,          // Learn at your own pace
        INSTRUCTOR_LED,      // Scheduled with instructor
        BLENDED,             // Mix of self-paced and instructor-led
        VIRTUAL_CLASSROOM,   // Live virtual sessions
        MICROLEARNING,       // Short, focused lessons
        CERTIFICATION_PREP   // Certification exam preparation
    }

    public enum DifficultyLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        EXPERT,
        ALL_LEVELS
    }

    public enum LicenseType {
        SINGLE_USER,           // One user only
        SINGLE_ORGANIZATION,   // Unlimited users in one org
        MULTI_ORGANIZATION,    // Multiple orgs with limit
        UNLIMITED,             // No restrictions
        ENTERPRISE             // Custom enterprise terms
    }

    @Builder(builderMethodName = "courseBuilder")
    public MarketplaceCourse(String slug, String name, String description,
                             CourseType courseType, DifficultyLevel level) {
        this.setSlug(slug);
        this.setName(name);
        this.setDescription(description);
        this.setType(ItemType.COURSE);
        this.courseType = courseType;
        this.difficultyLevel = level;
    }

    // Helper methods
    public String getFormattedDuration() {
        if (totalDurationMinutes == null || totalDurationMinutes == 0) {
            return "N/A";
        }
        int hours = totalDurationMinutes / 60;
        int minutes = totalDurationMinutes % 60;
        if (hours > 0) {
            return hours + " ч " + (minutes > 0 ? minutes + " мин" : "");
        }
        return minutes + " мин";
    }

    public Double getCompletionRate() {
        if (enrollmentCount == null || enrollmentCount == 0) {
            return 0.0;
        }
        return (completionCount * 100.0) / enrollmentCount;
    }
}
