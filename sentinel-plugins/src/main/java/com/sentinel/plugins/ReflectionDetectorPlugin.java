package com.sentinel.plugins;

import com.sentinel.plugin.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Detects parameter reflection in responses (potential XSS indicator).
 * PASSIVE plugin - only analyzes existing request/response pairs.
 */
public class ReflectionDetectorPlugin implements ScannerPlugin {
    
    private PluginConfig config;
    
    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata(
                "reflection-detector",
                "Parameter Reflection Detector",
                "1.0.0",
                "Sentinel Team",
                "Detects parameter values reflected in responses",
                SafetyLevel.PASSIVE
        );
    }
    
    @Override
    public PluginCapabilities getCapabilities() {
        return PluginCapabilities.builder().build();
    }
    
    @Override
    public List<PluginFinding> run(PluginContext context) throws PluginExecutionException {
        List<PluginFinding> findings = new ArrayList<>();
        
        Map<String, String> params = context.getAllParameters();
        if (params.isEmpty()) {
            return findings; // No parameters to check
        }
        
        String responseBody = context.getResponse().getBody();
        if (responseBody == null || responseBody.isEmpty()) {
            return findings;
        }
        
        // Check each parameter for reflection
        for (Map.Entry<String, String> param : params.entrySet()) {
            String paramName = param.getKey();
            String paramValue = param.getValue();
            
            if (paramValue == null || paramValue.length() < 3) {
                continue; // Skip very short values to reduce false positives
            }
            
            if (responseBody.contains(paramValue)) {
                // Found reflection - determine context
                String context_type = determineReflectionContext(responseBody, paramValue);
                Severity severity = determineSeverity(context_type);
                Confidence confidence = determineConfidence(context_type, paramValue);
                
                findings.add(PluginFinding.builder()
                        .pluginId("reflection-detector")
                        .title("Parameter Reflection Detected")
                        .endpoint(context.getTargetUrl())
                        .parameter(paramName)
                        .severity(severity)
                        .confidence(confidence)
                        .description(String.format(
                                "Parameter '%s' is reflected in the response (%s context)",
                                paramName, context_type
                        ))
                        .addEvidence(Evidence.responseSnippet(
                                extractReflectionSnippet(responseBody, paramValue),
                                "response_body"
                        ))
                        .remediation("Sanitize and encode all user input before rendering. Use context-appropriate encoding (HTML, JavaScript, URL)")
                        .notes(String.format(
                                "Reflection detected in %s context. " +
                                "This is a potential XSS indicator but requires manual verification. " +
                                "Check if proper encoding is applied.",
                                context_type
                        ))
                        .build());
            }
        }
        
        return findings;
    }
    
    private String determineReflectionContext(String body, String value) {
        int index = body.indexOf(value);
        if (index == -1) return "unknown";
        
        // Look at surrounding context
        int start = Math.max(0, index - 50);
        int end = Math.min(body.length(), index + value.length() + 50);
        String context = body.substring(start, end).toLowerCase();
        
        if (context.contains("<script")) {
            return "javascript";
        } else if (context.contains("href=") || context.contains("src=")) {
            return "attribute";
        } else if (context.contains("<") && context.contains(">")) {
            return "html";
        } else if (context.contains("style=")) {
            return "css";
        }
        
        return "text";
    }
    
    private Severity determineSeverity(String context) {
        switch (context) {
            case "javascript":
            case "attribute":
                return Severity.HIGH;
            case "html":
                return Severity.MEDIUM;
            case "css":
                return Severity.LOW;
            default:
                return Severity.LOW;
        }
    }
    
    private Confidence determineConfidence(String context, String value) {
        // Higher confidence for dangerous contexts
        if (context.equals("javascript") || context.equals("attribute")) {
            return Confidence.LIKELY;
        }
        
        // Lower confidence for plain text (might be intentional)
        if (context.equals("text")) {
            return Confidence.POSSIBLE;
        }
        
        return Confidence.LIKELY;
    }
    
    private String extractReflectionSnippet(String body, String value) {
        int index = body.indexOf(value);
        if (index == -1) return "";
        
        int start = Math.max(0, index - 30);
        int end = Math.min(body.length(), index + value.length() + 30);
        
        String snippet = body.substring(start, end);
        return snippet.replaceAll("\\s+", " ").trim() + "...";
    }
    
    @Override
    public void configure(PluginConfig config) {
        this.config = config;
    }
}
