package com.sentinel.plugin.api;

/**
 * Severity classification for security findings.
 */
public enum Severity {
    
    INFO(0, "Informational"),
    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High"),
    CRITICAL(4, "Critical");
    
    private final int level;
    private final String displayName;
    
    Severity(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isHigherThan(Severity other) {
        return this.level > other.level;
    }
}
