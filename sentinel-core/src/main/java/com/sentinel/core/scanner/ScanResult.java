package com.sentinel.core.scanner;

import com.sentinel.core.crawler.CrawlResult;
import com.sentinel.plugin.api.PluginFinding;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ScanResult {
    private final ScanConfiguration config;
    private final Instant startTime;
    private Instant endTime;
    private CrawlResult crawlResult;
    private final List<PluginFinding> findings;
    private String error;
    
    public ScanResult(ScanConfiguration config) {
        this.config = config;
        this.startTime = Instant.now();
        this.findings = new ArrayList<>();
    }
    
    public void setCrawlResult(CrawlResult crawlResult) {
        this.crawlResult = crawlResult;
    }
    
    public void addFinding(PluginFinding finding) {
        findings.add(finding);
    }
    
    public void addFindings(List<PluginFinding> findings) {
        this.findings.addAll(findings);
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public void complete() {
        this.endTime = Instant.now();
    }
    
    public ScanConfiguration getConfig() { return config; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public CrawlResult getCrawlResult() { return crawlResult; }
    public List<PluginFinding> getFindings() { return new ArrayList<>(findings); }
    public int getFindingCount() { return findings.size(); }
    public String getError() { return error; }
    public boolean hasError() { return error != null; }
    
    public long getDurationMs() {
        if (endTime == null) {
            return System.currentTimeMillis() - startTime.toEpochMilli();
        }
        return endTime.toEpochMilli() - startTime.toEpochMilli();
    }
}
