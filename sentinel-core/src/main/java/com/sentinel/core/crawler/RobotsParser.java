package com.sentinel.core.crawler;

import com.sentinel.core.http.HttpResponseData;
import com.sentinel.core.http.SentinelHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.*;

public class RobotsParser {
    private static final Logger logger = LoggerFactory.getLogger(RobotsParser.class);
    private final SentinelHttpClient httpClient;
    private final Map<String, List<String>> disallowedPaths;
    
    public RobotsParser(SentinelHttpClient httpClient) {
        this.httpClient = httpClient;
        this.disallowedPaths = new HashMap<>();
    }
    
    public void parse(URI baseUri) throws IOException {
        URI robotsUri = baseUri.resolve("/robots.txt");
        try {
            HttpResponseData response = httpClient.get(robotsUri);
            if (response.isSuccessful()) {
                parseRobotsTxt(baseUri.getHost(), response.getBody());
            }
        } catch (IOException e) {
            logger.debug("No robots.txt found for {}", baseUri.getHost());
        }
    }
    
    private void parseRobotsTxt(String host, String content) {
        List<String> disallowed = new ArrayList<>();
        boolean relevantSection = false;
        
        for (String line : content.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            
            if (line.toLowerCase().startsWith("user-agent:")) {
                String agent = line.substring(11).trim();
                relevantSection = agent.equals("*") || agent.toLowerCase().contains("sentinel");
            } else if (relevantSection && line.toLowerCase().startsWith("disallow:")) {
                String path = line.substring(9).trim();
                if (!path.isEmpty()) {
                    disallowed.add(path);
                }
            }
        }
        
        disallowedPaths.put(host, disallowed);
        logger.info("Parsed robots.txt for {}: {} disallowed paths", host, disallowed.size());
    }
    
    public boolean isAllowed(URI uri) {
        List<String> disallowed = disallowedPaths.get(uri.getHost());
        if (disallowed == null || disallowed.isEmpty()) {
            return true;
        }
        
        String path = uri.getPath();
        for (String disallowedPath : disallowed) {
            if (path.startsWith(disallowedPath)) {
                return false;
            }
        }
        return true;
    }
}
