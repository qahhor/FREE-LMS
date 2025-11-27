package com.freelms.marketplace.plugin;

import lombok.*;

import java.time.Instant;
import java.util.*;

/**
 * Smartup LMS - Plugin System Models
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PluginContext {
    private Long organizationId;
    private String pluginSlug;
    private String pluginVersion;
    private Map<String, String> configuration;
    private PluginServices services; // Injected platform services
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PluginInstance {
    private Plugin plugin;
    private PluginMetadata metadata;
    private PluginContext context;
    private Status status;
    private Instant loadedAt;
    private Instant lastUsedAt;
    private Long usageCount;
    private String lastError;

    public enum Status {
        LOADING,
        ACTIVE,
        INACTIVE,
        ERROR,
        UNLOADING
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PluginMetadata {
    private String slug;
    private String name;
    private String version;
    private String description;
    private String author;
    private String iconUrl;
    private Set<String> requiredPermissions;
    private Set<String> providedEndpoints;
    private Set<String> eventSubscriptions;
    private Map<String, String> configurationSchema;
    private PluginFactory pluginFactory;
}

@FunctionalInterface
interface PluginFactory {
    Plugin create();
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PluginHealth {
    private boolean healthy;
    private String status;
    private Map<String, Object> details;
    private Instant lastChecked;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PluginCapabilities {
    private boolean hasDashboardWidget;
    private boolean hasSettingsPage;
    private boolean hasApiEndpoints;
    private boolean hasScheduledTasks;
    private boolean hasEventHandlers;
    private Set<String> supportedMethods;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PluginEvent {
    private Type type;
    private String pluginSlug;
    private Long organizationId;
    private Instant timestamp;
    private Map<String, Object> data;

    public PluginEvent(Type type, String pluginSlug, Long organizationId) {
        this.type = type;
        this.pluginSlug = pluginSlug;
        this.organizationId = organizationId;
        this.timestamp = Instant.now();
        this.data = new HashMap<>();
    }

    public enum Type {
        LOADED,
        ACTIVATED,
        DEACTIVATED,
        UNLOADED,
        ERROR,
        CONFIG_CHANGED
    }
}

interface PluginEventListener {
    void onPluginEvent(PluginEvent event);
}

/**
 * Platform services available to plugins
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PluginServices {
    private DataService dataService;
    private CacheService cacheService;
    private EventService eventService;
    private HttpService httpService;
    private NotificationService notificationService;

    interface DataService {
        <T> T save(T entity);
        <T> Optional<T> findById(Class<T> type, Long id);
        <T> List<T> findAll(Class<T> type);
        void delete(Object entity);
    }

    interface CacheService {
        void put(String key, Object value);
        <T> Optional<T> get(String key, Class<T> type);
        void evict(String key);
    }

    interface EventService {
        void publish(String eventType, Object payload);
        void subscribe(String eventType, java.util.function.Consumer<Object> handler);
    }

    interface HttpService {
        <T> T get(String url, Class<T> responseType);
        <T> T post(String url, Object body, Class<T> responseType);
    }

    interface NotificationService {
        void sendEmail(Long userId, String subject, String body);
        void sendPush(Long userId, String title, String message);
    }
}

/**
 * Base class for plugin implementations
 */
abstract class BasePlugin implements Plugin {

    protected PluginContext context;
    protected boolean active = false;

    @Override
    public void initialize(PluginContext context) {
        this.context = context;
    }

    @Override
    public void activate() {
        this.active = true;
    }

    @Override
    public void deactivate() {
        this.active = false;
    }

    @Override
    public void destroy() {
        this.active = false;
        this.context = null;
    }

    @Override
    public void onConfigurationChange(Map<String, String> newConfig) {
        // Default: do nothing
    }

    @Override
    public PluginHealth getHealth() {
        return PluginHealth.builder()
                .healthy(active)
                .status(active ? "OK" : "Inactive")
                .lastChecked(Instant.now())
                .details(new HashMap<>())
                .build();
    }

    protected Long getOrganizationId() {
        return context.getOrganizationId();
    }

    protected String getConfig(String key) {
        return context.getConfiguration().get(key);
    }

    protected String getConfig(String key, String defaultValue) {
        return context.getConfiguration().getOrDefault(key, defaultValue);
    }
}
