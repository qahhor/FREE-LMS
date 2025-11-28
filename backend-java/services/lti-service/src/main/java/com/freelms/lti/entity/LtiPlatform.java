package com.freelms.lti.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * LTI 1.3 Platform (Consumer) registration.
 * Represents external platforms that can launch LTI tools in FREE LMS.
 */
@Entity
@Table(name = "lti_platforms")
@EntityListeners(AuditingEntityListener.class)
public class LtiPlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    // Platform identification
    @Column(nullable = false, unique = true)
    private String issuer;

    @Column(nullable = false)
    private String clientId;

    // Endpoints
    @Column(nullable = false)
    private String oidcAuthUrl;

    @Column(nullable = false)
    private String accessTokenUrl;

    @Column(nullable = false)
    private String jwksUrl;

    // Platform public key (alternative to JWKS)
    @Column(length = 4000)
    private String publicKey;

    // Our deployment
    @Column(nullable = false)
    private String deploymentId;

    // Supported services
    private Boolean supportsAgs = true;  // Assignment and Grade Services
    private Boolean supportsNrps = true; // Names and Roles Provisioning
    private Boolean supportsDeepLinking = true;

    // Security settings
    @Column(length = 4000)
    private String privateKey;

    private String keyId;

    // Organization mapping
    private Long organizationId;

    // Status
    @Enumerated(EnumType.STRING)
    private PlatformStatus status = PlatformStatus.ACTIVE;

    private LocalDateTime lastLaunchAt;
    private Long launchCount = 0L;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Long createdBy;

    public enum PlatformStatus {
        ACTIVE,
        INACTIVE,
        PENDING_VERIFICATION,
        SUSPENDED
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getOidcAuthUrl() { return oidcAuthUrl; }
    public void setOidcAuthUrl(String oidcAuthUrl) { this.oidcAuthUrl = oidcAuthUrl; }

    public String getAccessTokenUrl() { return accessTokenUrl; }
    public void setAccessTokenUrl(String accessTokenUrl) { this.accessTokenUrl = accessTokenUrl; }

    public String getJwksUrl() { return jwksUrl; }
    public void setJwksUrl(String jwksUrl) { this.jwksUrl = jwksUrl; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getDeploymentId() { return deploymentId; }
    public void setDeploymentId(String deploymentId) { this.deploymentId = deploymentId; }

    public Boolean getSupportsAgs() { return supportsAgs; }
    public void setSupportsAgs(Boolean supportsAgs) { this.supportsAgs = supportsAgs; }

    public Boolean getSupportsNrps() { return supportsNrps; }
    public void setSupportsNrps(Boolean supportsNrps) { this.supportsNrps = supportsNrps; }

    public Boolean getSupportsDeepLinking() { return supportsDeepLinking; }
    public void setSupportsDeepLinking(Boolean supportsDeepLinking) { this.supportsDeepLinking = supportsDeepLinking; }

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public PlatformStatus getStatus() { return status; }
    public void setStatus(PlatformStatus status) { this.status = status; }

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
