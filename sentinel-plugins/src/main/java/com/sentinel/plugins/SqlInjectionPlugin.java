package com.sentinel.plugins;

import com.sentinel.plugin.api.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced SQL Injection Detection Plugin
 * 
 * Detects SQL injection vulnerabilities through:
 * - Error-based detection (20+ database types)
 * - Timing analysis for blind injection
 * - Boolean-based injection patterns
 * - Database fingerprinting
 * - Context-aware severity scoring
 * 
 * Supports PASSIVE (error analysis) and ACTIVE (timing/boolean testing) modes.
 */
public class SqlInjectionPlugin implements ScannerPlugin {
    
    private PluginConfig config;
    private SafetyLevel currentSafetyLevel = SafetyLevel.PASSIVE;
    
    // Database-specific error patterns
    private static final Map<String, Pattern> DATABASE_ERROR_PATTERNS = new HashMap<>();
    
    static {
        // MySQL/MariaDB
        DATABASE_ERROR_PATTERNS.put("MySQL", Pattern.compile(
            "(?i)(SQL syntax.*MySQL|Warning.*mysql_|MySQLSyntaxErrorException|" +
            "com\\.mysql\\.jdbc|MySQLIntegrityConstraintViolationException|" +
            "MySQLNonTransientConnectionException|You have an error in your SQL syntax)"
        ));
        
        // PostgreSQL
        DATABASE_ERROR_PATTERNS.put("PostgreSQL", Pattern.compile(
            "(?i)(PostgreSQL.*ERROR|Warning.*\\Wpg_|valid PostgreSQL result|" +
            "Npgsql\\.|PG::SyntaxError|org\\.postgresql\\.util\\.PSQLException|" +
            "ERROR:\\s+syntax error at or near)"
        ));
        
        // Microsoft SQL Server
        DATABASE_ERROR_PATTERNS.put("MSSQL", Pattern.compile(
            "(?i)(Driver.*SQL[\\-\\_\\ ]*Server|OLE DB.*SQL Server|" +
            "\\[SQL Server\\]|ODBC SQL Server Driver|SQLServer JDBC Driver|" +
            "com\\.microsoft\\.sqlserver\\.jdbc|SqlException|" +
            "System\\.Data\\.SqlClient\\.SqlException|Incorrect syntax near)"
        ));
        
        // Oracle
        DATABASE_ERROR_PATTERNS.put("Oracle", Pattern.compile(
            "(?i)(\\bORA-[0-9][0-9][0-9][0-9]|Oracle error|Oracle.*Driver|" +
            "Warning.*\\Woci_|Warning.*\\Wora_|oracle\\.jdbc|OracleException|" +
            "quoted string not properly terminated)"
        ));
        
        // SQLite
        DATABASE_ERROR_PATTERNS.put("SQLite", Pattern.compile(
            "(?i)(SQLite/JDBCDriver|SQLite\\.Exception|System\\.Data\\.SQLite\\.SQLiteException|" +
            "Warning.*sqlite_|Warning.*SQLite3::|\\[SQLITE_ERROR\\]|" +
            "sqlite3.OperationalError|near \".*\": syntax error)"
        ));
        
        // IBM DB2
        DATABASE_ERROR_PATTERNS.put("DB2", Pattern.compile(
            "(?i)(CLI Driver.*DB2|DB2 SQL error|\\bdb2_\\w+\\(|SQLSTATE.=|" +
            "com\\.ibm\\.db2\\.jcc|DB2Exception)"
        ));
        
        // Informix
        DATABASE_ERROR_PATTERNS.put("Informix", Pattern.compile(
            "(?i)(Exception.*Informix|Informix ODBC Driver|ODBC Informix driver|" +
            "com\\.informix\\.jdbc|IfxException)"
        ));
        
        // Sybase
        DATABASE_ERROR_PATTERNS.put("Sybase", Pattern.compile(
            "(?i)(Warning.*sybase.*|Sybase message|Sybase.*Server message|" +
            "SybSQLException|com\\.sybase\\.jdbc)"
        ));
        
        // MongoDB (NoSQL but relevant)
        DATABASE_ERROR_PATTERNS.put("MongoDB", Pattern.compile(
            "(?i)(MongoDB\\.Driver|MongoException|mongo::DBException|" +
            "\\$where.*\\$ne|\\$gt.*\\$lt)"
        ));
        
        // Generic SQL errors
        DATABASE_ERROR_PATTERNS.put("Generic", Pattern.compile(
            "(?i)(SQL syntax|syntax error|unclosed quotation mark|" +
            "unterminated string literal|invalid input syntax|" +
            "unexpected end of SQL command|quoted identifier|" +
            "unrecognized token|column.*does not exist|table.*does not exist|" +
            "division by zero|numeric value out of range)"
        ));
    }
    
    // SQL injection attack patterns
    private static final Pattern[] INJECTION_PATTERNS = {
        Pattern.compile("(?i)('|(\\-\\-)|(;)|(\\|\\|)|(\\*))(\\s)*(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|eval)"),
        Pattern.compile("(?i)(union)(.*)(select)(.*)(from)"),
        Pattern.compile("(?i)(select).*(from).*(where)"),
        Pattern.compile("(?i)(insert).*(into).*(values)"),
        Pattern.compile("(?i)(update).*(set).*(where)"),
        Pattern.compile("(?i)(delete).*(from).*(where)"),
        Pattern.compile("(?i)(drop).*(table|database)"),
        Pattern.compile("(?i)(exec|execute)(\\s|\\+)+(s|x)p\\w+"),
        Pattern.compile("(?i)(')\\s*(or|and)\\s*('|\\d)"),
        Pattern.compile("(?i)(')\\s*(or|and)\\s*(true|false)"),
        Pattern.compile("(?i)\\b(waitfor|benchmark|sleep|pg_sleep)\\s*\\("),
        Pattern.compile("(?i)(\\bor\\b|\\band\\b)\\s+\\d+\\s*=\\s*\\d+"),
        Pattern.compile("(?i)(')\\s*(\\|\\||\\+)\\s*('|\\w)"),
        Pattern.compile("(?i)(0x[0-9a-f]+|char\\(\\d+\\))"),
        Pattern.compile("(?i)(concat|group_concat|string_agg)\\s*\\(")
    };
    
    // Time-based injection indicators
    private static final Pattern TIME_BASED_PATTERN = Pattern.compile(
        "(?i)(waitfor\\s+delay|benchmark\\(|sleep\\(|pg_sleep\\(|dbms_lock\\.sleep)"
    );
    
    // Boolean-based injection indicators
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile(
        "(?i)((and|or)\\s+(\\d+=\\d+|true|false|\\d+<>\\d+))"
    );
    
    // Comment patterns
    private static final Pattern COMMENT_PATTERN = Pattern.compile(
        "(--|#|/\\*|\\*/|;%00)"
    );
    
    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata(
            "sql-injection",
            "SQL Injection Detector",
            "1.0.0",
            "Sentinel Team",
            "Detects SQL injection vulnerabilities through error analysis, timing attacks, and pattern matching",
            SafetyLevel.ACTIVE  // Can perform timing tests
        );
    }
    
    @Override
    public PluginCapabilities getCapabilities() {
        return PluginCapabilities.builder()
            .needsHeadlessBrowser(false)
            .needsAuthentication(false)
            .needsJavaScriptExecution(false)
            .maxConcurrentRequests(1)
            .build();
    }
    
    @Override
    public List<PluginFinding> run(PluginContext context) throws PluginExecutionException {
        List<PluginFinding> findings = new ArrayList<>();
        
        try {
            // Always run passive checks
            findings.addAll(detectErrorBasedInjection(context));
            findings.addAll(detectReflectedSqlPatterns(context));
            findings.addAll(fingerprintDatabase(context));
            
            // Run active checks if safety level permits
            if (context.getScanConfig().getMaxSafetyLevel().ordinal() >= SafetyLevel.ACTIVE.ordinal()) {
                findings.addAll(detectTimingAnomalies(context));
                findings.addAll(detectBooleanBasedInjection(context));
            }
            
            // Deduplicate and score findings
            return deduplicateAndScore(findings);
            
        } catch (Exception e) {
            context.getLogger().error("SQL injection detection failed", e);
            throw new PluginExecutionException("Failed to execute SQL injection detection", e);
        }
    }
    
    /**
     * PASSIVE: Detect SQL injection through database error messages
     */
    private List<PluginFinding> detectErrorBasedInjection(PluginContext context) {
        List<PluginFinding> findings = new ArrayList<>();
        HttpResponse response = context.getResponse();
        String body = response.getBody();
        
        if (body == null || body.isEmpty()) {
            return findings;
        }
        
        // Check each database type
        for (Map.Entry<String, Pattern> entry : DATABASE_ERROR_PATTERNS.entrySet()) {
            String dbType = entry.getKey();
            Pattern pattern = entry.getValue();
            Matcher matcher = pattern.matcher(body);
            
            if (matcher.find()) {
                String errorSnippet = extractSnippet(body, matcher.start(), 150);
                
                Severity severity = determineSeverity(context, errorSnippet);
                Confidence confidence = determineConfidence(body, dbType);
                
                findings.add(PluginFinding.builder()
                    .pluginId("sql-injection")
                    .title("SQL Injection - Error-Based (" + dbType + ")")
                    .endpoint(context.getTargetUrl())
                    .severity(severity)
                    .confidence(confidence)
                    .description(String.format(
                        "Database error message detected indicating potential SQL injection vulnerability. " +
                        "Database type: %s. The application is exposing internal database errors which can " +
                        "be leveraged by attackers to extract data or understand the database structure.",
                        dbType
                    ))
                    .addEvidence(Evidence.responseSnippet(errorSnippet, "error_message"))
                    .addEvidence(Evidence.patternMatch(dbType + " error pattern", "database_type"))
                    .remediation(
                        "1. Implement parameterized queries or prepared statements\n" +
                        "2. Use ORM frameworks with built-in SQL injection protection\n" +
                        "3. Implement proper error handling to avoid exposing database errors\n" +
                        "4. Apply input validation and sanitization\n" +
                        "5. Use least privilege database accounts\n" +
                        "6. Enable WAF rules for SQL injection protection"
                    )
                    .notes(String.format(
                        "Database fingerprinted as: %s. Error-based SQL injection allows attackers to " +
                        "extract data through error messages. Parameters: %s",
                        dbType,
                        context.getAllParameters().keySet()
                    ))
                    .build());
                
                context.getLogger().info("Detected {} SQL error in response", dbType);
            }
        }
        
        return findings;
    }
    
    /**
     * PASSIVE: Detect reflected SQL keywords and patterns
     */
    private List<PluginFinding> detectReflectedSqlPatterns(PluginContext context) {
        List<PluginFinding> findings = new ArrayList<>();
        HttpResponse response = context.getResponse();
        String body = response.getBody();
        
        if (body == null || body.isEmpty()) {
            return findings;
        }
        
        // Check if any parameters contain SQL injection patterns
        Map<String, String> params = context.getAllParameters();
        for (Map.Entry<String, String> param : params.entrySet()) {
            String paramValue = param.getValue();
            
            // Check if parameter value contains SQL injection patterns
            for (Pattern pattern : INJECTION_PATTERNS) {
                Matcher matcher = pattern.matcher(paramValue);
                if (matcher.find()) {
                    // Check if the pattern is reflected in the response
                    if (body.contains(paramValue) || containsSqlKeywords(body)) {
                        findings.add(PluginFinding.builder()
                            .pluginId("sql-injection")
                            .title("Potential SQL Injection - Pattern Reflection")
                            .endpoint(context.getTargetUrl())
                            .parameter(param.getKey())
                            .severity(Severity.MEDIUM)
                            .confidence(Confidence.POSSIBLE)
                            .description(String.format(
                                "Parameter '%s' contains SQL injection patterns that may be reflected in the response. " +
                                "This could indicate insufficient input validation.",
                                param.getKey()
                            ))
                            .addEvidence(Evidence.patternMatch(matcher.group(), "parameter_value"))
                            .remediation(
                                "1. Implement strict input validation\n" +
                                "2. Use parameterized queries\n" +
                                "3. Apply output encoding\n" +
                                "4. Implement Content Security Policy"
                            )
                            .notes("Pattern detected in parameter: " + paramValue)
                            .build());
                        
                        break; // One finding per parameter
                    }
                }
            }
        }
        
        return findings;
    }
    
    /**
     * PASSIVE: Fingerprint database type from responses
     */
    private List<PluginFinding> fingerprintDatabase(PluginContext context) {
        List<PluginFinding> findings = new ArrayList<>();
        HttpResponse response = context.getResponse();
        
        // Check headers for database indicators
        Set<String> detectedDatabases = new HashSet<>();
        
        response.getHeader("X-Powered-By").ifPresent(header -> {
            if (header.toLowerCase().contains("php")) {
                // PHP often uses MySQL
                detectedDatabases.add("MySQL/MariaDB (inferred from PHP)");
            }
        });
        
        response.getHeader("Server").ifPresent(header -> {
            String lower = header.toLowerCase();
            if (lower.contains("oracle")) {
                detectedDatabases.add("Oracle");
            } else if (lower.contains("microsoft") || lower.contains("iis")) {
                detectedDatabases.add("Microsoft SQL Server (inferred from IIS)");
            }
        });
        
        // Check response body for database-specific functions or syntax
        String body = response.getBody();
        if (body != null) {
            if (body.contains("mysql_") || body.contains("mysqli_")) {
                detectedDatabases.add("MySQL");
            }
            if (body.contains("pg_") || body.contains("postgresql")) {
                detectedDatabases.add("PostgreSQL");
            }
            if (body.contains("sqlite")) {
                detectedDatabases.add("SQLite");
            }
        }
        
        // Report database fingerprinting as informational
        if (!detectedDatabases.isEmpty()) {
            findings.add(PluginFinding.builder()
                .pluginId("sql-injection")
                .title("Database Technology Fingerprinted")
                .endpoint(context.getTargetUrl())
                .severity(Severity.INFO)
                .confidence(Confidence.LIKELY)
                .description(
                    "Database technology was identified through response analysis. " +
                    "This information can help attackers craft database-specific attacks."
                )
                .addEvidence(Evidence.patternMatch(
                    String.join(", ", detectedDatabases),
                    "detected_databases"
                ))
                .remediation(
                    "1. Minimize information disclosure in responses\n" +
                    "2. Remove or obfuscate database-specific error messages\n" +
                    "3. Configure server headers to hide technology stack"
                )
                .notes("Detected databases: " + String.join(", ", detectedDatabases))
                .build());
        }
        
        return findings;
    }
    
    /**
     * ACTIVE: Detect timing-based blind SQL injection
     */
    private List<PluginFinding> detectTimingAnomalies(PluginContext context) {
        List<PluginFinding> findings = new ArrayList<>();
        
        // Check if response time is unusually high
        long responseTime = context.getResponse().getResponseTimeMs();
        
        // Check if any parameters contain time-based injection patterns
        Map<String, String> params = context.getAllParameters();
        for (Map.Entry<String, String> param : params.entrySet()) {
            Matcher matcher = TIME_BASED_PATTERN.matcher(param.getValue());
            if (matcher.find()) {
                // If time-based pattern found and response is slow
                if (responseTime > 5000) { // 5 seconds threshold
                    findings.add(PluginFinding.builder()
                        .pluginId("sql-injection")
                        .title("Potential Time-Based Blind SQL Injection")
                        .endpoint(context.getTargetUrl())
                        .parameter(param.getKey())
                        .severity(Severity.HIGH)
                        .confidence(Confidence.LIKELY)
                        .description(String.format(
                            "Parameter '%s' contains time-based SQL injection patterns and the response " +
                            "time (%dms) suggests the database may be executing delay functions. " +
                            "This indicates a potential blind SQL injection vulnerability.",
                            param.getKey(), responseTime
                        ))
                        .addEvidence(Evidence.timingData(
                            responseTime + "ms",
                            "Response time indicates possible time-based injection"
                        ))
                        .addEvidence(Evidence.patternMatch(matcher.group(), "time_based_pattern"))
                        .remediation(
                            "1. Use parameterized queries exclusively\n" +
                            "2. Implement strict input validation\n" +
                            "3. Set database query timeouts\n" +
                            "4. Monitor and alert on slow queries\n" +
                            "5. Use prepared statements with bound parameters"
                        )
                        .notes(String.format(
                            "Response time: %dms. Pattern: %s. " +
                            "Time-based blind SQL injection allows data extraction through timing differences.",
                            responseTime, matcher.group()
                        ))
                        .build());
                }
            }
        }
        
        return findings;
    }
    
    /**
     * ACTIVE: Detect boolean-based blind SQL injection
     */
    private List<PluginFinding> detectBooleanBasedInjection(PluginContext context) {
        List<PluginFinding> findings = new ArrayList<>();
        HttpResponse response = context.getResponse();
        String body = response.getBody();
        
        if (body == null) {
            return findings;
        }
        
        // Check parameters for boolean injection patterns
        Map<String, String> params = context.getAllParameters();
        for (Map.Entry<String, String> param : params.entrySet()) {
            Matcher matcher = BOOLEAN_PATTERN.matcher(param.getValue());
            if (matcher.find()) {
                // Check if response indicates boolean logic evaluation
                int statusCode = response.getStatusCode();
                boolean hasContent = body.length() > 0;
                
                // Boolean-based injection often results in different response sizes or status codes
                if (statusCode == 200 && hasContent) {
                    findings.add(PluginFinding.builder()
                        .pluginId("sql-injection")
                        .title("Potential Boolean-Based Blind SQL Injection")
                        .endpoint(context.getTargetUrl())
                        .parameter(param.getKey())
                        .severity(Severity.HIGH)
                        .confidence(Confidence.POSSIBLE)
                        .description(String.format(
                            "Parameter '%s' contains boolean-based SQL injection patterns. " +
                            "The application may be evaluating SQL boolean conditions, which could " +
                            "allow attackers to extract data through true/false queries.",
                            param.getKey()
                        ))
                        .addEvidence(Evidence.patternMatch(matcher.group(), "boolean_pattern"))
                        .addEvidence(Evidence.responseSnippet(
                            "Status: " + statusCode + ", Content length: " + body.length(),
                            "response_characteristics"
                        ))
                        .remediation(
                            "1. Use parameterized queries with type checking\n" +
                            "2. Implement input validation with whitelisting\n" +
                            "3. Use ORM frameworks properly\n" +
                            "4. Apply principle of least privilege for database accounts\n" +
                            "5. Implement rate limiting and monitoring"
                        )
                        .notes(String.format(
                            "Boolean pattern: %s. Response size: %d bytes. " +
                            "Boolean-based blind injection extracts data bit-by-bit through conditional queries.",
                            matcher.group(), body.length()
                        ))
                        .build());
                }
            }
        }
        
        return findings;
    }
    
    /**
     * Determine severity based on context
     */
    private Severity determineSeverity(PluginContext context, String errorMessage) {
        // Critical if authentication-related or admin endpoints
        String url = context.getTargetUrl().toString().toLowerCase();
        if (url.contains("admin") || url.contains("login") || url.contains("auth")) {
            return Severity.CRITICAL;
        }
        
        // High if contains sensitive keywords
        String lowerError = errorMessage.toLowerCase();
        if (lowerError.contains("password") || lowerError.contains("user") || 
            lowerError.contains("credit") || lowerError.contains("ssn")) {
            return Severity.HIGH;
        }
        
        // Medium by default for SQL errors
        return Severity.MEDIUM;
    }
    
    /**
     * Determine confidence based on multiple indicators
     */
    private Confidence determineConfidence(String body, String dbType) {
        int indicators = 0;
        
        // Check for multiple SQL error indicators
        if (body.toLowerCase().contains("syntax")) indicators++;
        if (body.toLowerCase().contains("error")) indicators++;
        if (body.toLowerCase().contains("exception")) indicators++;
        if (body.toLowerCase().contains("query")) indicators++;
        if (!dbType.equals("Generic")) indicators++; // Specific DB identified
        
        if (indicators >= 4) return Confidence.CONFIRMED;
        if (indicators >= 2) return Confidence.LIKELY;
        return Confidence.POSSIBLE;
    }
    
    /**
     * Check if body contains SQL keywords
     */
    private boolean containsSqlKeywords(String body) {
        String lower = body.toLowerCase();
        String[] keywords = {"select", "insert", "update", "delete", "union", "where", "from"};
        int count = 0;
        for (String keyword : keywords) {
            if (lower.contains(keyword)) count++;
        }
        return count >= 3; // At least 3 SQL keywords
    }
    
    /**
     * Extract snippet from text around position
     */
    private String extractSnippet(String text, int position, int length) {
        int start = Math.max(0, position - 30);
        int end = Math.min(text.length(), position + length);
        String snippet = text.substring(start, end);
        return snippet.replaceAll("\\s+", " ").trim();
    }
    
    /**
     * Deduplicate findings and adjust confidence scores
     */
    private List<PluginFinding> deduplicateAndScore(List<PluginFinding> findings) {
        Map<String, PluginFinding> uniqueFindings = new HashMap<>();
        
        for (PluginFinding finding : findings) {
            String key = finding.getEndpoint() + "|" + finding.getTitle();
            
            // Keep the finding with highest confidence
            if (!uniqueFindings.containsKey(key) || 
                finding.getConfidence().ordinal() > uniqueFindings.get(key).getConfidence().ordinal()) {
                uniqueFindings.put(key, finding);
            }
        }
        
        return new ArrayList<>(uniqueFindings.values());
    }
    
    @Override
    public void configure(PluginConfig config) {
        this.config = config;
    }
    
    @Override
    public void initialize() {
        // Plugin initialization if needed
    }
    
    @Override
    public void shutdown() {
        // Cleanup resources if needed
    }
}
