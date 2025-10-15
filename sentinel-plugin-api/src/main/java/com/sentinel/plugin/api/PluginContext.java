package com.sentinel.plugin.api;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

/**
 * Execution context provided to plugins during scanning.
 * Contains sanitized request/response data and metadata.
 */
public interface PluginContext {
    
    /**
     * Get the target URL being scanned.
     */
    URI getTargetUrl();
    
    /**
     * Get the HTTP request that was sent.
     */
    HttpRequest getRequest();
    
    /**
     * Get the HTTP response that was received.
     */
    HttpResponse getResponse();
    
    /**
     * Get a specific request parameter value.
     * @param name Parameter name
     * @return Parameter value if present
     */
    Optional<String> getParameterValue(String name);
    
    /**
     * Get all request parameters.
     */
    Map<String, String> getAllParameters();
    
    /**
     * Get the crawl depth of this URL.
     */
    int getCrawlDepth();
    
    /**
     * Check if JavaScript execution is available.
     */
    boolean isJavaScriptAvailable();
    
    /**
     * Check if authentication is active for this scan.
     */
    boolean isAuthenticated();
    
    /**
     * Get scan configuration for this context.
     */
    ScanConfig getScanConfig();
    
    /**
     * Get a logger for this plugin execution.
     */
    PluginLogger getLogger();
}
