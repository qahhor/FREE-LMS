package com.freelms.marketplace.plugin;

import java.util.Map;

/**
 * Smartup LMS - Plugin Interface
 *
 * Base interface for all marketplace plugins.
 */
public interface Plugin {

    /**
     * Initialize the plugin with context
     */
    void initialize(PluginContext context);

    /**
     * Activate the plugin
     */
    void activate();

    /**
     * Deactivate the plugin (pause without destroying)
     */
    void deactivate();

    /**
     * Destroy and cleanup the plugin
     */
    void destroy();

    /**
     * Execute a plugin method
     */
    Object execute(String method, Object... args);

    /**
     * Called when configuration changes
     */
    void onConfigurationChange(Map<String, String> newConfig);

    /**
     * Get plugin health status
     */
    PluginHealth getHealth();

    /**
     * Get plugin capabilities
     */
    PluginCapabilities getCapabilities();
}
