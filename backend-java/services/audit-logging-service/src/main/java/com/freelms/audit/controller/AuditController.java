package com.freelms.audit.controller;

import com.freelms.audit.entity.AuditEvent;
import com.freelms.audit.entity.RetentionPolicy;
import com.freelms.audit.service.AuditLoggingService;
import com.freelms.audit.service.ComplianceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit Logging", description = "Audit logging and compliance")
public class AuditController {

    private final AuditLoggingService auditService;
    private final ComplianceReportService reportService;

    public AuditController(AuditLoggingService auditService, ComplianceReportService reportService) {
        this.auditService = auditService;
        this.reportService = reportService;
    }

    // ==================== Event Endpoints ====================

    @PostMapping("/events")
    @Operation(summary = "Log an audit event")
    public ResponseEntity<AuditEvent> logEvent(@RequestBody AuditEvent event) {
        return ResponseEntity.ok(auditService.logEvent(event));
    }

    @GetMapping("/events/{eventId}")
    @Operation(summary = "Get audit event by ID")
    public ResponseEntity<AuditEvent> getEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(auditService.getEvent(eventId));
    }

    @GetMapping("/events")
    @Operation(summary = "List audit events")
    public ResponseEntity<Page<AuditEvent>> listEvents(
            @RequestParam(required = false) Long organizationId,
            Pageable pageable) {
        return ResponseEntity.ok(auditService.getEvents(organizationId, pageable));
    }

    @GetMapping("/events/by-type")
    @Operation(summary = "Get events by type")
    public ResponseEntity<Page<AuditEvent>> getEventsByType(
            @RequestParam AuditEvent.EventType type,
            Pageable pageable) {
        return ResponseEntity.ok(auditService.getEventsByType(type, pageable));
    }

    @GetMapping("/events/by-category")
    @Operation(summary = "Get events by category")
    public ResponseEntity<Page<AuditEvent>> getEventsByCategory(
            @RequestParam AuditEvent.EventCategory category,
            Pageable pageable) {
        return ResponseEntity.ok(auditService.getEventsByCategory(category, pageable));
    }

    @GetMapping("/events/by-date-range")
    @Operation(summary = "Get events by date range")
    public ResponseEntity<Page<AuditEvent>> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable) {
        return ResponseEntity.ok(auditService.getEventsByDateRange(start, end, pageable));
    }

    @GetMapping("/events/user/{userId}")
    @Operation(summary = "Get user activity")
    public ResponseEntity<Page<AuditEvent>> getUserActivity(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(auditService.getUserActivity(userId, pageable));
    }

    @GetMapping("/events/resource/{resourceType}/{resourceId}")
    @Operation(summary = "Get resource history")
    public ResponseEntity<List<AuditEvent>> getResourceHistory(
            @PathVariable String resourceType,
            @PathVariable String resourceId) {
        return ResponseEntity.ok(auditService.getResourceHistory(resourceType, resourceId));
    }

    // ==================== Security Endpoints ====================

    @GetMapping("/security/events")
    @Operation(summary = "Get security events")
    public ResponseEntity<List<AuditEvent>> getSecurityEvents(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(auditService.getSecurityEvents(LocalDateTime.now().minusDays(days)));
    }

    @GetMapping("/security/errors")
    @Operation(summary = "Get recent errors")
    public ResponseEntity<List<AuditEvent>> getRecentErrors(
            @RequestParam(defaultValue = "1") int days) {
        return ResponseEntity.ok(auditService.getRecentErrors(LocalDateTime.now().minusDays(days)));
    }

    @GetMapping("/security/failed-logins/{userId}")
    @Operation(summary = "Get failed login attempts for user")
    public ResponseEntity<List<AuditEvent>> getFailedLoginAttempts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(auditService.getFailedLoginAttempts(
                userId, LocalDateTime.now().minusDays(days)));
    }

    // ==================== Statistics Endpoints ====================

    @GetMapping("/statistics")
    @Operation(summary = "Get audit statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(required = false) Long organizationId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(auditService.getStatistics(organizationId, days));
    }

    // ==================== Compliance Reports ====================

    @GetMapping("/reports/soc2")
    @Operation(summary = "Generate SOC2 compliance report")
    public ResponseEntity<Map<String, Object>> getSoc2Report(
            @RequestParam Long organizationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.generateSoc2Report(organizationId, start, end));
    }

    @GetMapping("/reports/gdpr")
    @Operation(summary = "Generate GDPR compliance report")
    public ResponseEntity<Map<String, Object>> getGdprReport(
            @RequestParam Long organizationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.generateGdprReport(organizationId, start, end));
    }

    @GetMapping("/reports/security-audit")
    @Operation(summary = "Generate security audit report")
    public ResponseEntity<Map<String, Object>> getSecurityAuditReport(
            @RequestParam Long organizationId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(reportService.generateSecurityAuditReport(organizationId, days));
    }

    @GetMapping("/reports/user-activity/{userId}")
    @Operation(summary = "Generate user activity report")
    public ResponseEntity<Map<String, Object>> getUserActivityReport(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(reportService.generateUserActivityReport(userId, days));
    }

    // ==================== GDPR Endpoints ====================

    @GetMapping("/gdpr/data-subject-report/{userId}")
    @Operation(summary = "Generate GDPR data subject access report")
    public ResponseEntity<Map<String, Object>> getDataSubjectReport(@PathVariable Long userId) {
        return ResponseEntity.ok(auditService.generateDataSubjectReport(userId));
    }

    @GetMapping("/gdpr/export/{userId}")
    @Operation(summary = "Export user data for GDPR portability")
    public ResponseEntity<List<AuditEvent>> exportUserData(@PathVariable Long userId) {
        return ResponseEntity.ok(auditService.exportUserData(userId));
    }

    // ==================== Retention Policy Endpoints ====================

    @PostMapping("/retention-policies")
    @Operation(summary = "Create retention policy")
    public ResponseEntity<RetentionPolicy> createRetentionPolicy(@RequestBody RetentionPolicy policy) {
        return ResponseEntity.ok(auditService.createRetentionPolicy(policy));
    }

    @GetMapping("/retention-policies")
    @Operation(summary = "List retention policies")
    public ResponseEntity<List<RetentionPolicy>> getRetentionPolicies(
            @RequestParam(required = false) Long organizationId) {
        return ResponseEntity.ok(auditService.getRetentionPolicies(organizationId));
    }

    @PostMapping("/retention-policies/apply")
    @Operation(summary = "Apply retention policies")
    public ResponseEntity<Void> applyRetentionPolicies() {
        auditService.applyRetentionPolicies();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cleanup")
    @Operation(summary = "Cleanup expired events")
    public ResponseEntity<Map<String, Object>> cleanupExpiredEvents() {
        int deleted = auditService.cleanupExpiredEvents();
        return ResponseEntity.ok(Map.of("deletedCount", deleted));
    }
}
