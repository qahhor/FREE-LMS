package com.freelms.assignment.service;

import com.freelms.assignment.entity.AssignmentSubmission;
import com.freelms.assignment.entity.ReviewComment;
import com.freelms.assignment.repository.AssignmentSubmissionRepository;
import com.freelms.assignment.repository.ReviewCommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for assignment review and grading operations.
 */
@Service
public class AssignmentReviewService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentReviewService.class);

    private final AssignmentSubmissionRepository submissionRepository;
    private final ReviewCommentRepository commentRepository;
    private final AutoGradingService autoGradingService;
    private final PlagiarismService plagiarismService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AssignmentReviewService(
            AssignmentSubmissionRepository submissionRepository,
            ReviewCommentRepository commentRepository,
            AutoGradingService autoGradingService,
            PlagiarismService plagiarismService,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.submissionRepository = submissionRepository;
        this.commentRepository = commentRepository;
        this.autoGradingService = autoGradingService;
        this.plagiarismService = plagiarismService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Submit assignment.
     */
    @Transactional
    public AssignmentSubmission submitAssignment(Long assignmentId, Long studentId, Long courseId,
                                                  String text, List<MultipartFile> files,
                                                  Float maxScore, boolean checkPlagiarism) {
        // Get previous attempts
        List<AssignmentSubmission> attempts = submissionRepository.findAllAttempts(assignmentId, studentId);
        int attemptNumber = attempts.isEmpty() ? 1 : attempts.get(0).getAttemptNumber() + 1;

        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(studentId);
        submission.setCourseId(courseId);
        submission.setSubmissionText(text);
        submission.setMaxScore(maxScore);
        submission.setAttemptNumber(attemptNumber);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(AssignmentSubmission.SubmissionStatus.SUBMITTED);

        // Store files
        if (files != null && !files.isEmpty()) {
            List<String> paths = new ArrayList<>();
            for (MultipartFile file : files) {
                String path = storeFile(file, submission.getId());
                paths.add(path);
            }
            submission.setFilePaths(String.join(",", paths));
        }

        AssignmentSubmission saved = submissionRepository.save(submission);

        // Trigger plagiarism check if enabled
        if (checkPlagiarism) {
            plagiarismService.checkPlagiarismAsync(saved.getId());
        }

        publishEvent("ASSIGNMENT_SUBMITTED", saved);

        return saved;
    }

    /**
     * Grade submission.
     */
    @Transactional
    public AssignmentSubmission gradeSubmission(UUID submissionId, Long graderId, Float score,
                                                 String grade, String feedback, Map<String, Float> rubricScores) {
        AssignmentSubmission submission = getSubmission(submissionId);

        submission.setScore(score);
        submission.setGrade(grade);
        submission.setFeedback(feedback);
        submission.setGradedBy(graderId);
        submission.setGradedAt(LocalDateTime.now());
        submission.setStatus(AssignmentSubmission.SubmissionStatus.GRADED);

        if (rubricScores != null) {
            submission.setRubricScores(new com.fasterxml.jackson.databind.ObjectMapper()
                    .valueToTree(rubricScores).toString());
        }

        AssignmentSubmission saved = submissionRepository.save(submission);

        publishEvent("ASSIGNMENT_GRADED", Map.of(
                "submissionId", submissionId,
                "studentId", submission.getStudentId(),
                "score", score,
                "grade", grade
        ));

        return saved;
    }

    /**
     * Add comment to submission.
     */
    @Transactional
    public ReviewComment addComment(UUID submissionId, Long reviewerId, ReviewComment.ReviewerType reviewerType,
                                    String text, Integer lineNumber, String filePath, String rubricCriterion) {
        AssignmentSubmission submission = getSubmission(submissionId);

        ReviewComment comment = new ReviewComment();
        comment.setSubmission(submission);
        comment.setReviewerId(reviewerId);
        comment.setReviewerType(reviewerType);
        comment.setCommentText(text);
        comment.setLineNumber(lineNumber);
        comment.setFilePath(filePath);
        comment.setRubricCriterion(rubricCriterion);

        return commentRepository.save(comment);
    }

    /**
     * Trigger auto-grading.
     */
    @Transactional
    public AssignmentSubmission autoGrade(UUID submissionId) {
        AssignmentSubmission submission = getSubmission(submissionId);

        AutoGradingService.AutoGradeResult result = autoGradingService.grade(submission);

        submission.setAutoGradeScore(result.getScore());
        submission.setAutoGradeFeedback(result.getFeedback());
        submission.setAutoGradedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    /**
     * Request resubmission.
     */
    @Transactional
    public AssignmentSubmission requestResubmission(UUID submissionId, Long reviewerId, String reason) {
        AssignmentSubmission submission = getSubmission(submissionId);

        submission.setStatus(AssignmentSubmission.SubmissionStatus.RESUBMISSION_REQUESTED);
        submission.setFeedback(reason);

        AssignmentSubmission saved = submissionRepository.save(submission);

        publishEvent("RESUBMISSION_REQUESTED", Map.of(
                "submissionId", submissionId,
                "studentId", submission.getStudentId(),
                "reason", reason
        ));

        return saved;
    }

    /**
     * Get submission queue for grading.
     */
    public Page<AssignmentSubmission> getGradingQueue(Long courseId, Pageable pageable) {
        if (courseId != null) {
            return submissionRepository.findPendingByCourse(courseId, pageable);
        }
        return submissionRepository.findPendingReview(pageable);
    }

    /**
     * Get submission.
     */
    public AssignmentSubmission getSubmission(UUID submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found: " + submissionId));
    }

    /**
     * Get submission comments.
     */
    public List<ReviewComment> getComments(UUID submissionId) {
        return commentRepository.findBySubmissionId(submissionId);
    }

    /**
     * Get assignment statistics.
     */
    public Map<String, Object> getAssignmentStats(Long assignmentId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageScore", submissionRepository.getAverageScore(assignmentId));
        stats.put("gradedCount", submissionRepository.countGraded(assignmentId));
        return stats;
    }

    private String storeFile(MultipartFile file, UUID submissionId) {
        // In real implementation, store to MinIO
        return String.format("/submissions/%s/%s", submissionId, file.getOriginalFilename());
    }

    private void publishEvent(String eventType, Object data) {
        try {
            kafkaTemplate.send("assignment-events", Map.of("eventType", eventType, "data", data));
        } catch (Exception e) {
            log.error("Failed to publish event: {}", e.getMessage());
        }
    }
}
