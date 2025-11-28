package com.freelms.lti.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * LTI 1.3 Tool (Provider) registration.
 * Represents external tools that can be launched from FREE LMS.
 */
@Entity
@Table(name = "lti_tools")
@EntityListeners(AuditingEntityListener.class)
public class LtiTool {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(length = 500)
    private String iconUrl;

    // Tool URLs
    @Column(nullable = false)
    private String targetLinkUri;

    @Column(nullable = false)
    private String oidcInitiationUrl;

    private String deepLinkingUrl;

    @Column(nullable = false)
    private String jwksUrl;

    // Tool identification
    @Column(nullable = false)
    private String clientId;

    private String publicKey;

    // Custom parameters
    @Column(length = 2000)
    private String customParameters;

    // Supported message types
    private Boolean supportsResourceLink = true;
    private Boolean supportsDeepLinking = true;

    // Privacy settings
    @Enumerated(EnumType.STRING)
    private PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC;

    private Boolean sendName = true;
    private Boolean sendEmail = true;
    private Boolean sendRoles = true;

    // Scope requirements
    @Column(length = 1000)
    private String requiredScopes;

    // Organization mapping
    private Long organizationId;

    // Placement settings
    @Enumerated(EnumType.STRING)
    private ToolPlacement placement = ToolPlacement.COURSE_NAVIGATION;

    private Boolean enabledByDefault = false;

    // Status
    @Enumerated(EnumType.STRING)
    private ToolStatus status = ToolStatus.ACTIVE;

    private LocalDateTime lastLaunchAt;
    private Long launchCount = 0L;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Long createdBy;

    public enum PrivacyLevel {
        PUBLIC,      // Full user info
        NAME_ONLY,   // Only name
        EMAIL_ONLY,  // Only email
        ANONYMOUS    // No user info
    }

    public enum ToolPlacement {
        COURSE_NAVIGATION,
        ACCOUNT_NAVIGATION,
        USER_NAVIGATION,
        EDITOR_BUTTON,
        ASSIGNMENT_SELECTION,
        LINK_SELECTION,
        GLOBAL_NAVIGATION
    }

    public enum ToolStatus {
        ACTIVE,
        INACTIVE,
        PENDING_APPROVAL,
        SUSPENDED
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public String getTargetLinkUri() { return targetLinkUri; }
    public void setTargetLinkUri(String targetLinkUri) { this.targetLinkUri = targetLinkUri; }

    public String getOidcInitiationUrl() { return oidcInitiationUrl; }
    public void setOidcInitiationUrl(String oidcInitiationUrl) { this.oidcInitiationUrl = oidcInitiationUrl; }

    public String getDeepLinkingUrl() { return deepLinkingUrl; }
    public void setDeepLinkingUrl(String deepLinkingUrl) { this.deepLinkingUrl = deepLinkingUrl; }

    public String getJwksUrl() { return jwksUrl; }
    public void setJwksUrl(String jwksUrl) { this.jwksUrl = jwksUrl; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getCustomParameters() { return customParameters; }
    public void setCustomParameters(String customParameters) { this.customParameters = customParameters; }

    public Boolean getSupportsResourceLink() { return supportsResourceLink; }
    public void setSupportsResourceLink(Boolean supportsResourceLink) { this.supportsResourceLink = supportsResourceLink; }

    public Boolean getSupportsDeepLinking() { return supportsDeepLinking; }
    public void setSupportsDeepLinking(Boolean supportsDeepLinking) { this.supportsDeepLinking = supportsDeepLinking; }

    public PrivacyLevel getPrivacyLevel() { return privacyLevel; }
    public void setPrivacyLevel(PrivacyLevel privacyLevel) { this.privacyLevel = privacyLevel; }

    public Boolean getSendName() { return sendName; }
    public void setSendName(Boolean sendName) { this.sendName = sendName; }

    public Boolean getSendEmail() { return sendEmail; }
    public void setSendEmail(Boolean sendEmail) { this.sendEmail = sendEmail; }

    public Boolean getSendRoles() { return sendRoles; }
    public void setSendRoles(Boolean sendRoles) { this.sendRoles = sendRoles; }

    public String getRequiredScopes() { return requiredScopes; }
    public void setRequiredScopes(String requiredScopes) { this.requiredScopes = requiredScopes; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public ToolPlacement getPlacement() { return placement; }
    public void setPlacement(ToolPlacement placement) { this.placement = placement; }

    public Boolean getEnabledByDefault() { return enabledByDefault; }
    public void setEnabledByDefault(Boolean enabledByDefault) { this.enabledByDefault = enabledByDefault; }

    public ToolStatus getStatus() { return status; }
    public void setStatus(ToolStatus status) { this.status = status; }

    public LocalDateTime getLastLaunchAt() { return lastLaunchAt; }
    public void setLastLaunchAt(LocalDateTime lastLaunchAt) { this.lastLaunchAt = lastLaunchAt; }

    public Long getLaunchCount() { return launchCount; }
    public void setLaunchCount(Long launchCount) { this.launchCount = launchCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}
