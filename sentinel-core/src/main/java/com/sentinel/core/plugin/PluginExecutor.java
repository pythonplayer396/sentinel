package com.sentinel.core.plugin;

import com.sentinel.plugin.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Executes plugins with sandboxing, timeouts, and safety checks.
 */
public class PluginExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(PluginExecutor.class);
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;
    
    private final ExecutorService executorService;
    private final long timeoutSeconds;
    
    public PluginExecutor(int threadPoolSize, long timeoutSeconds) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.timeoutSeconds = timeoutSeconds;
    }
    
    public PluginExecutor(int threadPoolSize) {
        this(threadPoolSize, DEFAULT_TIMEOUT_SECONDS);
    }
    
    /**
     * Execute a plugin with timeout and error handling.
     */
    public PluginExecutionResult execute(ScannerPlugin plugin, PluginContext context) {
        PluginMetadata metadata = plugin.getMetadata();
        logger.debug("Executing plugin: {}", metadata.getId());
        
        // Check safety level against scan config
        if (!isSafetyLevelAllowed(plugin, context)) {
            logger.warn("Plugin {} safety level {} not allowed by scan config",
                    metadata.getId(), metadata.getSafetyLevel());
            return PluginExecutionResult.skipped(metadata.getId(), "Safety level not allowed");
        }
        
        Future<List<PluginFinding>> future = executorService.submit(() -> {
            try {
                return plugin.run(context);
            } catch (Exception e) {
                logger.error("Plugin {} threw exception", metadata.getId(), e);
                throw e;
            }
        });
        
        try {
            List<PluginFinding> findings = future.get(timeoutSeconds, TimeUnit.SECONDS);
            logger.debug("Plugin {} completed with {} findings", metadata.getId(), findings.size());
            return PluginExecutionResult.success(metadata.getId(), findings);
            
        } catch (TimeoutException e) {
            future.cancel(true);
            logger.error("Plugin {} timed out after {} seconds", metadata.getId(), timeoutSeconds);
            return PluginExecutionResult.timeout(metadata.getId());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Plugin {} execution interrupted", metadata.getId());
            return PluginExecutionResult.error(metadata.getId(), "Interrupted");
            
        } catch (ExecutionException e) {
            logger.error("Plugin {} execution failed", metadata.getId(), e.getCause());
            return PluginExecutionResult.error(metadata.getId(), e.getCause().getMessage());
        }
    }
    
    /**
     * Execute multiple plugins in parallel.
     */
    public List<PluginExecutionResult> executeAll(List<ScannerPlugin> plugins, PluginContext context) {
        List<CompletableFuture<PluginExecutionResult>> futures = new ArrayList<>();
        
        for (ScannerPlugin plugin : plugins) {
            CompletableFuture<PluginExecutionResult> future = CompletableFuture.supplyAsync(
                    () -> execute(plugin, context),
                    executorService
            );
            futures.add(future);
        }
        
        // Wait for all to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        
        try {
            allOf.get();
        } catch (Exception e) {
            logger.error("Error waiting for plugin execution", e);
        }
        
        // Collect results
        List<PluginExecutionResult> results = new ArrayList<>();
        for (CompletableFuture<PluginExecutionResult> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                logger.error("Failed to get plugin result", e);
            }
        }
        
        return results;
    }
    
    private boolean isSafetyLevelAllowed(ScannerPlugin plugin, PluginContext context) {
        SafetyLevel pluginLevel = plugin.getMetadata().getSafetyLevel();
        SafetyLevel maxLevel = context.getScanConfig().getMaxSafetyLevel();
        
        return !pluginLevel.requiresMorePermissionsThan(maxLevel);
    }
    
    public void shutdown() {
        logger.info("Shutting down plugin executor");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
