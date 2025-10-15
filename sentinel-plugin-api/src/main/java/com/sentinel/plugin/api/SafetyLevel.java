package com.sentinel.plugin.api;

/**
 * Safety level classification for scanner plugins.
 * Determines what permissions and consent are required to run the plugin.
 */
public enum SafetyLevel {
    
    /**
     * PASSIVE plugins only analyze existing request/response data.
     * No additional requests are made. Safe by default.
     * Examples: header analysis, response pattern matching, fingerprinting
     */
    PASSIVE(0, "Passive analysis only - no additional requests"),
    
    /**
     * ACTIVE plugins may send additional safe, non-destructive requests.
     * Requires explicit opt-in and signed consent.
     * Examples: probing for specific endpoints, testing for information disclosure
     */
    ACTIVE(1, "May send additional safe requests - requires consent"),
    
    /**
     * EXPERT plugins perform advanced testing that may be more intrusive.
     * Requires opt-in, signed consent, and "Expert Mode" confirmation.
     * Examples: timing-based tests, complex multi-step probes
     */
    EXPERT(2, "Advanced testing - requires expert mode and consent");
    
    private final int level;
    private final String description;
    
    SafetyLevel(int level, String description) {
        this.level = level;
        this.description = description;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this safety level requires more permissions than another.
     */
    public boolean requiresMorePermissionsThan(SafetyLevel other) {
        return this.level > other.level;
    }
}
