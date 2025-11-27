package com.freelms.marketplace.plugin;

import com.freelms.marketplace.entity.MarketplaceModule;
import com.freelms.marketplace.entity.ModuleInstallation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Smartup LMS - Plugin Manager
 *
 * Manages plugin lifecycle: loading, activation, deactivation, and unloading.
 */
@Component
@Slf4j
public class PluginManager {

    private final Map<String, PluginInstance> loadedPlugins = new ConcurrentHashMap<>();
    private final Map<String, PluginMetadata> registeredPlugins = new ConcurrentHashMap<>();
    private final List<PluginEventListener> eventListeners = new ArrayList<>();

    /**
     * Register a plugin with its metadata
     */
    public void registerPlugin(PluginMetadata metadata) {
        registeredPlugins.put(metadata.getSlug(), metadata);
        log.info("Registered plugin: {} v{}", metadata.getName(), metadata.getVersion());
    }

    /**
     * Load and activate a plugin for an organization
     */
    public PluginInstance loadPlugin(String pluginSlug, Long organizationId, Map<String, String> config) {
        PluginMetadata metadata = registeredPlugins.get(pluginSlug);
        if (metadata == null) {
            throw new IllegalArgumentException("Plugin not found: " + pluginSlug);
        }

        String instanceKey = createInstanceKey(pluginSlug, organizationId);

        if (loadedPlugins.containsKey(instanceKey)) {
            log.warn("Plugin already loaded: {} for org: {}", pluginSlug, organizationId);
            return loadedPlugins.get(instanceKey);
        }

        try {
            // Create plugin instance
            Plugin plugin = metadata.getPluginFactory().create();

            PluginContext context = PluginContext.builder()
                    .organizationId(organizationId)
                    .configuration(config)
                    .pluginSlug(pluginSlug)
                    .pluginVersion(metadata.getVersion())
                    .build();

            // Initialize plugin
            plugin.initialize(context);

            PluginInstance instance = PluginInstance.builder()
                    .plugin(plugin)
                    .metadata(metadata)
                    .context(context)
                    .status(PluginInstance.Status.ACTIVE)
                    .loadedAt(java.time.Instant.now())
                    .build();

            loadedPlugins.put(instanceKey, instance);
            notifyListeners(new PluginEvent(PluginEvent.Type.LOADED, pluginSlug, organizationId));

            log.info("Loaded plugin: {} for organization: {}", pluginSlug, organizationId);
            return instance;

        } catch (Exception e) {
            log.error("Failed to load plugin: {} - {}", pluginSlug, e.getMessage());
            throw new PluginLoadException("Failed to load plugin: " + pluginSlug, e);
        }
    }

    /**
     * Deactivate a plugin without unloading
     */
    public void deactivatePlugin(String pluginSlug, Long organizationId) {
        String instanceKey = createInstanceKey(pluginSlug, organizationId);
        PluginInstance instance = loadedPlugins.get(instanceKey);

        if (instance != null) {
            try {
                instance.getPlugin().deactivate();
                instance.setStatus(PluginInstance.Status.INACTIVE);
                notifyListeners(new PluginEvent(PluginEvent.Type.DEACTIVATED, pluginSlug, organizationId));
                log.info("Deactivated plugin: {} for organization: {}", pluginSlug, organizationId);
            } catch (Exception e) {
                log.error("Failed to deactivate plugin: {} - {}", pluginSlug, e.getMessage());
            }
        }
    }

    /**
     * Reactivate a deactivated plugin
     */
    public void activatePlugin(String pluginSlug, Long organizationId) {
        String instanceKey = createInstanceKey(pluginSlug, organizationId);
        PluginInstance instance = loadedPlugins.get(instanceKey);

        if (instance != null && instance.getStatus() == PluginInstance.Status.INACTIVE) {
            try {
                instance.getPlugin().activate();
                instance.setStatus(PluginInstance.Status.ACTIVE);
                notifyListeners(new PluginEvent(PluginEvent.Type.ACTIVATED, pluginSlug, organizationId));
                log.info("Activated plugin: {} for organization: {}", pluginSlug, organizationId);
            } catch (Exception e) {
                log.error("Failed to activate plugin: {} - {}", pluginSlug, e.getMessage());
            }
        }
    }

    /**
     * Unload and cleanup a plugin
     */
    public void unloadPlugin(String pluginSlug, Long organizationId) {
        String instanceKey = createInstanceKey(pluginSlug, organizationId);
        PluginInstance instance = loadedPlugins.remove(instanceKey);

        if (instance != null) {
            try {
                instance.getPlugin().destroy();
                notifyListeners(new PluginEvent(PluginEvent.Type.UNLOADED, pluginSlug, organizationId));
                log.info("Unloaded plugin: {} for organization: {}", pluginSlug, organizationId);
            } catch (Exception e) {
                log.error("Failed to unload plugin: {} - {}", pluginSlug, e.getMessage());
            }
        }
    }

    /**
     * Get a loaded plugin instance
     */
    public Optional<PluginInstance> getPluginInstance(String pluginSlug, Long organizationId) {
        String instanceKey = createInstanceKey(pluginSlug, organizationId);
        return Optional.ofNullable(loadedPlugins.get(instanceKey));
    }

    /**
     * Get all loaded plugins for an organization
     */
    public List<PluginInstance> getOrganizationPlugins(Long organizationId) {
        return loadedPlugins.entrySet().stream()
                .filter(e -> e.getKey().endsWith(":" + organizationId))
                .map(Map.Entry::getValue)
                .toList();
    }

    /**
     * Check if a plugin is loaded
     */
    public boolean isPluginLoaded(String pluginSlug, Long organizationId) {
        return loadedPlugins.containsKey(createInstanceKey(pluginSlug, organizationId));
    }

    /**
     * Execute a plugin method
     */
    @SuppressWarnings("unchecked")
    public <T> T executePlugin(String pluginSlug, Long organizationId, String method, Object... args) {
        PluginInstance instance = loadedPlugins.get(createInstanceKey(pluginSlug, organizationId));

        if (instance == null || instance.getStatus() != PluginInstance.Status.ACTIVE) {
            throw new IllegalStateException("Plugin not active: " + pluginSlug);
        }

        try {
            return (T) instance.getPlugin().execute(method, args);
        } catch (Exception e) {
            log.error("Plugin execution error: {} - {}", pluginSlug, e.getMessage());
            throw new PluginExecutionException("Plugin execution failed: " + pluginSlug, e);
        }
    }

    /**
     * Update plugin configuration
     */
    public void updateConfiguration(String pluginSlug, Long organizationId, Map<String, String> newConfig) {
        PluginInstance instance = loadedPlugins.get(createInstanceKey(pluginSlug, organizationId));

        if (instance != null) {
            instance.getContext().setConfiguration(newConfig);
            instance.getPlugin().onConfigurationChange(newConfig);
            log.info("Updated configuration for plugin: {} org: {}", pluginSlug, organizationId);
        }
    }

    /**
     * Add event listener
     */
    public void addEventListener(PluginEventListener listener) {
        eventListeners.add(listener);
    }

    private String createInstanceKey(String pluginSlug, Long organizationId) {
        return pluginSlug + ":" + organizationId;
    }

    private void notifyListeners(PluginEvent event) {
        eventListeners.forEach(listener -> {
            try {
                listener.onPluginEvent(event);
            } catch (Exception e) {
                log.error("Error notifying plugin listener: {}", e.getMessage());
            }
        });
    }

    // Exception classes
    public static class PluginLoadException extends RuntimeException {
        public PluginLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class PluginExecutionException extends RuntimeException {
        public PluginExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
