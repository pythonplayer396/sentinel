package com.sentinel.plugin.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a security finding discovered by a plugin.
 * Immutable and JSON-serializable.
 */
public class PluginFinding {
    
    private final String findingId;
    private final String pluginId;
    private final URI endpoint;
    private final String parameter;
    private final String title;
    private final String description;
    private final Severity severity;
    private final Confidence confidence;
    private final List<Evidence> evidence;
    private final String remediation;
    private final String notes;
    private final Instant timestamp;
    
    @JsonCreator
    private PluginFinding(
            @JsonProperty("findingId") String findingId,
            @JsonProperty("pluginId") String pluginId,
            @JsonProperty("endpoint") URI endpoint,
            @JsonProperty("parameter") String parameter,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("severity") Severity severity,
            @JsonProperty("confidence") Confidence confidence,
            @JsonProperty("evidence") List<Evidence> evidence,
            @JsonProperty("remediation") String remediation,
            @JsonProperty("notes") String notes,
            @JsonProperty("timestamp") Instant timestamp) {
        this.findingId = findingId != null ? findingId : UUID.randomUUID().toString();
        this.pluginId = Objects.requireNonNull(pluginId, "Plugin ID cannot be null");
        this.endpoint = Objects.requireNonNull(endpoint, "Endpoint cannot be null");
        this.parameter = parameter;
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = description;
        this.severity = Objects.requireNonNull(severity, "Severity cannot be null");
        this.confidence = Objects.requireNonNull(confidence, "Confidence cannot be null");
        this.evidence = evidence != null ? new ArrayList<>(evidence) : new ArrayList<>();
        this.remediation = remediation;
        this.notes = notes;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }
    
    public String getFindingId() {
        return findingId;
    }
    
    public String getPluginId() {
        return pluginId;
    }
    
    public URI getEndpoint() {
        return endpoint;
    }
    
    public String getParameter() {
        return parameter;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public Confidence getConfidence() {
        return confidence;
    }
    
    public List<Evidence> getEvidence() {
        return new ArrayList<>(evidence);
    }
    
    public String getRemediation() {
        return remediation;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Quick factory for POSSIBLE confidence findings.
     */
    public static Builder possible(String pluginId, String title) {
        return builder()
                .pluginId(pluginId)
                .title(title)
                .confidence(Confidence.POSSIBLE);
    }
    
    /**
     * Quick factory for LIKELY confidence findings.
     */
    public static Builder likely(String pluginId, String title) {
        return builder()
                .pluginId(pluginId)
                .title(title)
                .confidence(Confidence.LIKELY);
    }
    
    /**
     * Quick factory for CONFIRMED confidence findings.
     */
    public static Builder confirmed(String pluginId, String title) {
        return builder()
                .pluginId(pluginId)
                .title(title)
                .confidence(Confidence.CONFIRMED);
    }
    
    public static class Builder {
        private String findingId;
        private String pluginId;
        private URI endpoint;
        private String parameter;
        private String title;
        private String description;
        private Severity severity = Severity.LOW;
        private Confidence confidence = Confidence.POSSIBLE;
        private List<Evidence> evidence = new ArrayList<>();
        private String remediation;
        private String notes;
        private Instant timestamp;
        
        public Builder findingId(String findingId) {
            this.findingId = findingId;
            return this;
        }
        
        public Builder pluginId(String pluginId) {
            this.pluginId = pluginId;
            return this;
        }
        
        public Builder endpoint(URI endpoint) {
            this.endpoint = endpoint;
            return this;
        }
        
        public Builder parameter(String parameter) {
            this.parameter = parameter;
            return this;
        }
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder confidence(Confidence confidence) {
            this.confidence = confidence;
            return this;
        }
        
        public Builder addEvidence(Evidence evidence) {
            this.evidence.add(evidence);
            return this;
        }
        
        public Builder evidence(List<Evidence> evidence) {
            this.evidence = new ArrayList<>(evidence);
            return this;
        }
        
        public Builder remediation(String remediation) {
            this.remediation = remediation;
            return this;
        }
        
        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public PluginFinding build() {
            return new PluginFinding(findingId, pluginId, endpoint, parameter, title,
                    description, severity, confidence, evidence, remediation, notes, timestamp);
        }
    }
}
