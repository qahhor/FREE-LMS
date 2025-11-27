package com.freelms.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Smartup LMS - Module Installation Entity
 *
 * Tracks installed modules per organization.
 */
@Entity
@Table(name = "module_installations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "module_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleInstallation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private MarketplaceModule module;

    @Column(name = "installed_version", nullable = false)
    private String installedVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallationStatus status = InstallationStatus.PENDING;

    @Column(name = "installed_by")
    private Long installedBy;

    // Configuration
    @Column(name = "config", columnDefinition = "TEXT")
    private String config; // JSON configuration

    @ElementCollection
    @CollectionTable(name = "module_installation_settings",
                     joinColumns = @JoinColumn(name = "installation_id"))
    @MapKeyColumn(name = "setting_key")
    @Column(name = "setting_value", columnDefinition = "TEXT")
    private Map<String, String> settings = new HashMap<>();

    // Activation
    @Column(name = "active")
    private boolean active = true;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    // License
    @Column(name = "license_key")
    private String licenseKey;

    @Column(name = "license_expires_at")
    private Instant licenseExpiresAt;

    // Trial
    @Column(name = "is_trial")
    private boolean trial;

    @Column(name = "trial_ends_at")
    private Instant trialEndsAt;

    // Usage tracking
    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "usage_count")
    private Long usageCount = 0L;

    // Update info
    @Column(name = "update_available")
    private boolean updateAvailable;

    @Column(name = "available_version")
    private String availableVersion;

    @Column(name = "auto_update")
    private boolean autoUpdate = false;

    // Error tracking
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "error_count")
    private Integer errorCount = 0;

    // Timestamps
    @CreationTimestamp
    @Column(name = "installed_at", updatable = false)
    private Instant installedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum InstallationStatus {
        PENDING,        // Waiting to install
        INSTALLING,     // Installation in progress
        ACTIVE,         // Successfully installed and active
        INACTIVE,       // Installed but disabled
        FAILED,         // Installation failed
        UPDATING,       // Update in progress
        UNINSTALLING    // Being removed
    }

    // Helper methods
    public boolean isExpired() {
        if (licenseExpiresAt != null && Instant.now().isAfter(licenseExpiresAt)) {
            return true;
        }
        if (trial && trialEndsAt != null && Instant.now().isAfter(trialEndsAt)) {
            return true;
        }
        return false;
    }

    public boolean needsUpdate() {
        return updateAvailable && availableVersion != null &&
               !availableVersion.equals(installedVersion);
    }

    public void incrementUsage() {
        this.usageCount++;
        this.lastUsedAt = Instant.now();
    }

    public void recordError(String error) {
        this.lastError = error;
        this.errorCount++;
    }
}
