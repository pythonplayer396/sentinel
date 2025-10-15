package com.sentinel.core.http;

import java.util.*;

/**
 * Immutable HTTP response data.
 */
public class HttpResponseData {
    
    private final int statusCode;
    private final String reasonPhrase;
    private final Map<String, List<String>> headers;
    private final String body;
    private final long responseTimeMs;
    
    public HttpResponseData(int statusCode, String reasonPhrase, 
                           Map<String, List<String>> headers, String body, long responseTimeMs) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        this.body = body;
        this.responseTimeMs = responseTimeMs;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getReasonPhrase() {
        return reasonPhrase;
    }
    
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
    
    public Optional<String> getHeader(String name) {
        List<String> values = headers.get(name);
        return values != null && !values.isEmpty() 
                ? Optional.of(values.get(0)) 
                : Optional.empty();
    }
    
    public String getBody() {
        return body;
    }
    
    public String getBodyTruncated(int maxLength) {
        if (body.length() <= maxLength) {
            return body;
        }
        return body.substring(0, maxLength) + "... [truncated]";
    }
    
    public long getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public Optional<String> getContentType() {
        return getHeader("Content-Type");
    }
    
    public long getContentLength() {
        return body.length();
    }
    
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
    
    public boolean isRedirect() {
        return statusCode >= 300 && statusCode < 400;
    }
}
