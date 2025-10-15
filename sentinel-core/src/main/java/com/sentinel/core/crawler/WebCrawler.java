package com.sentinel.core.crawler;

import com.sentinel.core.http.HttpResponseData;
import com.sentinel.core.http.SentinelHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Web crawler with robots.txt respect, depth control, and scope management.
 */
public class WebCrawler {
    
    private static final Logger logger = LoggerFactory.getLogger(WebCrawler.class);
    
    private final SentinelHttpClient httpClient;
    private final CrawlConfig config;
    private final Set<URI> visited;
    private final Queue<CrawlTask> queue;
    private final RobotsParser robotsParser;
    private final ExecutorService executor;
    
    public WebCrawler(SentinelHttpClient httpClient, CrawlConfig config) {
        this.httpClient = httpClient;
        this.config = config;
        this.visited = ConcurrentHashMap.newKeySet();
        this.queue = new ConcurrentLinkedQueue<>();
        this.robotsParser = new RobotsParser(httpClient);
        this.executor = Executors.newFixedThreadPool(config.getConcurrency());
    }
    
    /**
     * Start crawling from a seed URL.
     */
    public CrawlResult crawl(URI seedUrl) {
        logger.info("Starting crawl from: {}", seedUrl);
        
        CrawlResult result = new CrawlResult(seedUrl);
        
        // Check robots.txt if configured
        if (config.isRespectRobotsTxt()) {
            try {
                robotsParser.parse(seedUrl);
            } catch (IOException e) {
                logger.warn("Failed to parse robots.txt for {}: {}", seedUrl, e.getMessage());
            }
        }
        
        // Add seed to queue
        queue.offer(new CrawlTask(seedUrl, 0));
        
        // Process queue
        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < config.getConcurrency(); i++) {
            futures.add(executor.submit(() -> {
                processQueue(result);
                return null;
            }));
        }
        
        // Wait for completion
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Crawl task failed", e);
            }
        }
        
        logger.info("Crawl completed. Visited {} URLs", visited.size());
        return result;
    }
    
    private void processQueue(CrawlResult result) {
        while (!queue.isEmpty()) {
            CrawlTask task = queue.poll();
            if (task == null) {
                break;
            }
            
            // Check if already visited
            if (!visited.add(task.getUrl())) {
                continue;
            }
            
            // Check depth limit
            if (task.getDepth() > config.getMaxDepth()) {
                continue;
            }
            
            // Check scope
            if (!isInScope(task.getUrl())) {
                logger.debug("URL out of scope: {}", task.getUrl());
                continue;
            }
            
            // Check robots.txt
            if (config.isRespectRobotsTxt() && !robotsParser.isAllowed(task.getUrl())) {
                logger.debug("URL disallowed by robots.txt: {}", task.getUrl());
                continue;
            }
            
            // Fetch the page
            try {
                HttpResponseData response = httpClient.get(task.getUrl());
                CrawledPage page = new CrawledPage(task.getUrl(), response, task.getDepth());
                result.addPage(page);
                
                // Extract links if HTML
                if (isHtmlContent(response)) {
                    extractLinks(page, task.getDepth() + 1);
                }
                
            } catch (IOException e) {
                logger.warn("Failed to fetch {}: {}", task.getUrl(), e.getMessage());
                result.addError(task.getUrl(), e.getMessage());
            }
        }
    }
    
    private void extractLinks(CrawledPage page, int nextDepth) {
        try {
            Document doc = Jsoup.parse(page.getResponse().getBody(), page.getUrl().toString());
            
            // Extract links from <a> tags
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String href = link.attr("abs:href");
                if (!href.isEmpty()) {
                    try {
                        URI uri = new URI(href).normalize();
                        if (isInScope(uri)) {
                            queue.offer(new CrawlTask(uri, nextDepth));
                        }
                    } catch (URISyntaxException e) {
                        logger.debug("Invalid URI: {}", href);
                    }
                }
            }
            
            // Extract forms
            Elements forms = doc.select("form");
            for (Element form : forms) {
                String action = form.attr("abs:action");
                if (!action.isEmpty()) {
                    try {
                        URI uri = new URI(action).normalize();
                        page.addForm(new FormData(uri, form.attr("method"), extractFormFields(form)));
                    } catch (URISyntaxException e) {
                        logger.debug("Invalid form action: {}", action);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.warn("Failed to parse page {}: {}", page.getUrl(), e.getMessage());
        }
    }
    
    private Map<String, String> extractFormFields(Element form) {
        Map<String, String> fields = new HashMap<>();
        Elements inputs = form.select("input, select, textarea");
        for (Element input : inputs) {
            String name = input.attr("name");
            String type = input.attr("type");
            if (!name.isEmpty()) {
                fields.put(name, type);
            }
        }
        return fields;
    }
    
    private boolean isInScope(URI uri) {
        // Same origin check
        URI seed = config.getSeedUrl();
        return uri.getHost() != null 
                && uri.getHost().equals(seed.getHost())
                && uri.getScheme().equals(seed.getScheme());
    }
    
    private boolean isHtmlContent(HttpResponseData response) {
        return response.getContentType()
                .map(ct -> ct.toLowerCase().contains("text/html"))
                .orElse(false);
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private static class CrawlTask {
        private final URI url;
        private final int depth;
        
        CrawlTask(URI url, int depth) {
            this.url = url;
            this.depth = depth;
        }
        
        URI getUrl() {
            return url;
        }
        
        int getDepth() {
            return depth;
        }
    }
}
