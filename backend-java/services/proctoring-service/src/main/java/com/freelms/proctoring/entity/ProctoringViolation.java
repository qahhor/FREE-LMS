package com.freelms.proctoring.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a proctoring violation detected during exam.
 */
@Entity
@Table(name = "proctoring_violations")
public class ProctoringViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ProctoringSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ViolationType violationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(length = 500)
    private String description;

    @Column(name = "confidence_score")
    private Float confidenceScore;

    @Column(name = "timestamp_in_recording")
    private Long timestampInRecording; // milliseconds into recording

    @Column(name = "screenshot_path")
    private String screenshotPath;

    @Column(name = "video_clip_path")
    private String videoClipPath;

    @Column(name = "detected_by")
    @Enumerated(EnumType.STRING)
    private DetectionSource detectedBy = DetectionSource.AI;

    @Column(name = "action_taken")
    @Enumerated(EnumType.STRING)
    private ActionTaken actionTaken;

    @Column(name = "is_false_positive")
    private Boolean isFalsePositive;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    @CreationTimestamp
    @Column(name = "detected_at", updatable = false)
    private LocalDateTime detectedAt;

    public enum ViolationType {
        // Face-related
        FACE_NOT_VISIBLE,
        MULTIPLE_FACES,
        FACE_MISMATCH,
        LOOKING_AWAY,

        // Audio-related
        VOICE_DETECTED,
        MULTIPLE_VOICES,

        // Environment-related
        PERSON_IN_BACKGROUND,
        PHONE_DETECTED,
        BOOK_DETECTED,
        UNAUTHORIZED_DEVICE,

        // Browser-related
        TAB_SWITCH,
        WINDOW_SWITCH,
        COPY_PASTE,
        RIGHT_CLICK,
        BROWSER_CLOSED,

        // Screen-related
        SCREEN_SHARE_STOPPED,
        SUSPICIOUS_APPLICATION,
        VIRTUAL_MACHINE_DETECTED,

        // Network-related
        NETWORK_DISCONNECTED,
        VPN_DETECTED,
        PROXY_DETECTED,

        // System-related
        SYSTEM_TIME_CHANGED,
        FULLSCREEN_EXIT,

        // Other
        OTHER
    }

    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }

    public enum DetectionSource {
        AI,
        AUTOMATED_RULE,
        MANUAL_REVIEW,
        LIVE_PROCTOR
    }

    public enum ActionTaken {
        NONE,
        WARNING_SHOWN,
        EXAM_PAUSED,
        EXAM_TERMINATED,
        FLAGGED_FOR_REVIEW
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ProctoringSession getSession() { return session; }
    public void setSession(ProctoringSession session) { this.session = session; }
    public ViolationType getViolationType() { return violationType; }
    public void setViolationType(ViolationType violationType) { this.violationType = violationType; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Float getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Float confidenceScore) { this.confidenceScore = confidenceScore; }
    public Long getTimestampInRecording() { return timestampInRecording; }
    public void setTimestampInRecording(Long timestampInRecording) { this.timestampInRecording = timestampInRecording; }
    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    public String getVideoClipPath() { return videoClipPath; }
    public void setVideoClipPath(String videoClipPath) { this.videoClipPath = videoClipPath; }
    public DetectionSource getDetectedBy() { return detectedBy; }
    public void setDetectedBy(DetectionSource detectedBy) { this.detectedBy = detectedBy; }
    public ActionTaken getActionTaken() { return actionTaken; }
    public void setActionTaken(ActionTaken actionTaken) { this.actionTaken = actionTaken; }
    public Boolean getIsFalsePositive() { return isFalsePositive; }
    public void setIsFalsePositive(Boolean isFalsePositive) { this.isFalsePositive = isFalsePositive; }
    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
}
