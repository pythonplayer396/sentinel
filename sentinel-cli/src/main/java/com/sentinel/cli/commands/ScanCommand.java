package com.sentinel.cli.commands;

import com.sentinel.core.consent.ConsentManager;
import com.sentinel.core.plugin.PluginManager;
import com.sentinel.core.scanner.*;
import com.sentinel.plugin.api.SafetyLevel;
import com.sentinel.plugins.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.Callable;



@Command(
        name = "scan",
        description = "Execute a security scan on a target URL"
)
public class ScanCommand implements Callable<Integer> {
    
    @Option(names = {"-t", "--target"}, required = true, description = "Target URL to scan")
    private String targetUrl;
    
    @Option(names = {"-d", "--depth"}, defaultValue = "10", description = "Maximum crawl depth")
    private int maxDepth;
    
    @Option(names = {"-r", "--rate"}, defaultValue = "5.0", description = "Request rate limit (req/s)")
    private double rateLimit;
    
    @Option(names = {"-s", "--safety"}, defaultValue = "PASSIVE", 
            description = "Maximum safety level: PASSIVE, ACTIVE, EXPERT")
    private SafetyLevel safetyLevel;
    
    @Option(names = {"-c", "--consent"}, description = "Path to consent document")
    private String consentPath;
    
    @Option(names = {"-o", "--output"}, description = "Output file for report (JSON)")
    private String outputPath;
    
    @Option(names = {"--no-robots"}, description = "Ignore robots.txt")
    private boolean ignoreRobots;
    
    @Option(names = {"--expert-mode"}, description = "Enable expert mode")
    private boolean expertMode;
    
    @Override
    public Integer call() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  SENTINEL - Protection-First Web Vulnerability Scanner    ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Validate target URL
        URI target = new URI(targetUrl);
        System.out.println("Target: " + target);
        System.out.println("Safety Level: " + safetyLevel);
        System.out.println("Max Depth: " + maxDepth);
        System.out.println("Rate Limit: " + rateLimit + " req/s");
        System.out.println();
        
        // Initialize consent manager
        ConsentManager consentManager = new ConsentManager(
                Paths.get(System.getProperty("user.home"), ".sentinel", "consent")
        );
        
        // Load consent if provided
        if (consentPath != null) {
            System.out.println("Loading consent from: " + consentPath);
            consentManager.loadConsentFromFile(Paths.get(consentPath));
        }
        
        // Check consent
        if (!consentManager.hasValidConsent(target)) {
            System.err.println("ERROR: No valid consent for target: " + target);
            System.err.println("Please create a consent document using: sentinel consent create");
            return 1;
        }
        
        System.out.println("✓ Valid consent found");
        System.out.println();
        
        // Initialize plugin manager and register built-in plugins
        PluginManager pluginManager = new PluginManager();
        registerBuiltInPlugins(pluginManager);
        
        System.out.println("Loaded " + pluginManager.getAllPlugins().size() + " plugins");
        System.out.println();
        
        // Create scan configuration
        ScanConfiguration config = ScanConfiguration.builder()
                .targetUrl(target)
                .maxSafetyLevel(safetyLevel)
                .maxCrawlDepth(maxDepth)
                .rateLimit(rateLimit)
                .respectRobotsTxt(!ignoreRobots)
                .expertMode(expertMode)
                .build();
        
        // Execute scan
        System.out.println("Starting scan...");
        System.out.println("════════════════════════════════════════════════════════════");
        
        ScanEngine engine = new ScanEngine(consentManager, pluginManager);
        
        try {
            ScanResult result = engine.scan(config);
            
            System.out.println();
            System.out.println("════════════════════════════════════════════════════════════");
            System.out.println("Scan completed!");
            System.out.println();
            System.out.println("Duration: " + result.getDurationMs() + " ms");
            System.out.println("Pages crawled: " + result.getCrawlResult().getPageCount());
            System.out.println("Findings: " + result.getFindingCount());
            System.out.println();
            
            // Print summary
            printFindingsSummary(result);
            
            // Save report if output specified
            if (outputPath != null) {
                System.out.println("Saving report to: " + outputPath);
                // TODO: Implement report saving
            }
            
            engine.shutdown();
            pluginManager.shutdown();
            
            return 0;
            
        } catch (ScanException e) {
            System.err.println("Scan failed: " + e.getMessage());
            engine.shutdown();
            pluginManager.shutdown();
            return 1;
        }
    }
    
    private void registerBuiltInPlugins(PluginManager pluginManager) {
        pluginManager.registerPlugin(new SecurityHeadersPlugin());
        pluginManager.registerPlugin(new InformationDisclosurePlugin());
        pluginManager.registerPlugin(new ReflectionDetectorPlugin());
        pluginManager.registerPlugin(new SubdomainFinderPlugin());
        pluginManager.registerPlugin(new SqlInjectionPlugin());
    }
    
    private void printFindingsSummary(ScanResult result) {
        System.out.println("Findings Summary:");
        System.out.println("─────────────────────────────────────────────────────────────");
        
        long critical = result.getFindings().stream()
                .filter(f -> f.getSeverity().name().equals("CRITICAL")).count();
        long high = result.getFindings().stream()
                .filter(f -> f.getSeverity().name().equals("HIGH")).count();
        long medium = result.getFindings().stream()
                .filter(f -> f.getSeverity().name().equals("MEDIUM")).count();
        long low = result.getFindings().stream()
                .filter(f -> f.getSeverity().name().equals("LOW")).count();
        long info = result.getFindings().stream()
                .filter(f -> f.getSeverity().name().equals("INFO")).count();
        
        System.out.println("  Critical: " + critical);
        System.out.println("  High:     " + high);
        System.out.println("  Medium:   " + medium);
        System.out.println("  Low:      " + low);
        System.out.println("  Info:     " + info);
        System.out.println();
        
        // Print top findings
        if (!result.getFindings().isEmpty()) {
            System.out.println("Top Findings:");
            result.getFindings().stream()
                    .limit(10)
                    .forEach(finding -> {
                        System.out.printf("  [%s] %s - %s%n",
                                finding.getSeverity(),
                                finding.getTitle(),
                                finding.getEndpoint().getPath()
                        );
                    });
        }
    }
}
