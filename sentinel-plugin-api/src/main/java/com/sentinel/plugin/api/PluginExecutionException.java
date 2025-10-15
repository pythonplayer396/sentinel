package com.sentinel.plugin.api;

/**
 * Exception thrown when plugin execution fails.
 */
public class PluginExecutionException extends Exception {
    
    public PluginExecutionException(String message) {
        super(message);
    }
    
    public PluginExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PluginExecutionException(Throwable cause) {
        super(cause);
    }
}
