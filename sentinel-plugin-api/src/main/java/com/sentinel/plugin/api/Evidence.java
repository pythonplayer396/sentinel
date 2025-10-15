package com.sentinel.plugin.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Evidence supporting a security finding.
 * Must not contain sensitive data (credentials, tokens, etc.).
 */
public class Evidence {
    
    private final EvidenceType type;
    private final String snippet;
    private final String context;
    private final String description;
    
    @JsonCreator
    public Evidence(
            @JsonProperty("type") EvidenceType type,
            @JsonProperty("snippet") String snippet,
            @JsonProperty("context") String context,
            @JsonProperty("description") String description) {
        this.type = Objects.requireNonNull(type, "Evidence type cannot be null");
        this.snippet = snippet;
        this.context = context;
        this.description = description;
    }
    
    public EvidenceType getType() {
        return type;
    }
    
    public String getSnippet() {
        return snippet;
    }
    
    public String getContext() {
        return context;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static Evidence responseSnippet(String snippet, String context) {
        return new Evidence(EvidenceType.RESPONSE_SNIPPET, snippet, context, null);
    }
    
    public static Evidence headerValue(String snippet, String context) {
        return new Evidence(EvidenceType.HEADER_VALUE, snippet, context, null);
    }
    
    public static Evidence patternMatch(String snippet, String description) {
        return new Evidence(EvidenceType.PATTERN_MATCH, snippet, null, description);
    }
    
    public static Evidence timingData(String snippet, String description) {
        return new Evidence(EvidenceType.TIMING_DATA, snippet, null, description);
    }
    
    public enum EvidenceType {
        RESPONSE_SNIPPET,
        HEADER_VALUE,
        PATTERN_MATCH,
        TIMING_DATA,
        BEHAVIOR_OBSERVATION,
        CONFIGURATION_ISSUE
    }
}
