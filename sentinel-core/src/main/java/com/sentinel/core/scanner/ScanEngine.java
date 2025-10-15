package com.sentinel.core.scanner;

import com.sentinel.core.consent.ConsentManager;
import com.sentinel.core.crawler.*;
import com.sentinel.core.http.SentinelHttpClient;
import com.sentinel.core.plugin.*;
import com.sentinel.plugin.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

/**
 * Main scanning engine that orchestrates crawling and plugin execution.
 */
public class ScanEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(ScanEngine.class);
    
    private final ConsentManager consentManager;
    private final PluginManager pluginManager;
    private final PluginExecutor pluginExecutor;
    
    public ScanEngine(ConsentManager consentManager, PluginManager pluginManager) {
        this.consentManager = consentManager;
        this.pluginManager = pluginManager;
        this.pluginExecutor = new PluginExecutor(4);
    }
    
    /**
     * Execute a scan with the given configuration.
     */
    public ScanResult scan(ScanConfiguration config) throws ScanException {
        logger.info("Starting scan: {}", config.getScanId());
        
        // Validate consent
        if (!consentManager.hasValidConsent(config.getTargetUrl())) {
            throw new ScanException("No valid consent for target: " + config.getTargetUrl());
        }
        
        ScanResult result = new ScanResult(config);
        
        try {
            // Initialize HTTP client with rate limiting
            try (SentinelHttpClient httpClient = new SentinelHttpClient(config.getRateLimit())) {
                
                // Crawl the target
                WebCrawler crawler = new WebCrawler(
                        httpClient,
                        new CrawlConfig(
                                config.getTargetUrl(),
                                config.getMaxCrawlDepth(),
                                config.getConcurrency(),
                                config.isRespectRobotsTxt()
                        )
                );
                
                CrawlResult crawlResult = crawler.crawl(config.getTargetUrl());
                result.setCrawlResult(crawlResult);
                logger.info("Crawl completed: {} pages", crawlResult.getPageCount());
                
                // Get applicable plugins
                List<ScannerPlugin> plugins = pluginManager.getPluginsBySafetyLevel(
                        config.getMaxSafetyLevel()
                );
                logger.info("Running {} plugins", plugins.size());
                
                // Execute plugins on each crawled page
                for (CrawledPage page : crawlResult.getPages()) {
                    PluginContext context = createPluginContext(page, config);
                    
                    List<PluginExecutionResult> pluginResults = pluginExecutor.executeAll(plugins, context);
                    
                    // Collect findings
                    for (PluginExecutionResult pluginResult : pluginResults) {
                        if (pluginResult.isSuccess()) {
                            result.addFindings(pluginResult.getFindings());
                        }
                    }
                }
                
                crawler.shutdown();
            }
            
            result.complete();
            logger.info("Scan completed: {} findings", result.getFindingCount());
            
        } catch (Exception e) {
            logger.error("Scan failed", e);
            result.setError(e.getMessage());
            throw new ScanException("Scan failed: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    private PluginContext createPluginContext(CrawledPage page, ScanConfiguration config) {
        return new DefaultPluginContext(page, config);
    }
    
    public void shutdown() {
        pluginExecutor.shutdown();
    }
}
