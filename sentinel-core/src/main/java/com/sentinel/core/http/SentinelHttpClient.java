package com.sentinel.core.http;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client wrapper for safe, rate-limited requests.
 */
public class SentinelHttpClient implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(SentinelHttpClient.class);
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final String USER_AGENT = "Sentinel-Scanner/1.0 (Protection-First Security Scanner)";
    
    private final CloseableHttpClient httpClient;
    private final CookieStore cookieStore;
    private final RateLimiter rateLimiter;
    private final Map<String, String> defaultHeaders;
    
    public SentinelHttpClient(double requestsPerSecond) {
        this.cookieStore = new BasicCookieStore();
        this.rateLimiter = new RateLimiter(requestsPerSecond);
        this.defaultHeaders = new HashMap<>();
        this.defaultHeaders.put("User-Agent", USER_AGENT);
        
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .setResponseTimeout(Timeout.of(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .build();
        
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(USER_AGENT)
                .build();
        
        logger.info("Initialized HTTP client with rate limit: {} req/s", requestsPerSecond);
    }
    
    /**
     * Execute a GET request with rate limiting.
     */
    public HttpResponseData get(URI uri) throws IOException {
        return execute(new HttpGet(uri));
    }
    
    /**
     * Execute an HTTP request with rate limiting and safety checks.
     */
    public HttpResponseData execute(HttpUriRequestBase request) throws IOException {
        // Apply rate limiting
        rateLimiter.acquire();
        
        // Add default headers
        for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
            if (!request.containsHeader(header.getKey())) {
                request.addHeader(header.getKey(), header.getValue());
            }
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            return httpClient.execute(request, response -> {
                long responseTime = System.currentTimeMillis() - startTime;
                return convertResponse(response, responseTime);
            });
        } catch (IOException e) {
            try {
                logger.error("HTTP request failed for {}: {}", request.getUri(), e.getMessage());
            } catch (URISyntaxException ex) {
                logger.error("HTTP request failed: {}", e.getMessage());
            }
            throw e;
        }
    }
    
    /**
     * Set a custom header for all requests.
     */
    public void setDefaultHeader(String name, String value) {
        defaultHeaders.put(name, value);
    }
    
    /**
     * Get the cookie store for session management.
     */
    public CookieStore getCookieStore() {
        return cookieStore;
    }
    
    /**
     * Clear all cookies.
     */
    public void clearCookies() {
        cookieStore.clear();
    }
    
    private HttpResponseData convertResponse(ClassicHttpResponse response, long responseTime) throws IOException {
        int statusCode = response.getCode();
        String reasonPhrase = response.getReasonPhrase();
        
        // Extract headers
        Map<String, List<String>> headers = new HashMap<>();
        for (Header header : response.getHeaders()) {
            headers.computeIfAbsent(header.getName(), k -> new ArrayList<>())
                    .add(header.getValue());
        }
        
        // Extract body
        String body = "";
        if (response.getEntity() != null) {
            try {
                body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            } catch (ParseException e) {
                logger.warn("Failed to parse response body: {}", e.getMessage());
                body = "";
            }
        }
        
        return new HttpResponseData(statusCode, reasonPhrase, headers, body, responseTime);
    }
    
    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
