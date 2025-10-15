package com.sentinel.plugin.api;

import java.util.Objects;

/**
 * Metadata describing a scanner plugin.
 */
public class PluginMetadata {
    
    private final String id;
    private final String name;
    private final String version;
    private final String author;
    private final String description;
    private final SafetyLevel safetyLevel;
    
    public PluginMetadata(String id, String name, String version, String author, 
                         String description, SafetyLevel safetyLevel) {
        this.id = Objects.requireNonNull(id, "Plugin ID cannot be null");
        this.name = Objects.requireNonNull(name, "Plugin name cannot be null");
        this.version = Objects.requireNonNull(version, "Plugin version cannot be null");
        this.author = Objects.requireNonNull(author, "Plugin author cannot be null");
        this.description = description;
        this.safetyLevel = Objects.requireNonNull(safetyLevel, "Safety level cannot be null");
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public String getDescription() {
        return description;
    }
    
    public SafetyLevel getSafetyLevel() {
        return safetyLevel;
    }
    
    @Override
    public String toString() {
        return String.format("%s v%s (%s) - %s", name, version, safetyLevel, id);
    }
}
