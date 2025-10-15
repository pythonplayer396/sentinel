package com.sentinel.plugin.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration for a plugin instance.
 * Allows plugins to be customized without code changes.
 */
public class PluginConfig {
    
    private final Map<String, Object> properties;
    
    public PluginConfig() {
        this.properties = new HashMap<>();
    }
    
    public PluginConfig(Map<String, Object> properties) {
        this.properties = new HashMap<>(properties);
    }
    
    public Optional<Object> get(String key) {
        return Optional.ofNullable(properties.get(key));
    }
    
    public String getString(String key, String defaultValue) {
        return get(key).map(Object::toString).orElse(defaultValue);
    }
    
    public int getInt(String key, int defaultValue) {
        return get(key)
                .map(v -> v instanceof Number ? ((Number) v).intValue() : Integer.parseInt(v.toString()))
                .orElse(defaultValue);
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        return get(key)
                .map(v -> v instanceof Boolean ? (Boolean) v : Boolean.parseBoolean(v.toString()))
                .orElse(defaultValue);
    }
    
    public void set(String key, Object value) {
        properties.put(key, value);
    }
    
    public Map<String, Object> getAll() {
        return new HashMap<>(properties);
    }
}
