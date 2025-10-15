package com.sentinel.core.crawler;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CrawlResult {
    private final URI seedUrl;
    private final Map<URI, CrawledPage> pages;
    private final Map<URI, String> errors;
    private final long startTime;
    
    public CrawlResult(URI seedUrl) {
        this.seedUrl = seedUrl;
        this.pages = new ConcurrentHashMap<>();
        this.errors = new ConcurrentHashMap<>();
        this.startTime = System.currentTimeMillis();
    }
    
    public void addPage(CrawledPage page) {
        pages.put(page.getUrl(), page);
    }
    
    public void addError(URI url, String error) {
        errors.put(url, error);
    }
    
    public Collection<CrawledPage> getPages() {
        return pages.values();
    }
    
    public Map<URI, String> getErrors() {
        return new HashMap<>(errors);
    }
    
    public long getDurationMs() {
        return System.currentTimeMillis() - startTime;
    }
    
    public int getPageCount() {
        return pages.size();
    }
}
