package com.sentinel.plugin.api;

/**
 * Describes the capabilities required by a plugin.
 */
public class PluginCapabilities {
    
    private final boolean needsHeadlessBrowser;
    private final boolean needsAuthentication;
    private final boolean needsJavaScriptExecution;
    private final boolean needsCustomHeaders;
    private final int maxConcurrentRequests;
    
    private PluginCapabilities(Builder builder) {
        this.needsHeadlessBrowser = builder.needsHeadlessBrowser;
        this.needsAuthentication = builder.needsAuthentication;
        this.needsJavaScriptExecution = builder.needsJavaScriptExecution;
        this.needsCustomHeaders = builder.needsCustomHeaders;
        this.maxConcurrentRequests = builder.maxConcurrentRequests;
    }
    
    public boolean needsHeadlessBrowser() {
        return needsHeadlessBrowser;
    }
    
    public boolean needsAuthentication() {
        return needsAuthentication;
    }
    
    public boolean needsJavaScriptExecution() {
        return needsJavaScriptExecution;
    }
    
    public boolean needsCustomHeaders() {
        return needsCustomHeaders;
    }
    
    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private boolean needsHeadlessBrowser = false;
        private boolean needsAuthentication = false;
        private boolean needsJavaScriptExecution = false;
        private boolean needsCustomHeaders = false;
        private int maxConcurrentRequests = 1;
        
        public Builder needsHeadlessBrowser(boolean needs) {
            this.needsHeadlessBrowser = needs;
            return this;
        }
        
        public Builder needsAuthentication(boolean needs) {
            this.needsAuthentication = needs;
            return this;
        }
        
        public Builder needsJavaScriptExecution(boolean needs) {
            this.needsJavaScriptExecution = needs;
            return this;
        }
        
        public Builder needsCustomHeaders(boolean needs) {
            this.needsCustomHeaders = needs;
            return this;
        }
        
        public Builder maxConcurrentRequests(int max) {
            this.maxConcurrentRequests = Math.max(1, max);
            return this;
        }
        
        public PluginCapabilities build() {
            return new PluginCapabilities(this);
        }
    }
}
