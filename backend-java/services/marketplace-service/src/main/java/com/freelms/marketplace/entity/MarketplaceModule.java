package com.freelms.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

/**
 * Smartup LMS - Marketplace Module Entity
 *
 * Represents a functional module/plugin that extends platform capabilities.
 * Examples: Currency rates widget, HR integration, Custom reports, etc.
 */
@Entity
@Table(name = "marketplace_modules")
@DiscriminatorValue("MODULE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceModule extends MarketplaceItem {

    @Enumerated(EnumType.STRING)
    @Column(name = "module_type")
    private ModuleType moduleType = ModuleType.EXTENSION;

    // Technical details
    @Column(name = "package_url")
    private String packageUrl;

    @Column(name = "package_size")
    private Long packageSize;

    @Column(name = "package_checksum")
    private String packageChecksum;

    @Column(name = "entry_point")
    private String entryPoint;

    @Column(name = "config_schema", columnDefinition = "TEXT")
    private String configSchema; // JSON Schema for configuration

    // Permissions required
    @ElementCollection
    @CollectionTable(name = "module_permissions", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "permission")
    private Set<String> requiredPermissions = new HashSet<>();

    // API endpoints provided
    @ElementCollection
    @CollectionTable(name = "module_endpoints", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "endpoint")
    private Set<String> providedEndpoints = new HashSet<>();

    // Events the module subscribes to
    @ElementCollection
    @CollectionTable(name = "module_event_subscriptions", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "event_type")
    private Set<String> eventSubscriptions = new HashSet<>();

    // Events the module emits
    @ElementCollection
    @CollectionTable(name = "module_event_emissions", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "event_type")
    private Set<String> eventEmissions = new HashSet<>();

    // UI Components
    @Column(name = "has_dashboard_widget")
    private boolean hasDashboardWidget;

    @Column(name = "has_settings_page")
    private boolean hasSettingsPage;

    @Column(name = "has_admin_page")
    private boolean hasAdminPage;

    @Column(name = "menu_items", columnDefinition = "TEXT")
    private String menuItems; // JSON array of menu items

    // Sandbox & Security
    @Column(name = "sandbox_enabled")
    private boolean sandboxEnabled = true;

    @Column(name = "security_audit_passed")
    private boolean securityAuditPassed;

    @Column(name = "security_audit_date")
    private String securityAuditDate;

    // Database requirements
    @Column(name = "requires_database")
    private boolean requiresDatabase;

    @Column(name = "database_migrations", columnDefinition = "TEXT")
    private String databaseMigrations; // SQL migrations

    // External services
    @ElementCollection
    @CollectionTable(name = "module_external_services", joinColumns = @JoinColumn(name = "module_id"))
    @MapKeyColumn(name = "service_name")
    @Column(name = "service_url")
    private Map<String, String> externalServices = new HashMap<>();

    // Hooks into platform
    @ElementCollection
    @CollectionTable(name = "module_hooks", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "hook_name")
    private Set<String> hooks = new HashSet<>();

    // Module types
    public enum ModuleType {
        EXTENSION,       // General functionality extension
        INTEGRATION,     // Third-party service integration
        WIDGET,          // Dashboard widget
        REPORT,          // Custom report
        AUTOMATION,      // Workflow automation
        AI_ASSISTANT,    // AI-powered feature
        ANALYTICS,       // Analytics/BI module
        COMMUNICATION,   // Communication tools
        GAMIFICATION,    // Gamification add-on
        COMPLIANCE,      // Compliance/audit module
        CONTENT,         // Content creation tools
        ASSESSMENT       // Assessment/quiz tools
    }

    @Builder(builderMethodName = "moduleBuilder")
    public MarketplaceModule(String slug, String name, String description,
                             ModuleType moduleType, String version) {
        this.setSlug(slug);
        this.setName(name);
        this.setDescription(description);
        this.setType(ItemType.MODULE);
        this.moduleType = moduleType;
        this.setVersion(version);
    }
}
