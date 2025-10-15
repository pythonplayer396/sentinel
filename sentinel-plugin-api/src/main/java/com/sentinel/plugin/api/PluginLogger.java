package com.sentinel.plugin.api;

/**
 * Logger interface for plugins.
 * Provides structured logging with automatic plugin context.
 */
public interface PluginLogger {
    
    void debug(String message);
    
    void debug(String message, Object... args);
    
    void info(String message);
    
    void info(String message, Object... args);
    
    void warn(String message);
    
    void warn(String message, Object... args);
    
    void error(String message);
    
    void error(String message, Throwable throwable);
    
    void error(String message, Object... args);
}
