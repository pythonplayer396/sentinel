package com.sentinel.cli.commands;

import com.sentinel.core.consent.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.concurrent.Callable;

/**
 * CLI command to manage consent documents.
 */
@Command(
        name = "consent",
        description = "Manage scan consent documents",
        subcommands = {
                ConsentCommand.CreateCommand.class,
                ConsentCommand.ValidateCommand.class
        }
)
public class ConsentCommand implements Runnable {
    
    @Override
    public void run() {
        System.out.println("Use 'consent create' or 'consent validate'");
    }
    
    @Command(name = "create", description = "Create a new consent document")
    static class CreateCommand implements Callable<Integer> {
        
        @Option(names = {"-t", "--target"}, required = true, description = "Target URL")
        private String targetUrl;
        
        @Option(names = {"-o", "--org"}, required = true, description = "Organization name")
        private String organization;
        
        @Option(names = {"-a", "--authorized-by"}, required = true, description = "Authorized by (name)")
        private String authorizedBy;
        
        @Option(names = {"-e", "--email"}, description = "Email address")
        private String email;
        
        @Option(names = {"-d", "--days"}, defaultValue = "30", description = "Valid for days")
        private int validDays;
        
        @Option(names = {"-s", "--scope"}, defaultValue = "standard", 
                description = "Scope: safe, standard, expert")
        private String scope;
        
        @Option(names = {"-f", "--file"}, required = true, description = "Output file path")
        private String outputFile;
        
        @Override
        public Integer call() throws Exception {
            System.out.println("Creating consent document...");
            
            URI target = new URI(targetUrl);
            Instant now = Instant.now();
            Instant validUntil = now.plus(validDays, ChronoUnit.DAYS);
            
            ConsentScope consentScope;
            switch (scope.toLowerCase()) {
                case "safe":
                    consentScope = ConsentScope.defaultSafe();
                    break;
                case "expert":
                    consentScope = ConsentScope.expert();
                    break;
                default:
                    consentScope = ConsentScope.standard();
            }
            
            ConsentDocument consent = ConsentDocument.builder()
                    .authorizedTargets(Collections.singletonList(target))
                    .organizationName(organization)
                    .authorizedBy(authorizedBy)
                    .authorizedByEmail(email)
                    .validFrom(now)
                    .validUntil(validUntil)
                    .scope(consentScope)
                    .notes("Created via Sentinel CLI")
                    .build();
            
            // Save to file
            ObjectMapper mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .enable(SerializationFeature.INDENT_OUTPUT);
            
            mapper.writeValue(new File(outputFile), consent);
            
            System.out.println("✓ Consent document created: " + outputFile);
            System.out.println("  Consent ID: " + consent.getConsentId());
            System.out.println("  Target: " + target);
            System.out.println("  Valid until: " + validUntil);
            System.out.println("  Scope: " + scope);
            
            return 0;
        }
    }
    
    @Command(name = "validate", description = "Validate a consent document")
    static class ValidateCommand implements Callable<Integer> {
        
        @Option(names = {"-f", "--file"}, required = true, description = "Consent file path")
        private String consentFile;
        
        @Override
        public Integer call() throws Exception {
            System.out.println("Validating consent document: " + consentFile);
            
            ObjectMapper mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule());
            
            ConsentDocument consent = mapper.readValue(new File(consentFile), ConsentDocument.class);
            
            System.out.println("  Consent ID: " + consent.getConsentId());
            System.out.println("  Organization: " + consent.getOrganizationName());
            System.out.println("  Authorized by: " + consent.getAuthorizedBy());
            System.out.println("  Valid from: " + consent.getValidFrom());
            System.out.println("  Valid until: " + consent.getValidUntil());
            System.out.println("  Targets: " + consent.getAuthorizedTargets().size());
            
            if (consent.isValid()) {
                System.out.println("✓ Consent is VALID");
                return 0;
            } else {
                System.out.println("✗ Consent is EXPIRED or not yet valid");
                return 1;
            }
        }
    }
}
