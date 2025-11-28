package com.freelms.assignment.controller;

import com.freelms.assignment.entity.AssignmentSubmission;
import com.freelms.assignment.entity.ReviewComment;
import com.freelms.assignment.service.AssignmentReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assignments")
@Tag(name = "Assignment Review", description = "Assignment submission and grading")
public class AssignmentReviewController {

    private final AssignmentReviewService reviewService;

    public AssignmentReviewController(AssignmentReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit assignment")
    public ResponseEntity<AssignmentSubmission> submitAssignment(
            @RequestParam Long assignmentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<MultipartFile> files,
            @RequestParam(defaultValue = "100") Float maxScore,
            @RequestParam(defaultValue = "false") boolean checkPlagiarism,
            @RequestHeader("X-User-Id") Long studentId) {
        return ResponseEntity.ok(reviewService.submitAssignment(
                assignmentId, studentId, courseId, text, files, maxScore, checkPlagiarism));
    }

    @GetMapping("/submissions/{submissionId}")
    @Operation(summary = "Get submission")
    public ResponseEntity<AssignmentSubmission> getSubmission(@PathVariable UUID submissionId) {
        return ResponseEntity.ok(reviewService.getSubmission(submissionId));
    }

    @PostMapping("/submissions/{submissionId}/grade")
    @Operation(summary = "Grade submission")
    public ResponseEntity<AssignmentSubmission> gradeSubmission(
            @PathVariable UUID submissionId,
            @RequestBody Map<String, Object> gradeData,
            @RequestHeader("X-User-Id") Long graderId) {
        return ResponseEntity.ok(reviewService.gradeSubmission(
                submissionId,
                graderId,
                ((Number) gradeData.get("score")).floatValue(),
                (String) gradeData.get("grade"),
                (String) gradeData.get("feedback"),
                (Map<String, Float>) gradeData.get("rubricScores")
        ));
    }

    @PostMapping("/submissions/{submissionId}/comments")
    @Operation(summary = "Add comment")
    public ResponseEntity<ReviewComment> addComment(
            @PathVariable UUID submissionId,
            @RequestBody Map<String, Object> commentData,
            @RequestHeader("X-User-Id") Long reviewerId) {
        return ResponseEntity.ok(reviewService.addComment(
                submissionId,
                reviewerId,
                ReviewComment.ReviewerType.valueOf((String) commentData.getOrDefault("reviewerType", "INSTRUCTOR")),
                (String) commentData.get("text"),
                commentData.get("lineNumber") != null ? ((Number) commentData.get("lineNumber")).intValue() : null,
                (String) commentData.get("filePath"),
                (String) commentData.get("rubricCriterion")
        ));
    }

    @GetMapping("/submissions/{submissionId}/comments")
    @Operation(summary = "Get comments")
    public ResponseEntity<List<ReviewComment>> getComments(@PathVariable UUID submissionId) {
        return ResponseEntity.ok(reviewService.getComments(submissionId));
    }

    @PostMapping("/submissions/{submissionId}/auto-grade")
    @Operation(summary = "Auto-grade submission")
    public ResponseEntity<AssignmentSubmission> autoGrade(@PathVariable UUID submissionId) {
        return ResponseEntity.ok(reviewService.autoGrade(submissionId));
    }

    @GetMapping("/queue")
    @Operation(summary = "Get grading queue")
    public ResponseEntity<Page<AssignmentSubmission>> getGradingQueue(
            @RequestParam(required = false) Long courseId,
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.getGradingQueue(courseId, pageable));
    }

    @GetMapping("/{assignmentId}/stats")
    @Operation(summary = "Get assignment statistics")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(reviewService.getAssignmentStats(assignmentId));
    }
}
