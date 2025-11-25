package com.freelms.integration.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "integrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Integration extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_type", nullable = false)
    private IntegrationType integrationType;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private IntegrationStatus status = IntegrationStatus.INACTIVE;

    @Column(name = "config", columnDefinition = "TEXT")
    private String config; // JSON - encrypted credentials

    @Column(name = "sync_frequency")
    private String syncFrequency; // cron expression

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "last_sync_status")
    private String lastSyncStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "webhook_url")
    private String webhookUrl;

    @Column(name = "webhook_secret")
    private String webhookSecret;

    @Column(name = "created_by")
    private Long createdBy;

    public enum IntegrationType {
        HR_SYSTEM,      // 1C, SAP HR, Workday
        CALENDAR,       // Google, Outlook
        VIDEO_CONF,     // Zoom, Teams, Meet
        SSO,            // LDAP, AD, Keycloak, SAML
        STORAGE,        // S3, Google Drive
        MESSAGING,      // Slack, Teams
        CUSTOM
    }

    public enum IntegrationStatus {
        INACTIVE,
        ACTIVE,
        ERROR,
        SYNCING
    }
}
