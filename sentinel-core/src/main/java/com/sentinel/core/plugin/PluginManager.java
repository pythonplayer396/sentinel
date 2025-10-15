package com.sentinel.core.plugin;

import com.sentinel.plugin.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

/**
 * Manages plugin loading, lifecycle, and execution.
 */
public class PluginManager {
    
    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);
    
    private final Map<String, ScannerPlugin> plugins;
    private final Map<String, ClassLoader> pluginClassLoaders;
    
    public PluginManager() {
        this.plugins = new ConcurrentHashMap<>();
        this.pluginClassLoaders = new ConcurrentHashMap<>();
    }
    
    /**
     * Register a plugin instance.
     */
    public void registerPlugin(ScannerPlugin plugin) {
        PluginMetadata metadata = plugin.getMetadata();
        String pluginId = metadata.getId();
        
        if (plugins.containsKey(pluginId)) {
            logger.warn("Plugin {} already registered, replacing", pluginId);
        }
        
        plugin.initialize();
        plugins.put(pluginId, plugin);
        logger.info("Registered plugin: {}", metadata);
    }
    
    /**
     * Load plugins from a directory containing JAR files.
     */
    public void loadPluginsFromDirectory(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            logger.warn("Plugin directory does not exist: {}", directory);
            return;
        }
        
        File[] jarFiles = directory.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            logger.info("No plugin JARs found in {}", directory);
            return;
        }
        
        for (File jarFile : jarFiles) {
            try {
                loadPluginFromJar(jarFile);
            } catch (Exception e) {
                logger.error("Failed to load plugin from {}: {}", jarFile, e.getMessage());
            }
        }
    }
    
    /**
     * Load a plugin from a JAR file.
     */
    private void loadPluginFromJar(File jarFile) throws Exception {
        logger.info("Loading plugin from: {}", jarFile);
        
        // Create class loader for the plugin
        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarUrl},
                getClass().getClassLoader()
        );
        
        // Find plugin classes using ServiceLoader
        ServiceLoader<ScannerPlugin> serviceLoader = ServiceLoader.load(
                ScannerPlugin.class,
                classLoader
        );
        
        boolean foundPlugin = false;
        for (ScannerPlugin plugin : serviceLoader) {
            registerPlugin(plugin);
            pluginClassLoaders.put(plugin.getMetadata().getId(), classLoader);
            foundPlugin = true;
        }
        
        if (!foundPlugin) {
            logger.warn("No ScannerPlugin implementation found in {}", jarFile);
            classLoader.close();
        }
    }
    
    /**
     * Get a plugin by ID.
     */
    public Optional<ScannerPlugin> getPlugin(String pluginId) {
        return Optional.ofNullable(plugins.get(pluginId));
    }
    
    /**
     * Get all registered plugins.
     */
    public Collection<ScannerPlugin> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }
    
    /**
     * Get plugins filtered by safety level.
     */
    public List<ScannerPlugin> getPluginsBySafetyLevel(SafetyLevel maxLevel) {
        List<ScannerPlugin> filtered = new ArrayList<>();
        for (ScannerPlugin plugin : plugins.values()) {
            if (!plugin.getMetadata().getSafetyLevel().requiresMorePermissionsThan(maxLevel)) {
                filtered.add(plugin);
            }
        }
        return filtered;
    }
    
    /**
     * Unregister a plugin.
     */
    public void unregisterPlugin(String pluginId) {
        ScannerPlugin plugin = plugins.remove(pluginId);
        if (plugin != null) {
            plugin.shutdown();
            logger.info("Unregistered plugin: {}", pluginId);
            
            // Close class loader if exists
            ClassLoader classLoader = pluginClassLoaders.remove(pluginId);
            if (classLoader instanceof URLClassLoader) {
                try {
                    ((URLClassLoader) classLoader).close();
                } catch (Exception e) {
                    logger.warn("Failed to close class loader for plugin {}", pluginId);
                }
            }
        }
    }
    
    /**
     * Shutdown all plugins.
     */
    public void shutdown() {
        logger.info("Shutting down plugin manager");
        for (String pluginId : new ArrayList<>(plugins.keySet())) {
            unregisterPlugin(pluginId);
        }
    }
}
