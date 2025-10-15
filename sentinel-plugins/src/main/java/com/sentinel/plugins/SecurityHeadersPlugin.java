package com.sentinel.plugins;

import com.sentinel.plugin.api.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects missing or misconfigured security headers.
 * PASSIVE plugin - only analyzes existing responses.
 */
public class SecurityHeadersPlugin implements ScannerPlugin {
    
    private PluginConfig config;
    
    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata(
                "security-headers",
                "Security Headers Checker",
                "1.0.0",
                "Sentinel Team",
                "Checks for missing or misconfigured security headers",
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
        
        // Check for missing HSTS
        if (context.getRequest().isSecure() && !response.getHeader("Strict-Transport-Security").isPresent()) {
            findings.add(PluginFinding.confirmed("security-headers", "Missing HSTS Header")
                    .endpoint(context.getTargetUrl())
                    .severity(Severity.MEDIUM)
                    .description("The Strict-Transport-Security header is not set on this HTTPS endpoint")
                    .addEvidence(Evidence.headerValue("Missing", "Strict-Transport-Security"))
                    .remediation("Add 'Strict-Transport-Security: max-age=31536000; includeSubDomains' header")
                    .build());
        }
        
        // Check for missing X-Content-Type-Options
        if (!response.getHeader("X-Content-Type-Options").isPresent()) {
            findings.add(PluginFinding.confirmed("security-headers", "Missing X-Content-Type-Options")
                    .endpoint(context.getTargetUrl())
                    .severity(Severity.LOW)
                    .description("The X-Content-Type-Options header is not set")
                    .addEvidence(Evidence.headerValue("Missing", "X-Content-Type-Options"))
                    .remediation("Add 'X-Content-Type-Options: nosniff' header")
                    .build());
        }
        
        // Check for missing X-Frame-Options
        if (!response.getHeader("X-Frame-Options").isPresent() && 
            !response.getHeader("Content-Security-Policy").isPresent()) {
            findings.add(PluginFinding.likely("security-headers", "Missing Clickjacking Protection")
                    .endpoint(context.getTargetUrl())
                    .severity(Severity.MEDIUM)
                    .description("Neither X-Frame-Options nor CSP frame-ancestors directive is set")
                    .addEvidence(Evidence.headerValue("Missing", "X-Frame-Options and CSP"))
                    .remediation("Add 'X-Frame-Options: DENY' or 'Content-Security-Policy: frame-ancestors 'none''")
                    .build());
        }
        
        // Check for missing Content-Security-Policy
        if (!response.getHeader("Content-Security-Policy").isPresent()) {
            findings.add(PluginFinding.possible("security-headers", "Missing Content Security Policy")
                    .endpoint(context.getTargetUrl())
                    .severity(Severity.LOW)
                    .description("No Content-Security-Policy header detected")
                    .addEvidence(Evidence.headerValue("Missing", "Content-Security-Policy"))
                    .remediation("Implement a Content-Security-Policy appropriate for your application")
                    .notes("CSP helps prevent XSS and other injection attacks")
                    .build());
        }
        
        // Check for insecure cookies
        checkCookieSecurity(context, findings);
        
        return findings;
    }
    
    private void checkCookieSecurity(PluginContext context, List<PluginFinding> findings) {
        List<String> setCookieHeaders = context.getResponse().getHeaders().get("Set-Cookie");
        if (setCookieHeaders != null) {
            for (String cookie : setCookieHeaders) {
                String lowerCookie = cookie.toLowerCase();
                
                // Check for missing Secure flag on HTTPS
                if (context.getRequest().isSecure() && !lowerCookie.contains("secure")) {
                    findings.add(PluginFinding.confirmed("security-headers", "Cookie Missing Secure Flag")
                            .endpoint(context.getTargetUrl())
                            .severity(Severity.MEDIUM)
                            .description("Cookie set without Secure flag on HTTPS connection")
                            .addEvidence(Evidence.headerValue(cookie.substring(0, Math.min(50, cookie.length())), "Set-Cookie"))
                            .remediation("Add 'Secure' flag to all cookies on HTTPS")
                            .build());
                }
                
                // Check for missing HttpOnly flag
                if (!lowerCookie.contains("httponly")) {
                    findings.add(PluginFinding.likely("security-headers", "Cookie Missing HttpOnly Flag")
                            .endpoint(context.getTargetUrl())
                            .severity(Severity.LOW)
                            .description("Cookie set without HttpOnly flag")
                            .addEvidence(Evidence.headerValue(cookie.substring(0, Math.min(50, cookie.length())), "Set-Cookie"))
                            .remediation("Add 'HttpOnly' flag to cookies that don't need JavaScript access")
                            .build());
                }
                
                // Check for missing SameSite
                if (!lowerCookie.contains("samesite")) {
                    findings.add(PluginFinding.possible("security-headers", "Cookie Missing SameSite Attribute")
                            .endpoint(context.getTargetUrl())
                            .severity(Severity.LOW)
                            .description("Cookie set without SameSite attribute")
                            .addEvidence(Evidence.headerValue(cookie.substring(0, Math.min(50, cookie.length())), "Set-Cookie"))
                            .remediation("Add 'SameSite=Strict' or 'SameSite=Lax' to cookies")
                            .notes("SameSite helps prevent CSRF attacks")
                            .build());
                }
            }
        }
    }
    
    @Override
    public void configure(PluginConfig config) {
        this.config = config;
    }
}
