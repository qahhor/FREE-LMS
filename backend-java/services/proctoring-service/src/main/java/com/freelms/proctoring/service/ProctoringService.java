package com.freelms.proctoring.service;

import com.freelms.proctoring.entity.ProctoringSession;
import com.freelms.proctoring.entity.ProctoringViolation;
import com.freelms.proctoring.repository.ProctoringSessionRepository;
import com.freelms.proctoring.repository.ProctoringViolationRepository;
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
 * Service for proctoring operations.
 */
@Service
public class ProctoringService {

    private static final Logger log = LoggerFactory.getLogger(ProctoringService.class);

    private final ProctoringSessionRepository sessionRepository;
    private final ProctoringViolationRepository violationRepository;
    private final FaceDetectionService faceDetectionService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProctoringService(
            ProctoringSessionRepository sessionRepository,
            ProctoringViolationRepository violationRepository,
            FaceDetectionService faceDetectionService,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.sessionRepository = sessionRepository;
        this.violationRepository = violationRepository;
        this.faceDetectionService = faceDetectionService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Create a new proctoring session.
     */
    @Transactional
    public ProctoringSession createSession(Long examId, Long userId, Long organizationId, Integer durationMinutes) {
        log.info("Creating proctoring session for exam {} user {}", examId, userId);

        // Check if session already exists
        Optional<ProctoringSession> existing = sessionRepository.findByExamIdAndUserId(examId, userId);
        if (existing.isPresent() && existing.get().getStatus() != ProctoringSession.SessionStatus.FAILED) {
            throw new RuntimeException("Proctoring session already exists");
        }

        ProctoringSession session = new ProctoringSession();
        session.setExamId(examId);
        session.setUserId(userId);
        session.setOrganizationId(organizationId);
        session.setScheduledDurationMinutes(durationMinutes);
        session.setStatus(ProctoringSession.SessionStatus.PENDING);

        ProctoringSession saved = sessionRepository.save(session);

        publishEvent("SESSION_CREATED", saved);

        return saved;
    }

    /**
     * Verify user identity.
     */
    @Transactional
    public Map<String, Object> verifyIdentity(UUID sessionId, MultipartFile photo, MultipartFile idDocument) {
        ProctoringSession session = getSession(sessionId);

        session.setStatus(ProctoringSession.SessionStatus.IDENTITY_CHECK);

        // Store photos
        String photoPath = storeFile(photo, sessionId, "identity_photo");
        String idPath = idDocument != null ? storeFile(idDocument, sessionId, "id_document") : null;

        session.setIdentityPhotoPath(photoPath);
        session.setIdDocumentPath(idPath);

        // Perform face detection and matching
        FaceDetectionService.FaceDetectionResult result = faceDetectionService.detectAndMatchFace(photoPath, idPath);

        session.setFaceMatchScore(result.getMatchScore());
        session.setIdentityVerified(result.isVerified());
        session.setIdentityVerificationTime(LocalDateTime.now());

        sessionRepository.save(session);

        Map<String, Object> response = new HashMap<>();
        response.put("verified", result.isVerified());
        response.put("matchScore", result.getMatchScore());
        response.put("message", result.isVerified() ? "Identity verified successfully" : "Identity verification failed");

        return response;
    }

    /**
     * Perform environment check.
     */
    @Transactional
    public Map<String, Object> performEnvironmentCheck(UUID sessionId, Map<String, Object> environmentData) {
        ProctoringSession session = getSession(sessionId);

        session.setStatus(ProctoringSession.SessionStatus.ENVIRONMENT_CHECK);

        // Validate environment
        boolean webcamOk = (Boolean) environmentData.getOrDefault("webcamEnabled", false);
        boolean microphoneOk = (Boolean) environmentData.getOrDefault("microphoneEnabled", false);
        boolean screenShareOk = (Boolean) environmentData.getOrDefault("screenShareEnabled", false);

        session.setWebcamEnabled(webcamOk);
        session.setMicrophoneEnabled(microphoneOk);
        session.setScreenShareEnabled(screenShareOk);

        List<String> issues = new ArrayList<>();
        if (!webcamOk) issues.add("Webcam not enabled");
        if (!microphoneOk) issues.add("Microphone not enabled");
        if (!screenShareOk) issues.add("Screen share not enabled");

        // Check for prohibited software, VMs, etc.
        if (environmentData.containsKey("runningProcesses")) {
            List<String> processes = (List<String>) environmentData.get("runningProcesses");
            List<String> prohibited = checkProhibitedProcesses(processes);
            if (!prohibited.isEmpty()) {
                issues.add("Prohibited software detected: " + String.join(", ", prohibited));
            }
        }

        boolean passed = issues.isEmpty();
        session.setEnvironmentChecked(passed);

        sessionRepository.save(session);

        Map<String, Object> response = new HashMap<>();
        response.put("passed", passed);
        response.put("issues", issues);

        return response;
    }

    /**
     * Start proctoring session.
     */
    @Transactional
    public ProctoringSession startSession(UUID sessionId) {
        ProctoringSession session = getSession(sessionId);

        if (!session.isIdentityVerified()) {
            throw new RuntimeException("Identity not verified");
        }
        if (!session.isEnvironmentChecked()) {
            throw new RuntimeException("Environment check not completed");
        }

        session.setStatus(ProctoringSession.SessionStatus.IN_PROGRESS);
        session.setStartTime(LocalDateTime.now());
        session.setBrowserLockdownActive(true);

        ProctoringSession saved = sessionRepository.save(session);

        publishEvent("SESSION_STARTED", saved);

        return saved;
    }

    /**
     * Record a violation during session.
     */
    @Transactional
    public ProctoringViolation recordViolation(UUID sessionId, ProctoringViolation.ViolationType type,
                                                ProctoringViolation.Severity severity, String description,
                                                Float confidenceScore, Long timestamp) {
        ProctoringSession session = getSession(sessionId);

        ProctoringViolation violation = new ProctoringViolation();
        violation.setSession(session);
        violation.setViolationType(type);
        violation.setSeverity(severity);
        violation.setDescription(description);
        violation.setConfidenceScore(confidenceScore);
        violation.setTimestampInRecording(timestamp);
        violation.setDetectedBy(ProctoringViolation.DetectionSource.AI);

        // Determine action based on severity and violation count
        ProctoringViolation.ActionTaken action = determineAction(session, severity);
        violation.setActionTaken(action);

        ProctoringViolation saved = violationRepository.save(violation);

        // Update session stats
        session.setViolationCount(session.getViolationCount() + 1);
        if (severity == ProctoringViolation.Severity.WARNING) {
            session.setWarningCount(session.getWarningCount() + 1);
        }

        // Recalculate risk score
        updateRiskScore(session);

        sessionRepository.save(session);

        // Handle action
        if (action == ProctoringViolation.ActionTaken.EXAM_TERMINATED) {
            terminateSession(sessionId, "Too many violations");
        }

        publishEvent("VIOLATION_RECORDED", Map.of(
                "sessionId", sessionId,
                "violationType", type,
                "severity", severity
        ));

        return saved;
    }

    /**
     * End proctoring session.
     */
    @Transactional
    public ProctoringSession endSession(UUID sessionId) {
        ProctoringSession session = getSession(sessionId);

        session.setStatus(ProctoringSession.SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        session.setBrowserLockdownActive(false);

        // Flag for review if high risk
        if (session.getRiskLevel() == ProctoringSession.RiskLevel.HIGH ||
            session.getRiskLevel() == ProctoringSession.RiskLevel.CRITICAL) {
            session.setRequiresManualReview(true);
        }

        ProctoringSession saved = sessionRepository.save(session);

        publishEvent("SESSION_COMPLETED", saved);

        return saved;
    }

    /**
     * Terminate session due to violations.
     */
    @Transactional
    public ProctoringSession terminateSession(UUID sessionId, String reason) {
        ProctoringSession session = getSession(sessionId);

        session.setStatus(ProctoringSession.SessionStatus.TERMINATED);
        session.setEndTime(LocalDateTime.now());
        session.setBrowserLockdownActive(false);
        session.setRequiresManualReview(true);

        ProctoringSession saved = sessionRepository.save(session);

        publishEvent("SESSION_TERMINATED", Map.of("sessionId", sessionId, "reason", reason));

        return saved;
    }

    /**
     * Submit review decision.
     */
    @Transactional
    public ProctoringSession submitReview(UUID sessionId, Long reviewerId,
                                           ProctoringSession.ReviewDecision decision, String notes) {
        ProctoringSession session = getSession(sessionId);

        session.setReviewedBy(reviewerId);
        session.setReviewedAt(LocalDateTime.now());
        session.setReviewDecision(decision);
        session.setReviewNotes(notes);

        ProctoringSession saved = sessionRepository.save(session);

        publishEvent("SESSION_REVIEWED", Map.of(
                "sessionId", sessionId,
                "decision", decision,
                "reviewerId", reviewerId
        ));

        return saved;
    }

    /**
     * Get session by ID.
     */
    public ProctoringSession getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
    }

    /**
     * Get sessions needing review.
     */
    public Page<ProctoringSession> getPendingReviews(Pageable pageable) {
        return sessionRepository.findPendingReviews(pageable);
    }

    /**
     * Get session violations.
     */
    public List<ProctoringViolation> getSessionViolations(UUID sessionId) {
        return violationRepository.findBySessionIdOrderByDetectedAtAsc(sessionId);
    }

    // Helper methods

    private String storeFile(MultipartFile file, UUID sessionId, String type) {
        // In real implementation, store to MinIO
        return String.format("/proctoring/%s/%s_%s", sessionId, type, file.getOriginalFilename());
    }

    private List<String> checkProhibitedProcesses(List<String> processes) {
        List<String> prohibited = new ArrayList<>();
        List<String> blacklist = List.of(
                "teamviewer", "anydesk", "vnc", "zoom", "skype", "discord",
                "obs", "camtasia", "screencast", "vmware", "virtualbox"
        );

        for (String process : processes) {
            String lower = process.toLowerCase();
            for (String blocked : blacklist) {
                if (lower.contains(blocked)) {
                    prohibited.add(process);
                    break;
                }
            }
        }
        return prohibited;
    }

    private ProctoringViolation.ActionTaken determineAction(ProctoringSession session,
                                                            ProctoringViolation.Severity severity) {
        int totalViolations = session.getViolationCount() + 1;

        if (severity == ProctoringViolation.Severity.CRITICAL) {
            return totalViolations >= 2 ?
                    ProctoringViolation.ActionTaken.EXAM_TERMINATED :
                    ProctoringViolation.ActionTaken.EXAM_PAUSED;
        }

        if (severity == ProctoringViolation.Severity.WARNING) {
            if (session.getWarningCount() >= 5) {
                return ProctoringViolation.ActionTaken.EXAM_TERMINATED;
            }
            return ProctoringViolation.ActionTaken.WARNING_SHOWN;
        }

        return ProctoringViolation.ActionTaken.FLAGGED_FOR_REVIEW;
    }

    private void updateRiskScore(ProctoringSession session) {
        float score = 0;

        // Base score from violations
        score += session.getViolationCount() * 5;
        score += session.getWarningCount() * 10;

        // Check for critical patterns
        List<ProctoringViolation> violations = violationRepository.findBySessionIdOrderByDetectedAtAsc(session.getId());
        for (ProctoringViolation v : violations) {
            if (v.getSeverity() == ProctoringViolation.Severity.CRITICAL) {
                score += 25;
            }
            if (v.getViolationType() == ProctoringViolation.ViolationType.MULTIPLE_FACES ||
                v.getViolationType() == ProctoringViolation.ViolationType.FACE_MISMATCH) {
                score += 20;
            }
        }

        // Cap at 100
        score = Math.min(score, 100);
        session.setRiskScore(score);

        // Set risk level
        if (score >= 75) {
            session.setRiskLevel(ProctoringSession.RiskLevel.CRITICAL);
        } else if (score >= 50) {
            session.setRiskLevel(ProctoringSession.RiskLevel.HIGH);
        } else if (score >= 25) {
            session.setRiskLevel(ProctoringSession.RiskLevel.MEDIUM);
        } else {
            session.setRiskLevel(ProctoringSession.RiskLevel.LOW);
        }
    }

    private void publishEvent(String eventType, Object data) {
        try {
            kafkaTemplate.send("proctoring-events", Map.of("eventType", eventType, "data", data));
        } catch (Exception e) {
            log.error("Failed to publish event: {}", e.getMessage());
        }
    }
}
