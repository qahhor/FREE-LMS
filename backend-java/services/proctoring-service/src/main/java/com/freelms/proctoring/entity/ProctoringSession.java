package com.freelms.proctoring.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a proctoring session for an exam.
 */
@Entity
@Table(name = "proctoring_sessions")
public class ProctoringSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.PENDING;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "scheduled_duration_minutes")
    private Integer scheduledDurationMinutes;

    // Identity verification
    @Column(name = "identity_verified")
    private boolean identityVerified = false;

    @Column(name = "identity_verification_time")
    private LocalDateTime identityVerificationTime;

    @Column(name = "identity_photo_path")
    private String identityPhotoPath;

    @Column(name = "id_document_path")
    private String idDocumentPath;

    @Column(name = "face_match_score")
    private Float faceMatchScore;

    // Environment check
    @Column(name = "environment_checked")
    private boolean environmentChecked = false;

    @Column(name = "browser_lockdown_active")
    private boolean browserLockdownActive = false;

    @Column(name = "webcam_enabled")
    private boolean webcamEnabled = false;

    @Column(name = "microphone_enabled")
    private boolean microphoneEnabled = false;

    @Column(name = "screen_share_enabled")
    private boolean screenShareEnabled = false;

    // Recording paths
    @Column(name = "webcam_recording_path")
    private String webcamRecordingPath;

    @Column(name = "screen_recording_path")
    private String screenRecordingPath;

    @Column(name = "audio_recording_path")
    private String audioRecordingPath;

    // Violation tracking
    @Column(name = "violation_count")
    private Integer violationCount = 0;

    @Column(name = "warning_count")
    private Integer warningCount = 0;

    @Column(name = "risk_score")
    private Float riskScore = 0f;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel = RiskLevel.LOW;

    // AI analysis results
    @Column(name = "ai_analysis_status")
    @Enumerated(EnumType.STRING)
    private AnalysisStatus aiAnalysisStatus = AnalysisStatus.PENDING;

    @Column(name = "ai_analysis_completed_at")
    private LocalDateTime aiAnalysisCompletedAt;

    // Review
    @Column(name = "requires_manual_review")
    private boolean requiresManualReview = false;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_notes", length = 2000)
    private String reviewNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_decision")
    private ReviewDecision reviewDecision;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProctoringViolation> violations = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SessionStatus {
        PENDING,
        IDENTITY_CHECK,
        ENVIRONMENT_CHECK,
        IN_PROGRESS,
        PAUSED,
        COMPLETED,
        TERMINATED,
        FAILED
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum AnalysisStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    public enum ReviewDecision {
        APPROVED,
        FLAGGED,
        INVALIDATED
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getScheduledDurationMinutes() { return scheduledDurationMinutes; }
    public void setScheduledDurationMinutes(Integer scheduledDurationMinutes) { this.scheduledDurationMinutes = scheduledDurationMinutes; }
    public boolean isIdentityVerified() { return identityVerified; }
    public void setIdentityVerified(boolean identityVerified) { this.identityVerified = identityVerified; }
    public LocalDateTime getIdentityVerificationTime() { return identityVerificationTime; }
    public void setIdentityVerificationTime(LocalDateTime identityVerificationTime) { this.identityVerificationTime = identityVerificationTime; }
    public String getIdentityPhotoPath() { return identityPhotoPath; }
    public void setIdentityPhotoPath(String identityPhotoPath) { this.identityPhotoPath = identityPhotoPath; }
    public String getIdDocumentPath() { return idDocumentPath; }
    public void setIdDocumentPath(String idDocumentPath) { this.idDocumentPath = idDocumentPath; }
    public Float getFaceMatchScore() { return faceMatchScore; }
    public void setFaceMatchScore(Float faceMatchScore) { this.faceMatchScore = faceMatchScore; }
    public boolean isEnvironmentChecked() { return environmentChecked; }
    public void setEnvironmentChecked(boolean environmentChecked) { this.environmentChecked = environmentChecked; }
    public boolean isBrowserLockdownActive() { return browserLockdownActive; }
    public void setBrowserLockdownActive(boolean browserLockdownActive) { this.browserLockdownActive = browserLockdownActive; }
    public boolean isWebcamEnabled() { return webcamEnabled; }
    public void setWebcamEnabled(boolean webcamEnabled) { this.webcamEnabled = webcamEnabled; }
    public boolean isMicrophoneEnabled() { return microphoneEnabled; }
    public void setMicrophoneEnabled(boolean microphoneEnabled) { this.microphoneEnabled = microphoneEnabled; }
    public boolean isScreenShareEnabled() { return screenShareEnabled; }
    public void setScreenShareEnabled(boolean screenShareEnabled) { this.screenShareEnabled = screenShareEnabled; }
    public String getWebcamRecordingPath() { return webcamRecordingPath; }
    public void setWebcamRecordingPath(String webcamRecordingPath) { this.webcamRecordingPath = webcamRecordingPath; }
    public String getScreenRecordingPath() { return screenRecordingPath; }
    public void setScreenRecordingPath(String screenRecordingPath) { this.screenRecordingPath = screenRecordingPath; }
    public String getAudioRecordingPath() { return audioRecordingPath; }
    public void setAudioRecordingPath(String audioRecordingPath) { this.audioRecordingPath = audioRecordingPath; }
    public Integer getViolationCount() { return violationCount; }
    public void setViolationCount(Integer violationCount) { this.violationCount = violationCount; }
    public Integer getWarningCount() { return warningCount; }
    public void setWarningCount(Integer warningCount) { this.warningCount = warningCount; }
    public Float getRiskScore() { return riskScore; }
    public void setRiskScore(Float riskScore) { this.riskScore = riskScore; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public AnalysisStatus getAiAnalysisStatus() { return aiAnalysisStatus; }
    public void setAiAnalysisStatus(AnalysisStatus aiAnalysisStatus) { this.aiAnalysisStatus = aiAnalysisStatus; }
    public LocalDateTime getAiAnalysisCompletedAt() { return aiAnalysisCompletedAt; }
    public void setAiAnalysisCompletedAt(LocalDateTime aiAnalysisCompletedAt) { this.aiAnalysisCompletedAt = aiAnalysisCompletedAt; }
    public boolean isRequiresManualReview() { return requiresManualReview; }
    public void setRequiresManualReview(boolean requiresManualReview) { this.requiresManualReview = requiresManualReview; }
    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    public ReviewDecision getReviewDecision() { return reviewDecision; }
    public void setReviewDecision(ReviewDecision reviewDecision) { this.reviewDecision = reviewDecision; }
    public List<ProctoringViolation> getViolations() { return violations; }
    public void setViolations(List<ProctoringViolation> violations) { this.violations = violations; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
