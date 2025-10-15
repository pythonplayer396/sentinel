package com.sentinel.plugins;

import com.sentinel.plugin.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects information disclosure in responses.
 * PASSIVE plugin - only analyzes existing responses.
 */
public class InformationDisclosurePlugin implements ScannerPlugin {
    
    private static final Pattern SERVER_VERSION_PATTERN = Pattern.compile(
            "(Apache|nginx|IIS|Tomcat|Jetty)/([0-9.]+)",
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern PHP_VERSION_PATTERN = Pattern.compile(
            "X-Powered-By:\\s*PHP/([0-9.]+)",
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern STACK_TRACE_PATTERN = Pattern.compile(
            "(at\\s+[a-zA-Z0-9_.]+\\([^)]+\\)|Exception in thread|Caused by:|Stack trace:)",
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern SQL_ERROR_PATTERN = Pattern.compile(
            "(SQL syntax|mysql_fetch|PostgreSQL.*ERROR|ORA-[0-9]+|Microsoft SQL Server)",
            Pattern.CASE_INSENSITIVE
    );
    
    private PluginConfig config;
    
    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata(
                "info-disclosure",
                "Information Disclosure Detector",
                "1.0.0",
                "Sentinel Team",
                "Detects information leakage in responses",
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
        HttpResponse response = context.getResponse();
        
        // Check Server header for version disclosure
        response.getHeader("Server").ifPresent(serverHeader -> {
            Matcher matcher = SERVER_VERSION_PATTERN.matcher(serverHeader);
            if (matcher.find()) {
                findings.add(PluginFinding.confirmed("info-disclosure", "Server Version Disclosure")
                        .endpoint(context.getTargetUrl())
                        .severity(Severity.LOW)
                        .description("Server header reveals software version")
                        .addEvidence(Evidence.headerValue(serverHeader, "Server"))
                        .remediation("Configure server to hide version information")
                        .build());
            }
        });
        
        // Check X-Powered-By header
        response.getHeader("X-Powered-By").ifPresent(poweredBy -> {
            findings.add(PluginFinding.confirmed("info-disclosure", "X-Powered-By Header Disclosure")
                    .endpoint(context.getTargetUrl())
                    .severity(Severity.LOW)
                    .description("X-Powered-By header reveals technology stack")
                    .addEvidence(Evidence.headerValue(poweredBy, "X-Powered-By"))
                    .remediation("Remove or disable X-Powered-By header")
                    .build());
        });
        
        // Check response body for stack traces
        String body = response.getBody();
        if (body != null && !body.isEmpty()) {
            Matcher stackTraceMatcher = STACK_TRACE_PATTERN.matcher(body);
            if (stackTraceMatcher.find()) {
                String snippet = extractSnippet(body, stackTraceMatcher.start(), 100);
                findings.add(PluginFinding.confirmed("info-disclosure", "Stack Trace Disclosure")
                        .endpoint(context.getTargetUrl())
                        .severity(Severity.MEDIUM)
                        .description("Response contains stack trace information")
                        .addEvidence(Evidence.responseSnippet(snippet, "response_body"))
                        .remediation("Configure error handling to show generic error pages")
                        .notes("Stack traces can reveal internal application structure")
                        .build());
            }
            
            // Check for SQL error messages
            Matcher sqlErrorMatcher = SQL_ERROR_PATTERN.matcher(body);
            if (sqlErrorMatcher.find()) {
                String snippet = extractSnippet(body, sqlErrorMatcher.start(), 100);
                findings.add(PluginFinding.confirmed("info-disclosure", "SQL Error Message Disclosure")
                        .endpoint(context.getTargetUrl())
                        .severity(Severity.MEDIUM)
                        .description("Response contains SQL error messages")
                        .addEvidence(Evidence.responseSnippet(snippet, "response_body"))
                        .remediation("Implement proper error handling and logging")
                        .notes("SQL errors can aid SQL injection attacks")
                        .build());
            }
            
            // Check for common debug/development indicators
            if (body.contains("DEBUG") || body.contains("TRACE") || body.contains("development mode")) {
                findings.add(PluginFinding.likely("info-disclosure", "Debug Information Disclosure")
                        .endpoint(context.getTargetUrl())
                        .severity(Severity.LOW)
                        .description("Response may contain debug information")
                        .addEvidence(Evidence.patternMatch("Debug indicators found", "response_body"))
                        .remediation("Disable debug mode in production")
                        .build());
            }
        }
        
        return findings;
    }
    
    private String extractSnippet(String text, int position, int length) {
        int start = Math.max(0, position - 20);
        int end = Math.min(text.length(), position + length);
        String snippet = text.substring(start, end);
        return snippet.replaceAll("\\s+", " ").trim();
    }
    
    @Override
    public void configure(PluginConfig config) {
        this.config = config;
    }
}
