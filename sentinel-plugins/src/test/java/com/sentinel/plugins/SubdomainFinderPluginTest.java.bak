package com.sentinel.plugins;

import com.sentinel.plugin.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SubdomainFinderPlugin
 */
class SubdomainFinderPluginTest {
    
    private SubdomainFinderPlugin plugin;
    
    @BeforeEach
    void setUp() {
        plugin = new SubdomainFinderPlugin();
        plugin.initialize();
    }
    
    @Test
    void testPluginMetadata() {
        PluginMetadata metadata = plugin.getMetadata();
        
        assertNotNull(metadata);
        assertEquals("subdomain-finder", metadata.getId());
        assertEquals("Subdomain Discovery", metadata.getName());
        assertEquals("1.0.0", metadata.getVersion());
        assertEquals(SafetyLevel.PASSIVE, metadata.getSafetyLevel());
    }
    
    @Test
    void testPluginCapabilities() {
        PluginCapabilities capabilities = plugin.getCapabilities();
        assertNotNull(capabilities);
    }
    
    @Test
    void testConfiguration() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("max-subdomains", 500);
        configMap.put("timeout-seconds", 15);
        configMap.put("enable-crtsh", true);
        
        PluginConfig config = new PluginConfig(configMap);
        plugin.configure(config);
        
        // Plugin should accept configuration without errors
        assertNotNull(plugin);
    }
    
    @Test
    void testRunWithValidDomain() throws Exception {
        // Create mock context for a real domain (example.com)
        URI targetUrl = URI.create("https://example.com");
        HttpRequest request = createMockRequest(targetUrl);
        HttpResponse response = createMockResponse();
        
        PluginContext context = new PluginContext(targetUrl, request, response, null);
        
        // Note: This test will make real network calls to crt.sh
        // In a production environment, you'd want to mock these
        List<PluginFinding> findings = plugin.run(context);
        
        assertNotNull(findings);
        // Findings may be empty if no subdomains are discovered
    }
    
    @Test
    void testRunWithLocalhost() throws Exception {
        // Localhost should not trigger subdomain discovery
        URI targetUrl = URI.create("http://localhost:3000");
        HttpRequest request = createMockRequest(targetUrl);
        HttpResponse response = createMockResponse();
        
        PluginContext context = new PluginContext(targetUrl, request, response, null);
        
        List<PluginFinding> findings = plugin.run(context);
        
        assertNotNull(findings);
        assertTrue(findings.isEmpty(), "Localhost should not trigger subdomain discovery");
    }
    
    @Test
    void testRunWithIpAddress() throws Exception {
        // IP addresses should not trigger subdomain discovery
        URI targetUrl = URI.create("http://192.168.1.1");
        HttpRequest request = createMockRequest(targetUrl);
        HttpResponse response = createMockResponse();
        
        PluginContext context = new PluginContext(targetUrl, request, response, null);
        
        List<PluginFinding> findings = plugin.run(context);
        
        assertNotNull(findings);
        assertTrue(findings.isEmpty(), "IP addresses should not trigger subdomain discovery");
    }
    
    private HttpRequest createMockRequest(URI uri) {
        return new HttpRequest() {
            @Override
            public String getMethod() {
                return "GET";
            }
            
            @Override
            public URI getUri() {
                return uri;
            }
            
            @Override
            public Map<String, String> getHeaders() {
                return new HashMap<>();
            }
            
            @Override
            public String getBody() {
                return "";
            }
            
            @Override
            public boolean isSecure() {
                return uri.getScheme().equals("https");
            }
        };
    }
    
    private HttpResponse createMockResponse() {
        return new HttpResponse() {
            @Override
            public int getStatusCode() {
                return 200;
            }
            
            @Override
            public String getStatusMessage() {
                return "OK";
            }
            
            @Override
            public Map<String, List<String>> getHeaders() {
                return new HashMap<>();
            }
            
            @Override
            public java.util.Optional<String> getHeader(String name) {
                return java.util.Optional.empty();
            }
            
            @Override
            public String getBody() {
                return "<html><body>Test</body></html>";
            }
            
            @Override
            public long getResponseTimeMs() {
                return 100;
            }
        };
    }
}
