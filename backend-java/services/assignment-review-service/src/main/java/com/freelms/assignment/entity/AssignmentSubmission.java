package com.freelms.assignment.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a student's assignment submission.
 */
@Entity
@Table(name = "assignment_submissions")
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(name = "submission_text", columnDefinition = "TEXT")
    private String submissionText;

    @Column(name = "file_paths")
    private String filePaths; // JSON array of file paths

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "is_late")
    private boolean isLate = false;

    @Column(name = "late_penalty_percent")
    private Integer latePenaltyPercent;

    // Grading
    @Column(name = "score")
    private Float score;

    @Column(name = "max_score")
    private Float maxScore;

    @Column(name = "grade")
    private String grade; // A, B, C, etc.

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "graded_by")
    private Long gradedBy;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    // Auto-grading
    @Column(name = "auto_grade_score")
    private Float autoGradeScore;

    @Column(name = "auto_grade_feedback", columnDefinition = "TEXT")
    private String autoGradeFeedback;

    @Column(name = "auto_graded_at")
    private LocalDateTime autoGradedAt;

    // Plagiarism check
    @Column(name = "plagiarism_score")
    private Float plagiarismScore;

    @Column(name = "plagiarism_report_path")
    private String plagiarismReportPath;

    @Column(name = "plagiarism_checked_at")
    private LocalDateTime plagiarismCheckedAt;

    // Peer review
    @Column(name = "peer_review_enabled")
    private boolean peerReviewEnabled = false;

    @Column(name = "peer_review_count")
    private Integer peerReviewCount = 0;

    @Column(name = "peer_average_score")
    private Float peerAverageScore;

    // Rubric
    @Column(name = "rubric_scores", columnDefinition = "TEXT")
    private String rubricScores; // JSON object with criterion -> score mapping

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewComment> comments = new ArrayList<>();

    @Column(name = "attempt_number")
    private Integer attemptNumber = 1;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SubmissionStatus {
        DRAFT,
        SUBMITTED,
        IN_REVIEW,
        PEER_REVIEW,
        GRADED,
        RETURNED,
        RESUBMISSION_REQUESTED
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }
    public String getSubmissionText() { return submissionText; }
    public void setSubmissionText(String submissionText) { this.submissionText = submissionText; }
    public String getFilePaths() { return filePaths; }
    public void setFilePaths(String filePaths) { this.filePaths = filePaths; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public boolean isLate() { return isLate; }
    public void setLate(boolean late) { isLate = late; }
    public Integer getLatePenaltyPercent() { return latePenaltyPercent; }
    public void setLatePenaltyPercent(Integer latePenaltyPercent) { this.latePenaltyPercent = latePenaltyPercent; }
    public Float getScore() { return score; }
    public void setScore(Float score) { this.score = score; }
    public Float getMaxScore() { return maxScore; }
    public void setMaxScore(Float maxScore) { this.maxScore = maxScore; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public Long getGradedBy() { return gradedBy; }
    public void setGradedBy(Long gradedBy) { this.gradedBy = gradedBy; }
    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
    public Float getAutoGradeScore() { return autoGradeScore; }
    public void setAutoGradeScore(Float autoGradeScore) { this.autoGradeScore = autoGradeScore; }
    public String getAutoGradeFeedback() { return autoGradeFeedback; }
    public void setAutoGradeFeedback(String autoGradeFeedback) { this.autoGradeFeedback = autoGradeFeedback; }
    public LocalDateTime getAutoGradedAt() { return autoGradedAt; }
    public void setAutoGradedAt(LocalDateTime autoGradedAt) { this.autoGradedAt = autoGradedAt; }
    public Float getPlagiarismScore() { return plagiarismScore; }
    public void setPlagiarismScore(Float plagiarismScore) { this.plagiarismScore = plagiarismScore; }
    public String getPlagiarismReportPath() { return plagiarismReportPath; }
    public void setPlagiarismReportPath(String plagiarismReportPath) { this.plagiarismReportPath = plagiarismReportPath; }
    public LocalDateTime getPlagiarismCheckedAt() { return plagiarismCheckedAt; }
    public void setPlagiarismCheckedAt(LocalDateTime plagiarismCheckedAt) { this.plagiarismCheckedAt = plagiarismCheckedAt; }
    public boolean isPeerReviewEnabled() { return peerReviewEnabled; }
    public void setPeerReviewEnabled(boolean peerReviewEnabled) { this.peerReviewEnabled = peerReviewEnabled; }
    public Integer getPeerReviewCount() { return peerReviewCount; }
    public void setPeerReviewCount(Integer peerReviewCount) { this.peerReviewCount = peerReviewCount; }
    public Float getPeerAverageScore() { return peerAverageScore; }
    public void setPeerAverageScore(Float peerAverageScore) { this.peerAverageScore = peerAverageScore; }
    public String getRubricScores() { return rubricScores; }
    public void setRubricScores(String rubricScores) { this.rubricScores = rubricScores; }
    public List<ReviewComment> getComments() { return comments; }
    public void setComments(List<ReviewComment> comments) { this.comments = comments; }
    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
