package com.freelms.assignment.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a comment on a submission.
 */
@Entity
@Table(name = "review_comments")
public class ReviewComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private AssignmentSubmission submission;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reviewer_type")
    private ReviewerType reviewerType;

    @Column(name = "comment_text", columnDefinition = "TEXT", nullable = false)
    private String commentText;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "rubric_criterion")
    private String rubricCriterion;

    @Column(name = "is_private")
    private boolean isPrivate = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum ReviewerType {
        INSTRUCTOR,
        TA,
        PEER,
        AUTO_GRADER
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AssignmentSubmission getSubmission() { return submission; }
    public void setSubmission(AssignmentSubmission submission) { this.submission = submission; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public ReviewerType getReviewerType() { return reviewerType; }
    public void setReviewerType(ReviewerType reviewerType) { this.reviewerType = reviewerType; }
    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getRubricCriterion() { return rubricCriterion; }
    public void setRubricCriterion(String rubricCriterion) { this.rubricCriterion = rubricCriterion; }
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
