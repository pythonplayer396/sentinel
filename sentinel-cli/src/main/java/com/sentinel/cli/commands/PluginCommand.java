package com.sentinel.cli.commands;

import com.sentinel.core.plugin.PluginManager;
import com.sentinel.plugin.api.ScannerPlugin;
import com.sentinel.plugins.*;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

/**
 * CLI command to manage plugins.
 */
@Command(
        name = "plugin",
        description = "Manage scanner plugins",
        subcommands = {
                PluginCommand.ListCommand.class
        }
)
public class PluginCommand implements Runnable {
    
    @Override
    public void run() {
        System.out.println("Use 'plugin list' to see available plugins");
    }
    
    @Command(name = "list", description = "List all available plugins")
    static class ListCommand implements Callable<Integer> {
        
        @Override
        public Integer call() {
            System.out.println("Available Plugins:");
            System.out.println("═══════════════════════════════════════════════════════════");
            
            PluginManager pluginManager = new PluginManager();
            
            // Register built-in plugins
            pluginManager.registerPlugin(new SecurityHeadersPlugin());
            pluginManager.registerPlugin(new InformationDisclosurePlugin());
            pluginManager.registerPlugin(new ReflectionDetectorPlugin());
            pluginManager.registerPlugin(new SubdomainFinderPlugin());
            
            for (ScannerPlugin plugin : pluginManager.getAllPlugins()) {
                System.out.println();
                System.out.println("Plugin: " + plugin.getMetadata().getName());
                System.out.println("  ID: " + plugin.getMetadata().getId());
                System.out.println("  Version: " + plugin.getMetadata().getVersion());
                System.out.println("  Author: " + plugin.getMetadata().getAuthor());
                System.out.println("  Safety Level: " + plugin.getMetadata().getSafetyLevel());
                System.out.println("  Description: " + plugin.getMetadata().getDescription());
            }
            
            System.out.println();
            System.out.println("Total: " + pluginManager.getAllPlugins().size() + " plugins");
            
            pluginManager.shutdown();
            return 0;
        }
    }
}
