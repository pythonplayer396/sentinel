package com.sentinel.plugin.api;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable representation of an HTTP request.
 * Sanitized for safe plugin consumption.
 */
public interface HttpRequest {
    
    /**
     * Get the request method (GET, POST, etc.).
     */
    String getMethod();
    
    /**
     * Get the request URI.
     */
    URI getUri();
    
    /**
     * Get all request headers.
     */
    Map<String, List<String>> getHeaders();
    
    /**
     * Get a specific header value.
     */
    Optional<String> getHeader(String name);
    
    /**
     * Get the request body if present.
     */
    Optional<String> getBody();
    
    /**
     * Get query parameters from the URL.
     */
    Map<String, String> getQueryParameters();
    
    /**
     * Get the content type.
     */
    Optional<String> getContentType();
    
    /**
     * Check if this is a secure (HTTPS) request.
     */
    boolean isSecure();
}
