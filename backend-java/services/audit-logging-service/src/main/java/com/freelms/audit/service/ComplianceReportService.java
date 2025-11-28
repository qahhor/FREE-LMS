package com.freelms.audit.service;

import com.freelms.audit.entity.AuditEvent;
import com.freelms.audit.repository.AuditEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for generating compliance reports (SOC2, GDPR, etc.).
 */
@Service
public class ComplianceReportService {

    private final AuditEventRepository eventRepository;

    public ComplianceReportService(AuditEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Generate SOC2 Type II report.
     */
    public Map<String, Object> generateSoc2Report(Long organizationId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "SOC2_TYPE_II");
        report.put("organizationId", organizationId);
        report.put("periodStart", start);
        report.put("periodEnd", end);
        report.put("generatedAt", LocalDateTime.now());

        // Security events
        List<AuditEvent> securityEvents = eventRepository.findSecurityEvents(start);
        report.put("securityIncidents", securityEvents.size());

        // Access control
        Map<String, Object> accessControl = new HashMap<>();
        long accessDenied = securityEvents.stream()
                .filter(e -> e.getEventType() == AuditEvent.EventType.ACCESS_DENIED)
                .count();
        accessControl.put("accessDeniedCount", accessDenied);
        accessControl.put("privilegeEscalationAttempts",
                securityEvents.stream()
                        .filter(e -> e.getEventType() == AuditEvent.EventType.PRIVILEGE_ESCALATION)
                        .count());
        report.put("accessControl", accessControl);

        // Authentication metrics
        Map<String, Object> authMetrics = new HashMap<>();
        List<Object[]> byType = eventRepository.countByEventType(organizationId, start);
        byType.stream()
                .filter(row -> row[0].toString().contains("LOGIN"))
                .forEach(row -> authMetrics.put(row[0].toString(), row[1]));
        report.put("authentication", authMetrics);

        // Data integrity
        Map<String, Object> dataIntegrity = new HashMap<>();
        List<AuditEvent> dataOps = eventRepository.findDataOperations(start);
        dataIntegrity.put("dataModifications", dataOps.size());
        dataIntegrity.put("dataExports", dataOps.stream()
                .filter(e -> e.getEventType() == AuditEvent.EventType.DATA_EXPORTED).count());
        dataIntegrity.put("dataDeletions", dataOps.stream()
                .filter(e -> e.getEventType() == AuditEvent.EventType.DATA_DELETED).count());
        report.put("dataIntegrity", dataIntegrity);

        // Availability
        Map<String, Object> availability = new HashMap<>();
        long systemEvents = byType.stream()
                .filter(row -> row[0].toString().contains("SYSTEM") ||
                               row[0].toString().contains("MAINTENANCE"))
                .mapToLong(row -> (Long) row[1])
                .sum();
        availability.put("systemEvents", systemEvents);
        report.put("availability", availability);

        return report;
    }

    /**
     * Generate GDPR compliance report.
     */
    public Map<String, Object> generateGdprReport(Long organizationId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "GDPR_COMPLIANCE");
        report.put("organizationId", organizationId);
        report.put("periodStart", start);
        report.put("periodEnd", end);
        report.put("generatedAt", LocalDateTime.now());

        // Data subject requests
        Map<String, Object> dataSubjectRequests = new HashMap<>();
        // In real implementation, count actual DSAR requests
        dataSubjectRequests.put("accessRequests", 0);
        dataSubjectRequests.put("rectificationRequests", 0);
        dataSubjectRequests.put("erasureRequests", 0);
        dataSubjectRequests.put("portabilityRequests", 0);
        report.put("dataSubjectRequests", dataSubjectRequests);

        // Consent tracking
        List<Object[]> byType = eventRepository.countByEventType(organizationId, start);
        Map<String, Object> consent = new HashMap<>();
        byType.stream()
                .filter(row -> row[0].toString().contains("CONSENT"))
                .forEach(row -> consent.put(row[0].toString(), row[1]));
        report.put("consent", consent);

        // Data breaches
        List<AuditEvent> securityEvents = eventRepository.findSecurityEvents(start);
        long breaches = securityEvents.stream()
                .filter(e -> e.getSeverity() == AuditEvent.Severity.CRITICAL)
                .count();
        report.put("dataBreaches", breaches);

        // PII handling
        Map<String, Object> piiHandling = new HashMap<>();
        List<AuditEvent> dataOps = eventRepository.findDataOperations(start);
        piiHandling.put("piiAccessed", dataOps.stream()
                .filter(e -> e.getContainsPii() != null && e.getContainsPii()).count());
        piiHandling.put("piiExported", dataOps.stream()
                .filter(e -> e.getDataExported() != null && e.getDataExported() &&
                             e.getContainsPii() != null && e.getContainsPii()).count());
        report.put("piiHandling", piiHandling);

        // Cross-border transfers
        Map<String, Object> transfers = new HashMap<>();
        // In real implementation, track data transfers by region
        transfers.put("euToEu", 0);
        transfers.put("euToUs", 0);
        transfers.put("euToOther", 0);
        report.put("crossBorderTransfers", transfers);

        return report;
    }

    /**
     * Generate security audit report.
     */
    public Map<String, Object> generateSecurityAuditReport(Long organizationId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "SECURITY_AUDIT");
        report.put("organizationId", organizationId);
        report.put("periodDays", days);
        report.put("generatedAt", LocalDateTime.now());

        // Failed login analysis
        List<AuditEvent> securityEvents = eventRepository.findSecurityEvents(since);
        Map<String, Object> loginAnalysis = new HashMap<>();
        long failedLogins = securityEvents.stream()
                .filter(e -> e.getEventType() == AuditEvent.EventType.LOGIN_FAILED)
                .count();
        loginAnalysis.put("failedAttempts", failedLogins);

        // Group by IP
        Map<String, Long> byIp = new HashMap<>();
        securityEvents.stream()
                .filter(e -> e.getEventType() == AuditEvent.EventType.LOGIN_FAILED)
                .filter(e -> e.getIpAddress() != null)
                .forEach(e -> byIp.merge(e.getIpAddress(), 1L, Long::sum));
        loginAnalysis.put("byIpAddress", byIp);
        report.put("loginAnalysis", loginAnalysis);

        // Privilege escalation attempts
        long escalationAttempts = securityEvents.stream()
                .filter(e -> e.getEventType() == AuditEvent.EventType.PRIVILEGE_ESCALATION)
                .count();
        report.put("privilegeEscalationAttempts", escalationAttempts);

        // Suspicious activity
        long suspiciousActivity = securityEvents.stream()
                .filter(e -> e.getEventType() == AuditEvent.EventType.SUSPICIOUS_ACTIVITY)
                .count();
        report.put("suspiciousActivityCount", suspiciousActivity);

        // Rate limiting events
        long rateLimitEvents = securityEvents.stream()
                .filter(e -> e.getEventType() == AuditEvent.EventType.RATE_LIMIT_EXCEEDED)
                .count();
        report.put("rateLimitExceeded", rateLimitEvents);

        // Critical events
        List<AuditEvent> criticalEvents = securityEvents.stream()
                .filter(e -> e.getSeverity() == AuditEvent.Severity.CRITICAL)
                .toList();
        report.put("criticalEventsCount", criticalEvents.size());

        // Recent errors
        List<AuditEvent> errors = eventRepository.findRecentErrors(since);
        report.put("errorCount", errors.size());

        return report;
    }

    /**
     * Generate user activity report for compliance.
     */
    public Map<String, Object> generateUserActivityReport(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "USER_ACTIVITY");
        report.put("userId", userId);
        report.put("periodDays", days);
        report.put("generatedAt", now);

        List<AuditEvent> activity = eventRepository.findUserActivityInRange(userId, since, now);
        report.put("totalEvents", activity.size());

        // By category
        Map<String, Long> byCategory = new HashMap<>();
        activity.forEach(e -> byCategory.merge(e.getCategory().name(), 1L, Long::sum));
        report.put("byCategory", byCategory);

        // By event type
        Map<String, Long> byType = new HashMap<>();
        activity.forEach(e -> byType.merge(e.getEventType().name(), 1L, Long::sum));
        report.put("byEventType", byType);

        // IP addresses
        List<String> ipAddresses = eventRepository.findUserIpAddresses(userId, since);
        report.put("uniqueIpAddresses", ipAddresses.size());
        report.put("ipAddresses", ipAddresses);

        // PII access
        List<AuditEvent> piiAccess = eventRepository.findPiiAccessByUser(userId);
        report.put("piiAccessCount", piiAccess.size());

        // Failed actions
        long failedActions = activity.stream()
                .filter(e -> e.getResult() == AuditEvent.ActionResult.FAILURE ||
                             e.getResult() == AuditEvent.ActionResult.DENIED)
                .count();
        report.put("failedActions", failedActions);

        return report;
    }
}
