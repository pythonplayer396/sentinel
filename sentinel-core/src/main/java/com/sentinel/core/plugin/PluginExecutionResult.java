package com.sentinel.core.plugin;

import com.sentinel.plugin.api.PluginFinding;
import java.util.ArrayList;
import java.util.List;

public class PluginExecutionResult {
    
    private final String pluginId;
    private final ExecutionStatus status;
    private final List<PluginFinding> findings;
    private final String errorMessage;
    
    private PluginExecutionResult(String pluginId, ExecutionStatus status, 
                                  List<PluginFinding> findings, String errorMessage) {
        this.pluginId = pluginId;
        this.status = status;
        this.findings = findings != null ? findings : new ArrayList<>();
        this.errorMessage = errorMessage;
    }
    
    public static PluginExecutionResult success(String pluginId, List<PluginFinding> findings) {
        return new PluginExecutionResult(pluginId, ExecutionStatus.SUCCESS, findings, null);
    }
    
    public static PluginExecutionResult error(String pluginId, String errorMessage) {
        return new PluginExecutionResult(pluginId, ExecutionStatus.ERROR, null, errorMessage);
    }
    
    public static PluginExecutionResult timeout(String pluginId) {
        return new PluginExecutionResult(pluginId, ExecutionStatus.TIMEOUT, null, "Execution timeout");
    }
    
    public static PluginExecutionResult skipped(String pluginId, String reason) {
        return new PluginExecutionResult(pluginId, ExecutionStatus.SKIPPED, null, reason);
    }
    
    public String getPluginId() { return pluginId; }
    public ExecutionStatus getStatus() { return status; }
    public List<PluginFinding> getFindings() { return findings; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isSuccess() { return status == ExecutionStatus.SUCCESS; }
    
    public enum ExecutionStatus {
        SUCCESS, ERROR, TIMEOUT, SKIPPED
    }
}
