package com.sentinel.core.crawler;

import java.net.URI;
import java.util.Map;

public class FormData {
    private final URI action;
    private final String method;
    private final Map<String, String> fields;
    
    public FormData(URI action, String method, Map<String, String> fields) {
        this.action = action;
        this.method = method.isEmpty() ? "GET" : method.toUpperCase();
        this.fields = fields;
    }
    
    public URI getAction() { return action; }
    public String getMethod() { return method; }
    public Map<String, String> getFields() { return fields; }
}
