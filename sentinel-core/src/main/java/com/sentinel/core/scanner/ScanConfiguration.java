package com.sentinel.core.scanner;

import com.sentinel.plugin.api.SafetyLevel;
import java.net.URI;
import java.util.UUID;

public class ScanConfiguration {
    private final String scanId;
    private final URI targetUrl;
    private final SafetyLevel maxSafetyLevel;
    private final int maxCrawlDepth;
    private final double rateLimit;
    private final int concurrency;
    private final boolean respectRobotsTxt;
    private final boolean expertMode;
    
    private ScanConfiguration(Builder builder) {
        this.scanId = builder.scanId != null ? builder.scanId : UUID.randomUUID().toString();
        this.targetUrl = builder.targetUrl;
        this.maxSafetyLevel = builder.maxSafetyLevel;
        this.maxCrawlDepth = builder.maxCrawlDepth;
        this.rateLimit = builder.rateLimit;
        this.concurrency = builder.concurrency;
        this.respectRobotsTxt = builder.respectRobotsTxt;
        this.expertMode = builder.expertMode;
    }
    
    public String getScanId() { return scanId; }
    public URI getTargetUrl() { return targetUrl; }
    public SafetyLevel getMaxSafetyLevel() { return maxSafetyLevel; }
    public int getMaxCrawlDepth() { return maxCrawlDepth; }
    public double getRateLimit() { return rateLimit; }
    public int getConcurrency() { return concurrency; }
    public boolean isRespectRobotsTxt() { return respectRobotsTxt; }
    public boolean isExpertMode() { return expertMode; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String scanId;
        private URI targetUrl;
        private SafetyLevel maxSafetyLevel = SafetyLevel.PASSIVE;
        private int maxCrawlDepth = 10;
        private double rateLimit = 5.0;
        private int concurrency = 2;
        private boolean respectRobotsTxt = true;
        private boolean expertMode = false;
        
        public Builder scanId(String scanId) {
            this.scanId = scanId;
            return this;
        }
        
        public Builder targetUrl(URI targetUrl) {
            this.targetUrl = targetUrl;
            return this;
        }
        
        public Builder maxSafetyLevel(SafetyLevel level) {
            this.maxSafetyLevel = level;
            return this;
        }
        
        public Builder maxCrawlDepth(int depth) {
            this.maxCrawlDepth = depth;
            return this;
        }
        
        public Builder rateLimit(double rateLimit) {
            this.rateLimit = rateLimit;
            return this;
        }
        
        public Builder concurrency(int concurrency) {
            this.concurrency = concurrency;
            return this;
        }
        
        public Builder respectRobotsTxt(boolean respect) {
            this.respectRobotsTxt = respect;
            return this;
        }
        
        public Builder expertMode(boolean enabled) {
            this.expertMode = enabled;
            return this;
        }
        
        public ScanConfiguration build() {
            if (targetUrl == null) {
                throw new IllegalStateException("Target URL is required");
            }
            return new ScanConfiguration(this);
        }
    }
}
