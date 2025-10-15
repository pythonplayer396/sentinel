package com.sentinel.cli;

import com.sentinel.cli.commands.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Main CLI entry point for Sentinel scanner.
 */
@Command(
        name = "sentinel",
        description = "Sentinel - Protection-First Web Vulnerability Scanner",
        version = "Sentinel 1.0.0",
        mixinStandardHelpOptions = true,
        subcommands = {
                ScanCommand.class,
                ConsentCommand.class,
                PluginCommand.class,
                ReportCommand.class
        }
)
public class SentinelCLI implements Runnable {
    
    @Override
    public void run() {
        System.out.println("Sentinel - Protection-First Web Vulnerability Scanner");
        System.out.println("Use --help to see available commands");
        System.out.println();
        System.out.println("Quick start:");
        System.out.println("  sentinel scan --target https://example.com");
        System.out.println("  sentinel consent create --target https://example.com");
        System.out.println("  sentinel plugin list");
    }
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new SentinelCLI()).execute(args);
        System.exit(exitCode);
    }
}
