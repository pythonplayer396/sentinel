package com.sentinel.plugins;

import com.sentinel.plugin.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Discovers subdomains using passive and active techniques.
 * PASSIVE plugin - uses Certificate Transparency logs and DNS lookups.
 * No API keys required - works with free data sources.
 */
public class SubdomainFinderPlugin implements ScannerPlugin {
    
    private static final Logger logger = LoggerFactory.getLogger(SubdomainFinderPlugin.class);
    
    private PluginConfig config;
    private int maxSubdomains = 1000;
    private int timeoutSeconds = 30;
    private boolean enableCrtSh = true;
    private boolean enableDnsEnum = true;
    private boolean enableWildcardDetection = true;
    
    // Common subdomain wordlist (embedded)
    private static final String[] COMMON_SUBDOMAINS = {
        "www", "mail", "ftp", "localhost", "webmail", "smtp", "pop", "ns1", "webdisk",
        "ns2", "cpanel", "whm", "autodiscover", "autoconfig", "m", "imap", "test",
        "ns", "blog", "pop3", "dev", "www2", "admin", "forum", "news", "vpn",
        "ns3", "mail2", "new", "mysql", "old", "lists", "support", "mobile", "mx",
        "static", "docs", "beta", "shop", "sql", "secure", "demo", "cp", "calendar",
        "wiki", "web", "media", "email", "images", "img", "www1", "intranet", "portal",
        "video", "sip", "dns2", "api", "cdn", "stats", "dns1", "ns4", "www3", "dns",
        "search", "staging", "server", "mx1", "chat", "wap", "my", "svn", "mail1",
        "sites", "proxy", "ads", "host", "crm", "cms", "backup", "mx2", "lyncdiscover",
        "info", "apps", "download", "remote", "db", "forums", "store", "relay", "files",
        "newsletter", "app", "live", "owa", "en", "start", "sms", "office", "exchange",
        "ipv4", "gateway", "git", "status", "prod", "production", "stage", "dev2"
    };
    
    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata(
            "subdomain-finder",
            "Subdomain Discovery",
            "1.0.0",
            "Sentinel Team",
            "Discovers subdomains using Certificate Transparency logs and DNS enumeration",
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
        
        try {
            String domain = extractDomain(context.getTargetUrl());
            if (domain == null || domain.isEmpty()) {
                return findings;
            }
            
            logger.info("Starting subdomain discovery for: {}", domain);
            
            Set<String> discoveredSubdomains = new ConcurrentHashMap<String, Boolean>().keySet(true);
            
            // Check for wildcard DNS first
            boolean hasWildcard = false;
            if (enableWildcardDetection) {
                hasWildcard = detectWildcardDns(domain);
                if (hasWildcard) {
                    findings.add(PluginFinding.confirmed("subdomain-finder", "Wildcard DNS Detected")
                        .endpoint(context.getTargetUrl())
                        .severity(Severity.INFO)
                        .description("Domain uses wildcard DNS - all subdomains resolve")
                        .addEvidence(Evidence.patternMatch(domain, "Wildcard DNS configuration"))
                        .notes("Subdomain enumeration may produce false positives")
                        .build());
                }
            }
            
            // Method 1: Certificate Transparency logs (crt.sh)
            if (enableCrtSh) {
                Set<String> crtSubdomains = discoverViaCrtSh(domain);
                discoveredSubdomains.addAll(crtSubdomains);
                logger.info("Found {} subdomains via Certificate Transparency", crtSubdomains.size());
            }
            
            // Method 2: DNS enumeration with common names
            if (enableDnsEnum && !hasWildcard) {
                Set<String> dnsSubdomains = discoverViaDnsEnum(domain);
                discoveredSubdomains.addAll(dnsSubdomains);
                logger.info("Found {} subdomains via DNS enumeration", dnsSubdomains.size());
            }
            
            // Verify discovered subdomains
            Set<String> validSubdomains = verifySubdomains(discoveredSubdomains, hasWildcard);
            
            // Limit results
            List<String> limitedSubdomains = validSubdomains.stream()
                .limit(maxSubdomains)
                .sorted()
                .collect(Collectors.toList());
            
            if (!limitedSubdomains.isEmpty()) {
                findings.add(createSubdomainFinding(context.getTargetUrl(), domain, limitedSubdomains));
            }
            
            // Check for subdomain takeover vulnerabilities
            findings.addAll(checkSubdomainTakeover(limitedSubdomains));
            
            logger.info("Subdomain discovery complete. Found {} valid subdomains", limitedSubdomains.size());
            
        } catch (Exception e) {
            logger.error("Error during subdomain discovery", e);
            throw new PluginExecutionException("Subdomain discovery failed: " + e.getMessage(), e);
        }
        
        return findings;
    }
    
    /**
     * Extract base domain from URL
     */
    private String extractDomain(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            return null;
        }
        
        // Remove port if present
        host = host.split(":")[0];
        
        // For localhost/IP addresses, return as-is
        if (host.equals("localhost") || host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            return null;
        }
        
        return host;
    }
    
    /**
     * Detect wildcard DNS configuration
     */
    private boolean detectWildcardDns(String domain) {
        try {
            String randomSubdomain = UUID.randomUUID().toString().substring(0, 8) + "." + domain;
            InetAddress.getByName(randomSubdomain);
            return true; // If random subdomain resolves, it's a wildcard
        } catch (UnknownHostException e) {
            return false; // Good - random subdomain doesn't resolve
        }
    }
    
    /**
     * Discover subdomains via Certificate Transparency logs (crt.sh)
     */
    private Set<String> discoverViaCrtSh(String domain) {
        Set<String> subdomains = new HashSet<>();
        
        try {
            String apiUrl = "https://crt.sh/?q=%25." + domain + "&output=json";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeoutSeconds * 1000);
            conn.setReadTimeout(timeoutSeconds * 1000);
            conn.setRequestProperty("User-Agent", "Sentinel-Scanner/1.0");
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Parse JSON response (simple regex-based parsing to avoid Jackson dependency)
                Pattern pattern = Pattern.compile("\"name_value\":\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(response.toString());
                
                while (matcher.find()) {
                    String nameValue = matcher.group(1);
                    // Split by newlines (crt.sh returns multiple names in one field)
                    for (String name : nameValue.split("\\n")) {
                        name = name.trim().toLowerCase();
                        if (name.endsWith("." + domain) || name.equals(domain)) {
                            // Remove wildcards
                            name = name.replace("*.", "");
                            if (!name.isEmpty() && name.contains(".")) {
                                subdomains.add(name);
                            }
                        }
                    }
                }
            }
            
            conn.disconnect();
            
        } catch (IOException e) {
            logger.warn("Failed to query crt.sh: {}", e.getMessage());
        }
        
        return subdomains;
    }
    
    /**
     * Discover subdomains via DNS enumeration
     */
    private Set<String> discoverViaDnsEnum(String domain) {
        Set<String> subdomains = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        List<Future<?>> futures = new ArrayList<>();
        
        for (String prefix : COMMON_SUBDOMAINS) {
            futures.add(executor.submit(() -> {
                String subdomain = prefix + "." + domain;
                try {
                    InetAddress.getByName(subdomain);
                    subdomains.add(subdomain);
                } catch (UnknownHostException e) {
                    // Subdomain doesn't exist - ignore
                }
            }));
        }
        
        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
            }
        }
        
        executor.shutdown();
        
        return subdomains;
    }
    
    /**
     * Verify discovered subdomains resolve to valid IPs
     */
    private Set<String> verifySubdomains(Set<String> subdomains, boolean hasWildcard) {
        Set<String> valid = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(20);
        
        List<Future<?>> futures = new ArrayList<>();
        
        for (String subdomain : subdomains) {
            futures.add(executor.submit(() -> {
                try {
                    InetAddress addr = InetAddress.getByName(subdomain);
                    // If wildcard DNS, we can't reliably verify, so accept all from CT logs
                    if (hasWildcard || addr != null) {
                        valid.add(subdomain);
                    }
                } catch (UnknownHostException e) {
                    // Doesn't resolve - skip
                }
            }));
        }
        
        // Wait for verification
        for (Future<?> future : futures) {
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
            }
        }
        
        executor.shutdown();
        
        return valid;
    }
    
    /**
     * Check for potential subdomain takeover vulnerabilities
     */
    private List<PluginFinding> checkSubdomainTakeover(List<String> subdomains) {
        List<PluginFinding> findings = new ArrayList<>();
        
        // Patterns indicating potential takeover
        String[] takeoverPatterns = {
            "NoSuchBucket",
            "No Such Account",
            "There isn't a GitHub Pages site here",
            "The specified bucket does not exist",
            "Repository not found",
            "Project not found",
            "Fastly error: unknown domain"
        };
        
        for (String subdomain : subdomains) {
            try {
                URL url = new URL("http://" + subdomain);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestProperty("User-Agent", "Sentinel-Scanner/1.0");
                
                int responseCode = conn.getResponseCode();
                
                if (responseCode == 404) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    String body = response.toString();
                    for (String pattern : takeoverPatterns) {
                        if (body.contains(pattern)) {
                            findings.add(PluginFinding.likely("subdomain-finder", "Potential Subdomain Takeover")
                                .endpoint(URI.create("http://" + subdomain))
                                .severity(Severity.HIGH)
                                .description("Subdomain may be vulnerable to takeover")
                                .addEvidence(Evidence.responseSnippet(pattern, "HTTP Response"))
                                .remediation("Verify subdomain ownership and remove dangling DNS records")
                                .build());
                            break;
                        }
                    }
                }
                
                conn.disconnect();
                
            } catch (Exception e) {
                // Ignore connection errors
            }
        }
        
        return findings;
    }
    
    /**
     * Create finding with discovered subdomains
     */
    private PluginFinding createSubdomainFinding(URI targetUrl, String domain, List<String> subdomains) {
        StringBuilder description = new StringBuilder();
        description.append("Discovered ").append(subdomains.size()).append(" subdomains for ").append(domain);
        
        StringBuilder evidence = new StringBuilder();
        int displayLimit = Math.min(50, subdomains.size());
        for (int i = 0; i < displayLimit; i++) {
            evidence.append(subdomains.get(i)).append("\n");
        }
        if (subdomains.size() > displayLimit) {
            evidence.append("... and ").append(subdomains.size() - displayLimit).append(" more");
        }
        
        return PluginFinding.confirmed("subdomain-finder", "Subdomains Discovered")
            .endpoint(targetUrl)
            .severity(Severity.INFO)
            .description(description.toString())
            .addEvidence(Evidence.patternMatch(evidence.toString(), "Discovered Subdomains"))
            .notes("Review subdomains for exposed services and misconfigurations")
            .build();
    }
    
    @Override
    public void configure(PluginConfig config) {
        this.config = config;
        
        // Read configuration
        if (config != null) {
            maxSubdomains = config.getInt("max-subdomains", 1000);
            timeoutSeconds = config.getInt("timeout-seconds", 30);
            enableCrtSh = config.getBoolean("enable-crtsh", true);
            enableDnsEnum = config.getBoolean("enable-dns-enum", true);
            enableWildcardDetection = config.getBoolean("enable-wildcard-detection", true);
        }
    }
}
