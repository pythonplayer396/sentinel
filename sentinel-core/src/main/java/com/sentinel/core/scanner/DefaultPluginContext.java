package com.sentinel.core.scanner;

import com.sentinel.core.crawler.CrawledPage;
import com.sentinel.core.http.HttpResponseData;
import com.sentinel.plugin.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

class DefaultPluginContext implements PluginContext {
    
    private final CrawledPage page;
    private final ScanConfiguration config;
    private final DefaultHttpRequest request;
    private final DefaultHttpResponse response;
    
    public DefaultPluginContext(CrawledPage page, ScanConfiguration config) {
        this.page = page;
        this.config = config;
        this.request = new DefaultHttpRequest(page.getUrl());
        this.response = new DefaultHttpResponse(page.getResponse());
    }
    
    @Override
    public URI getTargetUrl() {
        return page.getUrl();
    }
    
    @Override
    public HttpRequest getRequest() {
        return request;
    }
    
    @Override
    public HttpResponse getResponse() {
        return response;
    }
    
    @Override
    public Optional<String> getParameterValue(String name) {
        return Optional.ofNullable(request.getQueryParameters().get(name));
    }
    
    @Override
    public Map<String, String> getAllParameters() {
        return request.getQueryParameters();
    }
    
    @Override
    public int getCrawlDepth() {
        return page.getDepth();
    }
    
    @Override
    public boolean isJavaScriptAvailable() {
        return false; // TODO: Implement headless browser support
    }
    
    @Override
    public boolean isAuthenticated() {
        return false; // TODO: Implement authentication support
    }
    
    @Override
    public ScanConfig getScanConfig() {
        return new DefaultScanConfig(config);
    }
    
    @Override
    public PluginLogger getLogger() {
        return new Slf4jPluginLogger(LoggerFactory.getLogger("PluginLogger"));
    }
    
    private static class DefaultHttpRequest implements HttpRequest {
        private final URI uri;
        private final Map<String, String> queryParams;
        
        DefaultHttpRequest(URI uri) {
            this.uri = uri;
            this.queryParams = parseQueryParams(uri);
        }
        
        @Override
        public String getMethod() {
            return "GET";
        }
        
        @Override
        public URI getUri() {
            return uri;
        }
        
        @Override
        public Map<String, List<String>> getHeaders() {
            return Collections.emptyMap();
        }
        
        @Override
        public Optional<String> getHeader(String name) {
            return Optional.empty();
        }
        
        @Override
        public Optional<String> getBody() {
            return Optional.empty();
        }
        
        @Override
        public Map<String, String> getQueryParameters() {
            return queryParams;
        }
        
        @Override
        public Optional<String> getContentType() {
            return Optional.empty();
        }
        
        @Override
        public boolean isSecure() {
            return "https".equalsIgnoreCase(uri.getScheme());
        }
        
        private Map<String, String> parseQueryParams(URI uri) {
            Map<String, String> params = new HashMap<>();
            String query = uri.getQuery();
            if (query != null && !query.isEmpty()) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2) {
                        params.put(pair[0], pair[1]);
                    }
                }
            }
            return params;
        }
    }
    
    private static class DefaultHttpResponse implements HttpResponse {
        private final HttpResponseData data;
        
        DefaultHttpResponse(HttpResponseData data) {
            this.data = data;
        }
        
        @Override
        public int getStatusCode() {
            return data.getStatusCode();
        }
        
        @Override
        public String getReasonPhrase() {
            return data.getReasonPhrase();
        }
        
        @Override
        public Map<String, List<String>> getHeaders() {
            return data.getHeaders();
        }
        
        @Override
        public Optional<String> getHeader(String name) {
            return data.getHeader(name);
        }
        
        @Override
        public String getBody() {
            return data.getBody();
        }
        
        @Override
        public String getBodyTruncated(int maxLength) {
            return data.getBodyTruncated(maxLength);
        }
        
        @Override
        public Optional<String> getContentType() {
            return data.getContentType();
        }
        
        @Override
        public long getResponseTimeMs() {
            return data.getResponseTimeMs();
        }
        
        @Override
        public long getContentLength() {
            return data.getContentLength();
        }
        
        @Override
        public boolean isSuccessful() {
            return data.isSuccessful();
        }
        
        @Override
        public boolean isRedirect() {
            return data.isRedirect();
        }
    }
    
    private static class DefaultScanConfig implements ScanConfig {
        private final ScanConfiguration config;
        
        DefaultScanConfig(ScanConfiguration config) {
            this.config = config;
        }
        
        @Override
        public SafetyLevel getMaxSafetyLevel() {
            return config.getMaxSafetyLevel();
        }
        
        @Override
        public boolean isExpertModeEnabled() {
            return config.isExpertMode();
        }
        
        @Override
        public int getMaxCrawlDepth() {
            return config.getMaxCrawlDepth();
        }
        
        @Override
        public double getRateLimit() {
            return config.getRateLimit();
        }
        
        @Override
        public boolean hasValidConsent() {
            return true; // Already validated by scan engine
        }
        
        @Override
        public String getScanId() {
            return config.getScanId();
        }
        
        @Override
        public boolean isDryRun() {
            return false;
        }
    }
    
    private static class Slf4jPluginLogger implements PluginLogger {
        private final Logger logger;
        
        Slf4jPluginLogger(Logger logger) {
            this.logger = logger;
        }
        
        @Override
        public void debug(String message) {
            logger.debug(message);
        }
        
        @Override
        public void debug(String message, Object... args) {
            logger.debug(message, args);
        }
        
        @Override
        public void info(String message) {
            logger.info(message);
        }
        
        @Override
        public void info(String message, Object... args) {
            logger.info(message, args);
        }
        
        @Override
        public void warn(String message) {
            logger.warn(message);
        }
        
        @Override
        public void warn(String message, Object... args) {
            logger.warn(message, args);
        }
        
        @Override
        public void error(String message) {
            logger.error(message);
        }
        
        @Override
        public void error(String message, Throwable throwable) {
            logger.error(message, throwable);
        }
        
        @Override
        public void error(String message, Object... args) {
            logger.error(message, args);
        }
    }
}
