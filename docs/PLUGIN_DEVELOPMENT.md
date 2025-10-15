# Plugin Development Guide

## Overview

Sentinel's plugin system allows you to extend the scanner with custom detection modules. This guide covers everything you need to know to develop safe, effective plugins.

## Plugin Architecture

### Core Interfaces

Every plugin must implement the `ScannerPlugin` interface:

```java
public interface ScannerPlugin {
    PluginMetadata getMetadata();
    PluginCapabilities getCapabilities();
    List<PluginFinding> run(PluginContext context) throws PluginExecutionException;
    void configure(PluginConfig config);
    void initialize();
    void shutdown();
}
```

### Plugin Lifecycle

1. **Load** - Plugin JAR loaded via ServiceLoader
2. **Initialize** - `initialize()` called once
3. **Configure** - `configure()` called with config
4. **Execute** - `run()` called for each target
5. **Shutdown** - `shutdown()` called on unload

## Safety Levels

### PASSIVE (Recommended)

**Use for**: Analysis of existing responses only

```java
@Override
public PluginMetadata getMetadata() {
    return new PluginMetadata(
        "my-plugin",
        "My Passive Check",
        "1.0.0",
        "Author",
        "Description",
        SafetyLevel.PASSIVE  // ← Safe by default
    );
}
```

**Examples**:
- Header analysis
- Response pattern matching
- Version fingerprinting
- Error message detection

### ACTIVE (Requires Consent)

**Use for**: Additional safe, non-destructive requests

```java
SafetyLevel.ACTIVE  // Requires explicit consent
```

**Examples**:
- Probing for specific files
- Testing for information disclosure
- Checking for common paths

### EXPERT (Requires Expert Mode + Consent)

**Use for**: Advanced testing techniques

```java
SafetyLevel.EXPERT  // Requires expert mode
```

**Examples**:
- Timing-based tests
- Complex multi-step probes
- Advanced fingerprinting

## Plugin Template

### Basic Plugin Structure

```java
package com.example.plugins;

import com.sentinel.plugin.api.*;
import java.util.ArrayList;
import java.util.List;

public class MySecurityPlugin implements ScannerPlugin {
    
    private PluginConfig config;
    
    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata(
            "my-security-check",           // Unique ID
            "My Security Check",            // Display name
            "1.0.0",                        // Version
            "Your Name",                    // Author
            "Detects XYZ vulnerability",   // Description
            SafetyLevel.PASSIVE             // Safety level
        );
    }
    
    @Override
    public PluginCapabilities getCapabilities() {
        return PluginCapabilities.builder()
            .needsHeadlessBrowser(false)
            .needsAuthentication(false)
            .needsJavaScriptExecution(false)
            .maxConcurrentRequests(1)
            .build();
    }
    
    @Override
    public List<PluginFinding> run(PluginContext context) 
            throws PluginExecutionException {
        List<PluginFinding> findings = new ArrayList<>();
        
        // Your detection logic here
        HttpResponse response = context.getResponse();
        
        if (detectVulnerability(response)) {
            findings.add(createFinding(context));
        }
        
        return findings;
    }
    
    private boolean detectVulnerability(HttpResponse response) {
        // Detection logic
        return false;
    }
    
    private PluginFinding createFinding(PluginContext context) {
        return PluginFinding.confirmed("my-security-check", "Issue Found")
            .endpoint(context.getTargetUrl())
            .severity(Severity.MEDIUM)
            .description("Detailed description of the issue")
            .addEvidence(Evidence.responseSnippet("snippet", "context"))
            .remediation("How to fix this issue")
            .notes("Additional context")
            .build();
    }
    
    @Override
    public void configure(PluginConfig config) {
        this.config = config;
    }
    
    @Override
    public void initialize() {
        // One-time setup
    }
    
    @Override
    public void shutdown() {
        // Cleanup resources
    }
}
```

## Working with Context

### Accessing Request Data

```java
PluginContext context = ...;

// Get target URL
URI url = context.getTargetUrl();

// Get request details
HttpRequest request = context.getRequest();
String method = request.getMethod();
boolean isSecure = request.isSecure();

// Get parameters
Map<String, String> params = context.getAllParameters();
Optional<String> param = context.getParameterValue("id");
```

### Accessing Response Data

```java
HttpResponse response = context.getResponse();

// Status
int status = response.getStatusCode();
boolean success = response.isSuccessful();

// Headers
Optional<String> contentType = response.getContentType();
Optional<String> server = response.getHeader("Server");
Map<String, List<String>> allHeaders = response.getHeaders();

// Body
String body = response.getBody();
String truncated = response.getBodyTruncated(1000);

// Timing
long responseTime = response.getResponseTimeMs();
```

### Using the Logger

```java
PluginLogger logger = context.getLogger();

logger.debug("Checking for vulnerability");
logger.info("Found {} potential issues", count);
logger.warn("Unexpected response: {}", status);
logger.error("Plugin execution failed", exception);
```

## Creating Findings

### Finding Builder

```java
PluginFinding finding = PluginFinding.builder()
    .pluginId("my-plugin")
    .title("SQL Injection Possible")
    .endpoint(context.getTargetUrl())
    .parameter("id")
    .severity(Severity.HIGH)
    .confidence(Confidence.LIKELY)
    .description("Parameter 'id' may be vulnerable to SQL injection")
    .addEvidence(Evidence.responseSnippet(errorMsg, "response_body"))
    .remediation("Use parameterized queries")
    .notes("Detected SQL error message in response")
    .build();
```

### Severity Levels

```java
Severity.INFO       // Informational only
Severity.LOW        // Minor issue
Severity.MEDIUM     // Moderate risk
Severity.HIGH       // Serious vulnerability
Severity.CRITICAL   // Critical security issue
```

### Confidence Levels

```java
Confidence.POSSIBLE   // Single indicator, needs verification
Confidence.LIKELY     // Multiple indicators
Confidence.CONFIRMED  // Verified through multiple checks
```

### Evidence Types

```java
// Response snippet
Evidence.responseSnippet("error message", "response_body");

// Header value
Evidence.headerValue("Apache/2.4.1", "Server");

// Pattern match
Evidence.patternMatch("SQL syntax error", "Error pattern detected");

// Timing data
Evidence.timingData("1500ms", "Response time indicates possible issue");
```

## Best Practices

### 1. Be Conservative with Severity

```java
// ❌ Don't over-classify
.severity(Severity.CRITICAL)  // For minor issues

// ✅ Do classify appropriately
.severity(Severity.LOW)       // For informational findings
```

### 2. Provide Context

```java
// ✅ Good: Detailed description
.description("Missing X-Frame-Options header allows clickjacking attacks")
.remediation("Add 'X-Frame-Options: DENY' header to all responses")
.notes("This header prevents the page from being embedded in iframes")
```

### 3. Use Multiple Checks

```java
// ✅ Good: Multiple indicators
boolean hasErrorMessage = body.contains("SQL syntax");
boolean hasDbInfo = body.contains("mysql_");
boolean slowResponse = response.getResponseTimeMs() > 5000;

if (hasErrorMessage && hasDbInfo) {
    // Higher confidence
    return Confidence.LIKELY;
}
```

### 4. Avoid False Positives

```java
// ✅ Good: Context-aware checking
if (response.getContentType().map(ct -> ct.contains("text/html")).orElse(false)) {
    // Only check HTML responses
    if (body.contains(userInput)) {
        // Check if properly encoded
        if (!isProperlyEncoded(body, userInput)) {
            // Report finding
        }
    }
}
```

### 5. Handle Errors Gracefully

```java
@Override
public List<PluginFinding> run(PluginContext context) 
        throws PluginExecutionException {
    try {
        return performCheck(context);
    } catch (Exception e) {
        context.getLogger().error("Plugin execution failed", e);
        throw new PluginExecutionException("Failed to execute plugin", e);
    }
}
```

## Testing Your Plugin

### Unit Test Template

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MySecurityPluginTest {
    
    @Test
    void testDetectsVulnerability() {
        MySecurityPlugin plugin = new MySecurityPlugin();
        
        // Create mock context
        PluginContext context = createMockContext(
            "http://example.com",
            200,
            "Response with vulnerability indicator"
        );
        
        // Execute plugin
        List<PluginFinding> findings = plugin.run(context);
        
        // Verify
        assertEquals(1, findings.size());
        assertEquals(Severity.MEDIUM, findings.get(0).getSeverity());
    }
    
    @Test
    void testNoFalsePositives() {
        MySecurityPlugin plugin = new MySecurityPlugin();
        
        PluginContext context = createMockContext(
            "http://example.com",
            200,
            "Normal response"
        );
        
        List<PluginFinding> findings = plugin.run(context);
        
        assertTrue(findings.isEmpty());
    }
}
```

## Packaging Your Plugin

### 1. Create META-INF/services File

Create: `src/main/resources/META-INF/services/com.sentinel.plugin.api.ScannerPlugin`

```
com.example.plugins.MySecurityPlugin
```

### 2. Build JAR

```bash
mvn clean package
```

### 3. Install Plugin

Copy the JAR to Sentinel's plugin directory:

```bash
cp target/my-plugin-1.0.0.jar ~/.sentinel/plugins/
```

### 4. Verify Installation

```bash
sentinel plugin list
```

## Example Plugins

### Example 1: Missing Security Header

```java
public class MissingHeaderPlugin implements ScannerPlugin {
    
    @Override
    public List<PluginFinding> run(PluginContext context) {
        List<PluginFinding> findings = new ArrayList<>();
        HttpResponse response = context.getResponse();
        
        if (!response.getHeader("X-Content-Type-Options").isPresent()) {
            findings.add(PluginFinding.confirmed("missing-header", 
                    "Missing X-Content-Type-Options")
                .endpoint(context.getTargetUrl())
                .severity(Severity.LOW)
                .remediation("Add 'X-Content-Type-Options: nosniff'")
                .build());
        }
        
        return findings;
    }
}
```

### Example 2: Version Disclosure

```java
public class VersionDisclosurePlugin implements ScannerPlugin {
    
    private static final Pattern VERSION_PATTERN = 
        Pattern.compile("(Apache|nginx|IIS)/([0-9.]+)");
    
    @Override
    public List<PluginFinding> run(PluginContext context) {
        List<PluginFinding> findings = new ArrayList<>();
        
        context.getResponse().getHeader("Server").ifPresent(server -> {
            Matcher matcher = VERSION_PATTERN.matcher(server);
            if (matcher.find()) {
                findings.add(PluginFinding.confirmed("version-disclosure",
                        "Server Version Disclosure")
                    .endpoint(context.getTargetUrl())
                    .severity(Severity.LOW)
                    .addEvidence(Evidence.headerValue(server, "Server"))
                    .remediation("Configure server to hide version")
                    .build());
            }
        });
        
        return findings;
    }
}
```

## Common Patterns

### Pattern Matching

```java
private static final Pattern SQL_ERROR = Pattern.compile(
    "SQL syntax|mysql_fetch|ORA-[0-9]+",
    Pattern.CASE_INSENSITIVE
);

Matcher matcher = SQL_ERROR.matcher(response.getBody());
if (matcher.find()) {
    // Found SQL error
}
```

### Context Detection

```java
private String detectContext(String body, String value) {
    int index = body.indexOf(value);
    String context = body.substring(
        Math.max(0, index - 50),
        Math.min(body.length(), index + 50)
    );
    
    if (context.contains("<script")) return "javascript";
    if (context.contains("href=")) return "attribute";
    return "text";
}
```

### Multi-Check Validation

```java
private Confidence determineConfidence(HttpResponse response) {
    int indicators = 0;
    
    if (response.getBody().contains("error")) indicators++;
    if (response.getStatusCode() == 500) indicators++;
    if (response.getResponseTimeMs() > 5000) indicators++;
    
    if (indicators >= 3) return Confidence.CONFIRMED;
    if (indicators >= 2) return Confidence.LIKELY;
    return Confidence.POSSIBLE;
}
```

## Troubleshooting

### Plugin Not Loading

1. Check META-INF/services file exists
2. Verify class implements ScannerPlugin
3. Ensure JAR is in plugins directory
4. Check logs for errors

### Plugin Timing Out

1. Reduce processing complexity
2. Add early returns
3. Check for infinite loops
4. Profile performance

### False Positives

1. Add more validation checks
2. Use context-aware detection
3. Increase confidence threshold
4. Test against known-good targets

## Resources

- [Plugin API Javadoc](../javadoc/)
- [Example Plugins](../sentinel-plugins/)
- [Testing Guide](TESTING.md)
- [Ethical Charter](../ETHICAL_CHARTER.md)

## Support

For plugin development questions:
- GitHub Discussions
- Issue Tracker
- Community Forum
