package com.sentinel.plugin.api;

/**
 * Configuration for the current scan.
 * Provides context about scan settings to plugins.
 */
public interface ScanConfig {
    
    /**
     * Get the maximum allowed safety level for this scan.
     */
    SafetyLevel getMaxSafetyLevel();
    
    /**
     * Check if expert mode is enabled.
     */
    boolean isExpertModeEnabled();
    
    /**
     * Get the maximum crawl depth.
     */
    int getMaxCrawlDepth();
    
    /**
     * Get the request rate limit (requests per second).
     */
    double getRateLimit();
    
    /**
     * Check if the scan has valid consent.
     */
    boolean hasValidConsent();
    
    /**
     * Get the scan ID.
     */
    String getScanId();
    
    /**
     * Check if this is a dry-run (no actual requests).
     */
    boolean isDryRun();
}
