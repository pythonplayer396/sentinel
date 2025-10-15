package com.sentinel.core.crawler;

import java.net.URI;

public class CrawlConfig {
    private final URI seedUrl;
    private final int maxDepth;
    private final int concurrency;
    private final boolean respectRobotsTxt;
    
    public CrawlConfig(URI seedUrl, int maxDepth, int concurrency, boolean respectRobotsTxt) {
        this.seedUrl = seedUrl;
        this.maxDepth = maxDepth;
        this.concurrency = concurrency;
        this.respectRobotsTxt = respectRobotsTxt;
    }
    
    public URI getSeedUrl() { return seedUrl; }
    public int getMaxDepth() { return maxDepth; }
    public int getConcurrency() { return concurrency; }
    public boolean isRespectRobotsTxt() { return respectRobotsTxt; }
}
