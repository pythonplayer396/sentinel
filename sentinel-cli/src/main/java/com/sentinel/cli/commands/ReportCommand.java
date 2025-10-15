package com.sentinel.cli.commands;

import picocli.CommandLine.Command;

/**
 * CLI command to generate reports.
 */
@Command(
        name = "report",
        description = "Generate reports from scan results"
)
public class ReportCommand implements Runnable {
    
    @Override
    public void run() {
        System.out.println("Report generation - Coming soon");
        System.out.println("Supported formats: JSON, HTML, PDF");
    }
}
