package com.sentinel.plugin.api;

/**
 * Confidence level for security findings.
 * Indicates how certain the plugin is that the finding is a true positive.
 */
public enum Confidence {
    
    /**
     * Finding is possible but needs verification.
     * Based on heuristics or single indicators.
     */
    POSSIBLE(0, "Possible", "Finding is possible but requires manual verification"),
    
    /**
     * Finding is likely based on multiple indicators.
     * Higher confidence but not definitively confirmed.
     */
    LIKELY(1, "Likely", "Finding is likely based on multiple indicators"),
    
    /**
     * Finding is confirmed through multiple independent checks.
     * High confidence, minimal false positive risk.
     */
    CONFIRMED(2, "Confirmed", "Finding confirmed through multiple independent checks");
    
    private final int level;
    private final String displayName;
    private final String description;
    
    Confidence(int level, String displayName, String description) {
        this.level = level;
        this.displayName = displayName;
        this.description = description;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isHigherThan(Confidence other) {
        return this.level > other.level;
    }
}
