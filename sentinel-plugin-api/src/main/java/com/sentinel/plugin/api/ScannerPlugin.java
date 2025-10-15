package com.sentinel.plugin.api;

import java.util.List;

/**
 * Core interface for all Sentinel scanner plugins.
 * Plugins must be safe, non-destructive, and follow protection-first principles.
 */
public interface ScannerPlugin {
    
    /**
     * Returns metadata about this plugin.
     * @return Plugin metadata including id, name, version, author, and safety level
     */
    PluginMetadata getMetadata();
    
    /**
     * Returns capabilities required by this plugin.
     * @return Plugin capabilities (headless browser, authentication, etc.)
     */
    PluginCapabilities getCapabilities();
    
    /**
     * Execute the plugin's detection logic.
     * This method must be safe and non-destructive.
     * 
     * @param context The plugin execution context with request/response data
     * @return List of findings discovered by this plugin
     * @throws PluginExecutionException if plugin execution fails
     */
    List<PluginFinding> run(PluginContext context) throws PluginExecutionException;
    
    /**
     * Configure the plugin with custom settings.
     * @param config Plugin-specific configuration
     */
    void configure(PluginConfig config);
    
    /**
     * Initialize the plugin. Called once before any run() invocations.
     */
    default void initialize() {
        // Optional initialization
    }
    
    /**
     * Cleanup resources. Called when plugin is unloaded.
     */
    default void shutdown() {
        // Optional cleanup
    }
}
