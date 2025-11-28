package com.freelms.audit.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data retention policy configuration.
 */
@Entity
@Table(name = "retention_policies")
@EntityListeners(AuditingEntityListener.class)
public class RetentionPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // Scope
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    private AuditEvent.EventCategory category;

    @Enumerated(EnumType.STRING)
    private AuditEvent.EventType eventType;

    // Retention settings
    @Column(nullable = false)
    private Integer retentionDays;

    private Integer archiveAfterDays;

    private Boolean autoDelete = true;
    private Boolean archiveBeforeDelete = true;

    // Compliance framework
    @Enumerated(EnumType.STRING)
    private ComplianceFramework framework;

    // Schedule
    private Boolean active = true;
    private String cronSchedule;
    private LocalDateTime lastExecuted;
    private LocalDateTime nextExecution;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Long createdBy;

    public enum ComplianceFramework {
        SOC2,
        GDPR,
        HIPAA,
        PCI_DSS,
        FERPA,
        CCPA,
        CUSTOM
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public AuditEvent.EventCategory getCategory() { return category; }
    public void setCategory(AuditEvent.EventCategory category) { this.category = category; }

    public AuditEvent.EventType getEventType() { return eventType; }
    public void setEventType(AuditEvent.EventType eventType) { this.eventType = eventType; }

    public Integer getRetentionDays() { return retentionDays; }
    public void setRetentionDays(Integer retentionDays) { this.retentionDays = retentionDays; }

    public Integer getArchiveAfterDays() { return archiveAfterDays; }
    public void setArchiveAfterDays(Integer archiveAfterDays) { this.archiveAfterDays = archiveAfterDays; }

    public Boolean getAutoDelete() { return autoDelete; }
    public void setAutoDelete(Boolean autoDelete) { this.autoDelete = autoDelete; }

    public Boolean getArchiveBeforeDelete() { return archiveBeforeDelete; }
    public void setArchiveBeforeDelete(Boolean archiveBeforeDelete) { this.archiveBeforeDelete = archiveBeforeDelete; }

    public ComplianceFramework getFramework() { return framework; }
    public void setFramework(ComplianceFramework framework) { this.framework = framework; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getCronSchedule() { return cronSchedule; }
    public void setCronSchedule(String cronSchedule) { this.cronSchedule = cronSchedule; }

    public LocalDateTime getLastExecuted() { return lastExecuted; }
    public void setLastExecuted(LocalDateTime lastExecuted) { this.lastExecuted = lastExecuted; }

    public LocalDateTime getNextExecution() { return nextExecution; }
    public void setNextExecution(LocalDateTime nextExecution) { this.nextExecution = nextExecution; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}
