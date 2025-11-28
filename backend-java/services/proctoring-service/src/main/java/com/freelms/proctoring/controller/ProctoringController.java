package com.freelms.proctoring.controller;

import com.freelms.proctoring.entity.ProctoringSession;
import com.freelms.proctoring.entity.ProctoringViolation;
import com.freelms.proctoring.service.ProctoringService;
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

/**
 * REST controller for proctoring operations.
 */
@RestController
@RequestMapping("/api/v1/proctoring")
@Tag(name = "Proctoring", description = "Online exam proctoring and monitoring")
public class ProctoringController {

    private final ProctoringService proctoringService;

    public ProctoringController(ProctoringService proctoringService) {
        this.proctoringService = proctoringService;
    }

    @PostMapping("/sessions")
    @Operation(summary = "Create session", description = "Create a new proctoring session")
    public ResponseEntity<ProctoringSession> createSession(
            @RequestParam Long examId,
            @RequestParam Integer durationMinutes,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-Organization-Id", required = false) Long organizationId) {
        return ResponseEntity.ok(proctoringService.createSession(examId, userId, organizationId, durationMinutes));
    }

    @PostMapping(value = "/sessions/{sessionId}/verify-identity", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Verify identity", description = "Verify user identity with photo and ID")
    public ResponseEntity<Map<String, Object>> verifyIdentity(
            @PathVariable UUID sessionId,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "idDocument", required = false) MultipartFile idDocument) {
        return ResponseEntity.ok(proctoringService.verifyIdentity(sessionId, photo, idDocument));
    }

    @PostMapping("/sessions/{sessionId}/environment-check")
    @Operation(summary = "Environment check", description = "Perform environment validation")
    public ResponseEntity<Map<String, Object>> environmentCheck(
            @PathVariable UUID sessionId,
            @RequestBody Map<String, Object> environmentData) {
        return ResponseEntity.ok(proctoringService.performEnvironmentCheck(sessionId, environmentData));
    }

    @PostMapping("/sessions/{sessionId}/start")
    @Operation(summary = "Start session", description = "Start the proctoring session")
    public ResponseEntity<ProctoringSession> startSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(proctoringService.startSession(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/end")
    @Operation(summary = "End session", description = "End the proctoring session")
    public ResponseEntity<ProctoringSession> endSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(proctoringService.endSession(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/terminate")
    @Operation(summary = "Terminate session", description = "Terminate session due to violations")
    public ResponseEntity<ProctoringSession> terminateSession(
            @PathVariable UUID sessionId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(proctoringService.terminateSession(sessionId, body.get("reason")));
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get session", description = "Get proctoring session details")
    public ResponseEntity<ProctoringSession> getSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(proctoringService.getSession(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/violations")
    @Operation(summary = "Record violation", description = "Record a proctoring violation")
    public ResponseEntity<ProctoringViolation> recordViolation(
            @PathVariable UUID sessionId,
            @RequestBody Map<String, Object> violationData) {
        return ResponseEntity.ok(proctoringService.recordViolation(
                sessionId,
                ProctoringViolation.ViolationType.valueOf((String) violationData.get("type")),
                ProctoringViolation.Severity.valueOf((String) violationData.get("severity")),
                (String) violationData.get("description"),
                violationData.get("confidenceScore") != null ?
                        ((Number) violationData.get("confidenceScore")).floatValue() : null,
                violationData.get("timestamp") != null ?
                        ((Number) violationData.get("timestamp")).longValue() : null
        ));
    }

    @GetMapping("/sessions/{sessionId}/violations")
    @Operation(summary = "Get violations", description = "Get session violations")
    public ResponseEntity<List<ProctoringViolation>> getViolations(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(proctoringService.getSessionViolations(sessionId));
    }

    @GetMapping("/reviews/pending")
    @Operation(summary = "Pending reviews", description = "Get sessions needing review")
    public ResponseEntity<Page<ProctoringSession>> getPendingReviews(Pageable pageable) {
        return ResponseEntity.ok(proctoringService.getPendingReviews(pageable));
    }

    @PostMapping("/sessions/{sessionId}/review")
    @Operation(summary = "Submit review", description = "Submit review decision")
    public ResponseEntity<ProctoringSession> submitReview(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-Id") Long reviewerId,
            @RequestBody Map<String, String> reviewData) {
        return ResponseEntity.ok(proctoringService.submitReview(
                sessionId,
                reviewerId,
                ProctoringSession.ReviewDecision.valueOf(reviewData.get("decision")),
                reviewData.get("notes")
        ));
    }
}
