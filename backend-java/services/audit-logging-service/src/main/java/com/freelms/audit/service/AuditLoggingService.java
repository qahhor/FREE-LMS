package com.freelms.audit.service;

import com.freelms.audit.entity.AuditEvent;
import com.freelms.audit.entity.RetentionPolicy;
import com.freelms.audit.repository.AuditEventRepository;
import com.freelms.audit.repository.RetentionPolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Core service for audit logging operations.
 */
@Service
public class AuditLoggingService {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggingService.class);

    private final AuditEventRepository eventRepository;
    private final RetentionPolicyRepository policyRepository;

    public AuditLoggingService(AuditEventRepository eventRepository,
                                RetentionPolicyRepository policyRepository) {
        this.eventRepository = eventRepository;
        this.policyRepository = policyRepository;
    }

    // ==================== Event Logging ====================

    /**
     * Log an audit event.
     */
    @Transactional
    public AuditEvent logEvent(AuditEvent event) {
        // Apply retention policy
        applyRetentionPolicy(event);

        return eventRepository.save(event);
    }

    /**
     * Log event asynchronously for high-throughput scenarios.
     */
    @Async
    @Transactional
    public void logEventAsync(AuditEvent event) {
        try {
            logEvent(event);
        } catch (Exception e) {
            log.error("Failed to log audit event: {}", e.getMessage());
        }
    }

    /**
     * Build and log an event with fluent API.
     */
    @Transactional
    public AuditEvent log(AuditEvent.EventType eventType, String action) {
        AuditEvent event = new AuditEvent();
        event.setEventType(eventType);
        event.setAction(action);
        event.setCategory(categorizeEvent(eventType));
        return logEvent(event);
    }

    // ==================== Event Queries ====================

    public AuditEvent getEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Audit event not found: " + eventId));
    }

    public Page<AuditEvent> getEvents(Long organizationId, Pageable pageable) {
        if (organizationId != null) {
            return eventRepository.findByOrganizationId(organizationId, pageable);
        }
        return eventRepository.findAll(pageable);
    }

    public Page<AuditEvent> getEventsByType(AuditEvent.EventType type, Pageable pageable) {
        return eventRepository.findByEventType(type, pageable);
    }

    public Page<AuditEvent> getEventsByCategory(AuditEvent.EventCategory category, Pageable pageable) {
        return eventRepository.findByCategory(category, pageable);
    }

    public Page<AuditEvent> getEventsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return eventRepository.findByDateRange(start, end, pageable);
    }

    public Page<AuditEvent> getUserActivity(Long userId, Pageable pageable) {
        return eventRepository.findByActorId(userId, pageable);
    }

    public List<AuditEvent> getUserActivityInRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return eventRepository.findUserActivityInRange(userId, start, end);
    }

    public List<AuditEvent> getResourceHistory(String resourceType, String resourceId) {
        return eventRepository.findByResource(resourceType, resourceId);
    }

    // ==================== Security & Compliance ====================

    public List<AuditEvent> getSecurityEvents(LocalDateTime since) {
        return eventRepository.findSecurityEvents(since);
    }

    public List<AuditEvent> getRecentErrors(LocalDateTime since) {
        return eventRepository.findRecentErrors(since);
    }

    public List<AuditEvent> getFailedLoginAttempts(Long userId, LocalDateTime since) {
        return eventRepository.findFailedLoginAttempts(userId, since);
    }

    public List<AuditEvent> getPiiAccessByUser(Long userId) {
        return eventRepository.findPiiAccessByUser(userId);
    }

    public List<AuditEvent> getDataOperations(LocalDateTime since) {
        return eventRepository.findDataOperations(since);
    }

    // ==================== Statistics ====================

    public Map<String, Object> getStatistics(Long organizationId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Map<String, Object> stats = new HashMap<>();

        // Count by event type
        Map<String, Long> byType = new HashMap<>();
        eventRepository.countByEventType(organizationId, since)
                .forEach(row -> byType.put(row[0].toString(), (Long) row[1]));
        stats.put("byEventType", byType);

        // Count by category
        Map<String, Long> byCategory = new HashMap<>();
        eventRepository.countByCategory(organizationId, since)
                .forEach(row -> byCategory.put(row[0].toString(), (Long) row[1]));
        stats.put("byCategory", byCategory);

        // Count by day
        Map<String, Long> byDay = new LinkedHashMap<>();
        eventRepository.countByDay(since)
                .forEach(row -> byDay.put(row[0].toString(), (Long) row[1]));
        stats.put("byDay", byDay);

        return stats;
    }

    // ==================== GDPR Support ====================

    /**
     * Generate data subject access report (GDPR Article 15).
     */
    public Map<String, Object> generateDataSubjectReport(Long userId) {
        Map<String, Object> report = new HashMap<>();
        LocalDateTime yearAgo = LocalDateTime.now().minusYears(1);

        report.put("userId", userId);
        report.put("generatedAt", LocalDateTime.now());

        // Activity summary
        List<AuditEvent> activity = eventRepository.findUserActivityInRange(userId, yearAgo, LocalDateTime.now());
        report.put("totalEvents", activity.size());

        // IP addresses used
        List<String> ipAddresses = eventRepository.findUserIpAddresses(userId, yearAgo);
        report.put("ipAddresses", ipAddresses);

        // PII access
        List<AuditEvent> piiAccess = eventRepository.findPiiAccessByUser(userId);
        report.put("piiAccessCount", piiAccess.size());

        // Login history
        long loginCount = activity.stream()
                .filter(e -> e.getEventType() == AuditEvent.EventType.LOGIN)
                .count();
        report.put("loginCount", loginCount);

        // Data exports/deletions
        long dataExports = activity.stream()
                .filter(e -> e.getDataExported() != null && e.getDataExported())
                .count();
        report.put("dataExportCount", dataExports);

        return report;
    }

    /**
     * Export user data for portability (GDPR Article 20).
     */
    public List<AuditEvent> exportUserData(Long userId) {
        return eventRepository.findUserActivityInRange(
                userId,
                LocalDateTime.now().minusYears(7),
                LocalDateTime.now()
        );
    }

    // ==================== Retention Management ====================

    @Transactional
    public RetentionPolicy createRetentionPolicy(RetentionPolicy policy) {
        return policyRepository.save(policy);
    }

    public List<RetentionPolicy> getRetentionPolicies(Long organizationId) {
        if (organizationId != null) {
            return policyRepository.findByOrganizationId(organizationId);
        }
        return policyRepository.findAll();
    }

    @Transactional
    public void applyRetentionPolicies() {
        LocalDateTime now = LocalDateTime.now();
        List<RetentionPolicy> policies = policyRepository.findPoliciesDueForExecution(now);

        for (RetentionPolicy policy : policies) {
            try {
                executeRetentionPolicy(policy);
                policy.setLastExecuted(now);
                policy.setNextExecution(calculateNextExecution(policy));
                policyRepository.save(policy);
            } catch (Exception e) {
                log.error("Failed to execute retention policy {}: {}", policy.getName(), e.getMessage());
            }
        }
    }

    @Transactional
    public int cleanupExpiredEvents() {
        // First archive events that need archiving
        List<AuditEvent> expired = eventRepository.findExpiredEvents(LocalDateTime.now());
        List<UUID> toArchive = expired.stream()
                .filter(e -> !e.getArchived())
                .map(AuditEvent::getId)
                .toList();

        if (!toArchive.isEmpty()) {
            eventRepository.archiveEvents(toArchive);
            log.info("Archived {} audit events", toArchive.size());
        }

        // Delete already archived and expired events
        int deleted = eventRepository.deleteArchivedExpiredEvents(LocalDateTime.now());
        log.info("Deleted {} archived audit events", deleted);

        return deleted;
    }

    // ==================== Helpers ====================

    private void applyRetentionPolicy(AuditEvent event) {
        List<RetentionPolicy> policies = policyRepository.findApplicablePolicies(
                event.getCategory(),
                event.getOrganizationId()
        );

        if (!policies.isEmpty()) {
            // Apply the policy with the shortest retention period
            int minDays = policies.stream()
                    .mapToInt(RetentionPolicy::getRetentionDays)
                    .min()
                    .orElse(365);

            event.setRetentionUntil(LocalDateTime.now().plusDays(minDays));
        } else {
            // Default 1 year retention
            event.setRetentionUntil(LocalDateTime.now().plusYears(1));
        }
    }

    private void executeRetentionPolicy(RetentionPolicy policy) {
        log.info("Executing retention policy: {}", policy.getName());
        // Implementation would archive/delete events based on policy rules
    }

    private LocalDateTime calculateNextExecution(RetentionPolicy policy) {
        // Default: daily execution
        return LocalDateTime.now().plusDays(1);
    }

    private AuditEvent.EventCategory categorizeEvent(AuditEvent.EventType eventType) {
        return switch (eventType) {
            case LOGIN, LOGOUT, LOGIN_FAILED, PASSWORD_CHANGED, MFA_ENABLED, MFA_DISABLED,
                 SESSION_CREATED, SESSION_EXPIRED, TOKEN_REFRESHED -> AuditEvent.EventCategory.AUTHENTICATION;

            case PERMISSION_GRANTED, PERMISSION_REVOKED, ROLE_ASSIGNED, ROLE_REMOVED,
                 ACCESS_DENIED, PRIVILEGE_ESCALATION -> AuditEvent.EventCategory.AUTHORIZATION;

            case DATA_READ -> AuditEvent.EventCategory.DATA_ACCESS;

            case DATA_CREATED, DATA_UPDATED, DATA_DELETED, DATA_EXPORTED,
                 DATA_IMPORTED, DATA_ARCHIVED -> AuditEvent.EventCategory.DATA_MODIFICATION;

            case USER_CREATED, USER_UPDATED, USER_DELETED, USER_SUSPENDED, USER_ACTIVATED,
                 PROFILE_UPDATED, SETTINGS_CHANGED -> AuditEvent.EventCategory.USER_MANAGEMENT;

            case COURSE_CREATED, COURSE_UPDATED, COURSE_DELETED, COURSE_PUBLISHED,
                 ENROLLMENT_CREATED, ENROLLMENT_COMPLETED, ENROLLMENT_DROPPED -> AuditEvent.EventCategory.COURSE_MANAGEMENT;

            case CONTENT_CREATED, CONTENT_UPDATED, CONTENT_DELETED, CONTENT_PUBLISHED,
                 FILE_UPLOADED, FILE_DOWNLOADED, FILE_DELETED -> AuditEvent.EventCategory.CONTENT_MANAGEMENT;

            case QUIZ_STARTED, QUIZ_COMPLETED, ASSIGNMENT_SUBMITTED, GRADE_ASSIGNED -> AuditEvent.EventCategory.ASSESSMENT;

            case SYSTEM_STARTED, SYSTEM_STOPPED, CONFIG_CHANGED, BACKUP_CREATED,
                 BACKUP_RESTORED, MAINTENANCE_STARTED, MAINTENANCE_COMPLETED -> AuditEvent.EventCategory.SYSTEM;

            case SECURITY_ALERT, INTRUSION_DETECTED, VULNERABILITY_FOUND,
                 SUSPICIOUS_ACTIVITY, RATE_LIMIT_EXCEEDED -> AuditEvent.EventCategory.SECURITY;

            case CONSENT_GIVEN, CONSENT_WITHDRAWN, DATA_REQUEST_RECEIVED,
                 DATA_REQUEST_COMPLETED, RETENTION_POLICY_APPLIED -> AuditEvent.EventCategory.COMPLIANCE;

            case API_KEY_CREATED, API_KEY_REVOKED, WEBHOOK_TRIGGERED,
                 INTEGRATION_CONNECTED, INTEGRATION_DISCONNECTED -> AuditEvent.EventCategory.INTEGRATION;
        };
    }
}
