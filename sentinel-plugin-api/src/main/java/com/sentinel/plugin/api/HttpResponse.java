package com.sentinel.plugin.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable representation of an HTTP response.
 * Sanitized for safe plugin consumption.
 */
public interface HttpResponse {
    
    /**
     * Get the HTTP status code.
     */
    int getStatusCode();
    
    /**
     * Get the status reason phrase.
     */
    String getReasonPhrase();
    
    /**
     * Get all response headers.
     */
    Map<String, List<String>> getHeaders();
    
    /**
     * Get a specific header value.
     */
    Optional<String> getHeader(String name);
    
    /**
     * Get the response body.
     */
    String getBody();
    
    /**
     * Get the response body truncated to a maximum length.
     * Useful for avoiding memory issues with large responses.
     */
    String getBodyTruncated(int maxLength);
    
    /**
     * Get the content type.
     */
    Optional<String> getContentType();
    
    /**
     * Get the response time in milliseconds.
     */
    long getResponseTimeMs();
    
    /**
     * Get the content length.
     */
    long getContentLength();
    
    /**
     * Check if the response indicates success (2xx status).
     */
    boolean isSuccessful();
    
    /**
     * Check if the response is a redirect (3xx status).
     */
    boolean isRedirect();
}
