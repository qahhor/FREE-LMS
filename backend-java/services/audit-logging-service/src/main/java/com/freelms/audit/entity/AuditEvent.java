package com.freelms.audit.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit event entity for compliance logging.
 * Supports SOC2, GDPR, HIPAA, and other compliance requirements.
 */
@Entity
@Table(name = "audit_events", indexes = {
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_actor", columnList = "actor_id"),
    @Index(name = "idx_audit_event_type", columnList = "event_type"),
    @Index(name = "idx_audit_resource", columnList = "resource_type, resource_id"),
    @Index(name = "idx_audit_org", columnList = "organization_id")
})
@EntityListeners(AuditingEntityListener.class)
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Event classification
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity = Severity.INFO;

    // Actor information
    @Column(name = "actor_id")
    private Long actorId;

    private String actorName;
    private String actorEmail;

    @Enumerated(EnumType.STRING)
    private ActorType actorType = ActorType.USER;

    // Resource affected
    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    private String resourceName;

    // Context
    @Column(name = "organization_id")
    private Long organizationId;

    private Long courseId;
    private String serviceName;
    private String serviceVersion;

    // Action details
    @Column(nullable = false)
    private String action;

    @Column(length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    private ActionResult result = ActionResult.SUCCESS;

    private String errorCode;

    @Column(length = 2000)
    private String errorMessage;

    // Request context
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String requestId;
    private String requestMethod;
    private String requestPath;

    // Data changes (for data modification events)
    @Column(length = 10000)
    private String previousValue;

    @Column(length = 10000)
    private String newValue;

    @Column(length = 4000)
    private String changedFields;

    // Compliance flags
    private Boolean containsPii = false;
    private Boolean containsPhi = false;
    private Boolean dataExported = false;
    private Boolean dataDeleted = false;

    // Retention
    private LocalDateTime retentionUntil;
    private Boolean archived = false;

    // Geographic info (GDPR)
    private String country;
    private String region;

    public enum EventType {
        // Authentication
        LOGIN, LOGOUT, LOGIN_FAILED, PASSWORD_CHANGED, MFA_ENABLED, MFA_DISABLED,
        SESSION_CREATED, SESSION_EXPIRED, TOKEN_REFRESHED,

        // Authorization
        PERMISSION_GRANTED, PERMISSION_REVOKED, ROLE_ASSIGNED, ROLE_REMOVED,
        ACCESS_DENIED, PRIVILEGE_ESCALATION,

        // Data operations
        DATA_CREATED, DATA_READ, DATA_UPDATED, DATA_DELETED, DATA_EXPORTED,
        DATA_IMPORTED, DATA_ARCHIVED,

        // User management
        USER_CREATED, USER_UPDATED, USER_DELETED, USER_SUSPENDED, USER_ACTIVATED,
        PROFILE_UPDATED, SETTINGS_CHANGED,

        // Course operations
        COURSE_CREATED, COURSE_UPDATED, COURSE_DELETED, COURSE_PUBLISHED,
        ENROLLMENT_CREATED, ENROLLMENT_COMPLETED, ENROLLMENT_DROPPED,

        // Content operations
        CONTENT_CREATED, CONTENT_UPDATED, CONTENT_DELETED, CONTENT_PUBLISHED,
        FILE_UPLOADED, FILE_DOWNLOADED, FILE_DELETED,

        // Assessment
        QUIZ_STARTED, QUIZ_COMPLETED, ASSIGNMENT_SUBMITTED, GRADE_ASSIGNED,

        // System events
        SYSTEM_STARTED, SYSTEM_STOPPED, CONFIG_CHANGED, BACKUP_CREATED,
        BACKUP_RESTORED, MAINTENANCE_STARTED, MAINTENANCE_COMPLETED,

        // Security events
        SECURITY_ALERT, INTRUSION_DETECTED, VULNERABILITY_FOUND,
        SUSPICIOUS_ACTIVITY, RATE_LIMIT_EXCEEDED,

        // Compliance
        CONSENT_GIVEN, CONSENT_WITHDRAWN, DATA_REQUEST_RECEIVED,
        DATA_REQUEST_COMPLETED, RETENTION_POLICY_APPLIED,

        // Integration
        API_KEY_CREATED, API_KEY_REVOKED, WEBHOOK_TRIGGERED,
        INTEGRATION_CONNECTED, INTEGRATION_DISCONNECTED
    }

    public enum EventCategory {
        AUTHENTICATION,
        AUTHORIZATION,
        DATA_ACCESS,
        DATA_MODIFICATION,
        USER_MANAGEMENT,
        COURSE_MANAGEMENT,
        CONTENT_MANAGEMENT,
        ASSESSMENT,
        SYSTEM,
        SECURITY,
        COMPLIANCE,
        INTEGRATION
    }

    public enum Severity {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    public enum ActorType {
        USER,
        ADMIN,
        SYSTEM,
        API,
        INTEGRATION,
        ANONYMOUS
    }

    public enum ActionResult {
        SUCCESS,
        FAILURE,
        PARTIAL,
        DENIED
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public EventCategory getCategory() { return category; }
    public void setCategory(EventCategory category) { this.category = category; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getActorEmail() { return actorEmail; }
    public void setActorEmail(String actorEmail) { this.actorEmail = actorEmail; }

    public ActorType getActorType() { return actorType; }
    public void setActorType(ActorType actorType) { this.actorType = actorType; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServiceVersion() { return serviceVersion; }
    public void setServiceVersion(String serviceVersion) { this.serviceVersion = serviceVersion; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ActionResult getResult() { return result; }
    public void setResult(ActionResult result) { this.result = result; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }

    public String getRequestPath() { return requestPath; }
    public void setRequestPath(String requestPath) { this.requestPath = requestPath; }

    public String getPreviousValue() { return previousValue; }
    public void setPreviousValue(String previousValue) { this.previousValue = previousValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getChangedFields() { return changedFields; }
    public void setChangedFields(String changedFields) { this.changedFields = changedFields; }

    public Boolean getContainsPii() { return containsPii; }
    public void setContainsPii(Boolean containsPii) { this.containsPii = containsPii; }

    public Boolean getContainsPhi() { return containsPhi; }
    public void setContainsPhi(Boolean containsPhi) { this.containsPhi = containsPhi; }

    public Boolean getDataExported() { return dataExported; }
    public void setDataExported(Boolean dataExported) { this.dataExported = dataExported; }

    public Boolean getDataDeleted() { return dataDeleted; }
    public void setDataDeleted(Boolean dataDeleted) { this.dataDeleted = dataDeleted; }

    public LocalDateTime getRetentionUntil() { return retentionUntil; }
    public void setRetentionUntil(LocalDateTime retentionUntil) { this.retentionUntil = retentionUntil; }

    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
