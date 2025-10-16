# SQL Injection Detection Plugin

## Overview

The SQL Injection Detection Plugin is an advanced, production-ready security plugin for Sentinel that identifies SQL injection vulnerabilities through multiple detection techniques. It supports 20+ database types and operates in both PASSIVE and ACTIVE modes.

## Features

### Detection Capabilities

#### 1. Error-Based Detection (PASSIVE)
Analyzes responses for database error messages from:
- **MySQL/MariaDB** - Syntax errors, connection errors, integrity violations
- **PostgreSQL** - Syntax errors, PSQLException patterns
- **Microsoft SQL Server** - ODBC errors, SqlException patterns
- **Oracle** - ORA-XXXXX error codes, JDBC errors
- **SQLite** - SQLite3 exceptions and errors
- **IBM DB2** - DB2 SQL errors and SQLSTATE codes
- **Informix** - Informix ODBC driver errors
- **Sybase** - Sybase message patterns
- **MongoDB** - NoSQL injection patterns
- **Generic SQL** - Common syntax errors and patterns

#### 2. Pattern-Based Detection (PASSIVE)
Identifies SQL injection attack patterns in parameters:
- UNION-based injection
- Boolean-based injection (OR/AND conditions)
- Comment injection (--, #, /* */)
- String concatenation attacks
- Hexadecimal encoding
- Stacked queries
- Function-based attacks (CONCAT, CHAR, etc.)

#### 3. Timing Analysis (ACTIVE)
Detects time-based blind SQL injection:
- WAITFOR DELAY (SQL Server)
- BENCHMARK() (MySQL)
- SLEEP() (MySQL)
- PG_SLEEP() (PostgreSQL)
- DBMS_LOCK.SLEEP (Oracle)

Flags responses with >5 second delays when time-based patterns are present.

#### 4. Boolean-Based Detection (ACTIVE)
Identifies boolean-based blind injection:
- True/false condition testing
- Numeric comparison patterns
- Response size/status code analysis

#### 5. Database Fingerprinting (PASSIVE)
Identifies database technology through:
- Response headers analysis
- Error message patterns
- Database-specific function names
- Technology stack indicators

### Severity Scoring

The plugin uses context-aware severity assignment:

- **CRITICAL** - SQL errors on admin/auth endpoints
- **HIGH** - Time-based or boolean-based blind injection, sensitive data exposure
- **MEDIUM** - Error-based injection, standard endpoints
- **LOW** - Database fingerprinting, minor information disclosure
- **INFO** - Technology stack identification

### Confidence Levels

- **CONFIRMED** - Multiple indicators present (4+)
- **LIKELY** - Several indicators present (2-3)
- **POSSIBLE** - Single indicator detected

## Usage

### Basic Scan (PASSIVE Mode)

```bash
# Only error-based detection
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar scan \
  --target http://localhost:3000 \
  --safety PASSIVE
```

### Advanced Scan (ACTIVE Mode)

```bash
# Includes timing and boolean-based detection
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar scan \
  --target http://localhost:3000 \
  --safety ACTIVE \
  --consent consent.json
```

### Targeted Parameter Testing

The plugin automatically analyzes all request parameters for SQL injection patterns. Focus on endpoints with:
- User input fields
- Search functionality
- Filtering/sorting parameters
- ID parameters
- Authentication forms

## Detection Examples

### Example 1: MySQL Error-Based Injection

**Request:**
```
GET /user?id=1' HTTP/1.1
```

**Response:**
```
You have an error in your SQL syntax; check the manual that corresponds 
to your MySQL server version for the right syntax to use near ''1''' at line 1
```

**Finding:**
- **Title:** SQL Injection - Error-Based (MySQL)
- **Severity:** MEDIUM
- **Confidence:** CONFIRMED
- **Evidence:** Error message snippet, MySQL pattern match

### Example 2: Time-Based Blind Injection

**Request:**
```
GET /product?id=1; WAITFOR DELAY '00:00:05'-- HTTP/1.1
```

**Response Time:** 6,234ms

**Finding:**
- **Title:** Potential Time-Based Blind SQL Injection
- **Severity:** HIGH
- **Confidence:** LIKELY
- **Evidence:** Time-based pattern, response timing data

### Example 3: Boolean-Based Injection

**Request:**
```
GET /search?q=admin' AND 1=1-- HTTP/1.1
```

**Finding:**
- **Title:** Potential Boolean-Based Blind SQL Injection
- **Severity:** HIGH
- **Confidence:** POSSIBLE
- **Evidence:** Boolean pattern match, response characteristics

## Remediation Guidance

The plugin provides comprehensive remediation advice for each finding:

### Primary Defenses

1. **Use Parameterized Queries**
   ```java
   // ✅ SAFE
   PreparedStatement stmt = conn.prepareStatement(
       "SELECT * FROM users WHERE id = ?"
   );
   stmt.setInt(1, userId);
   ```

2. **Use ORM Frameworks Properly**
   ```java
   // ✅ SAFE (JPA/Hibernate)
   Query query = em.createQuery(
       "SELECT u FROM User u WHERE u.id = :id"
   );
   query.setParameter("id", userId);
   ```

3. **Input Validation**
   - Whitelist allowed characters
   - Validate data types
   - Enforce length limits
   - Reject SQL keywords in user input

4. **Error Handling**
   - Never expose database errors to users
   - Log errors securely server-side
   - Return generic error messages
   - Implement custom error pages

5. **Least Privilege**
   - Use database accounts with minimal permissions
   - Separate read/write accounts
   - Disable dangerous functions (xp_cmdshell, etc.)
   - Implement row-level security

### Additional Protections

6. **Web Application Firewall (WAF)**
   - Deploy ModSecurity or cloud WAF
   - Enable SQL injection rule sets
   - Monitor and tune rules

7. **Security Headers**
   - Content-Security-Policy
   - X-Content-Type-Options
   - Strict-Transport-Security

8. **Monitoring & Alerting**
   - Log all database errors
   - Alert on SQL error patterns
   - Monitor query execution times
   - Track failed authentication attempts

## Configuration

### Plugin Configuration

```yaml
plugins:
  sql-injection:
    enabled: true
    # Timing threshold for time-based detection (ms)
    timing_threshold: 5000
    # Minimum confidence level to report
    min_confidence: POSSIBLE
    # Check specific database types only
    database_types:
      - MySQL
      - PostgreSQL
      - MSSQL
```

### Safety Level Requirements

- **PASSIVE** - Error analysis, pattern detection, fingerprinting
- **ACTIVE** - All passive checks + timing analysis + boolean testing
- **EXPERT** - Reserved for future advanced techniques

## False Positive Reduction

The plugin implements several strategies to minimize false positives:

1. **Context-Aware Detection**
   - Checks content type (only analyzes HTML/text responses)
   - Validates parameter reflection context
   - Considers response status codes

2. **Multi-Indicator Validation**
   - Requires multiple indicators for high confidence
   - Cross-references different detection methods
   - Validates timing anomalies

3. **Deduplication**
   - Removes duplicate findings per endpoint
   - Keeps highest confidence finding
   - Aggregates evidence

4. **Smart Thresholds**
   - Adjustable timing thresholds
   - Configurable confidence levels
   - Severity based on endpoint context

## Performance Considerations

### Resource Usage

- **CPU:** Low to moderate (pattern matching, regex)
- **Memory:** Minimal (processes responses in streaming fashion)
- **Network:** No additional requests in PASSIVE mode
- **Time:** <100ms per response in PASSIVE mode

### Optimization Tips

1. **Use PASSIVE mode** for initial reconnaissance
2. **Target specific endpoints** for ACTIVE testing
3. **Adjust timing thresholds** based on network latency
4. **Enable only needed database types** in configuration

## Testing

### Running Tests

```bash
cd sentinel-plugins
mvn test -Dtest=SqlInjectionPluginTest
```

### Test Coverage

- ✅ MySQL error detection
- ✅ PostgreSQL error detection
- ✅ MSSQL error detection
- ✅ Oracle error detection
- ✅ SQLite error detection
- ✅ Pattern-based detection
- ✅ UNION injection detection
- ✅ Time-based detection
- ✅ Boolean-based detection
- ✅ Database fingerprinting
- ✅ Severity assignment
- ✅ False positive handling
- ✅ Empty/null response handling
- ✅ Deduplication
- ✅ Evidence collection
- ✅ Safety level respect

### Test Against Vulnerable Apps

```bash
# Start OWASP Juice Shop
docker-compose up -d

# Scan for SQL injection
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar scan \
  --target http://localhost:3000 \
  --depth 10 \
  --safety ACTIVE
```

## Integration

### CI/CD Pipeline

```yaml
# .github/workflows/security-scan.yml
- name: SQL Injection Scan
  run: |
    java -jar sentinel-cli.jar scan \
      --target ${{ env.STAGING_URL }} \
      --consent consent.json \
      --safety ACTIVE \
      --output results.json
    
    # Fail build if HIGH or CRITICAL findings
    if grep -q '"severity":"HIGH\|CRITICAL"' results.json; then
      exit 1
    fi
```

### SIEM Integration

```bash
# Send findings to SIEM
java -jar sentinel-cli.jar scan \
  --target https://app.example.com \
  --consent consent.json \
  --output /var/log/sentinel/sqli-scan.json

# Forward to Splunk/ELK
curl -X POST https://siem.example.com/api/events \
  -H "Content-Type: application/json" \
  -d @/var/log/sentinel/sqli-scan.json
```

## Limitations

### Known Limitations

1. **Second-Order Injection** - Limited detection (requires multiple requests)
2. **Stored Procedures** - Cannot analyze internal stored procedure logic
3. **NoSQL Injection** - Basic MongoDB detection only
4. **ORM-Specific** - May miss ORM-specific injection patterns
5. **Encrypted Parameters** - Cannot analyze encrypted/encoded parameters

### Future Enhancements

- Machine learning for pattern recognition
- Second-order injection tracking
- Advanced NoSQL injection detection
- ORM-specific attack patterns
- Payload mutation and fuzzing
- Automated exploitation validation (EXPERT mode)

## Security Considerations

### Ethical Use

This plugin is designed for **authorized security testing only**:

- ✅ Use only on systems you own or have permission to test
- ✅ Obtain valid consent before scanning
- ✅ Respect rate limits and robots.txt
- ✅ Report findings responsibly
- ❌ Never use for unauthorized access
- ❌ Never exploit detected vulnerabilities without permission

### Safe Testing

The plugin follows Sentinel's protection-first principles:

- **No exploitation** - Only detects, never exploits
- **Safe payloads** - Uses detection patterns, not malicious code
- **Rate limited** - Respects configured rate limits
- **Consent required** - ACTIVE mode requires valid consent
- **Audit trail** - All actions logged for compliance

## Support

### Troubleshooting

**Q: Plugin not detecting known SQL injection?**
- Verify safety level is ACTIVE for timing/boolean tests
- Check if error messages are being suppressed by WAF
- Review logs for plugin execution errors
- Ensure parameters are being analyzed

**Q: Too many false positives?**
- Increase minimum confidence level in configuration
- Use PASSIVE mode for initial scans
- Review and tune database type filters
- Check timing threshold settings

**Q: Plugin timing out?**
- Reduce timing threshold for faster scans
- Disable ACTIVE checks if not needed
- Check network latency to target
- Review plugin execution logs

### Getting Help

- **Documentation:** [docs/](../docs/)
- **Issues:** GitHub Issues
- **Discussions:** GitHub Discussions
- **Security:** Report vulnerabilities responsibly

## References

- [OWASP SQL Injection](https://owasp.org/www-community/attacks/SQL_Injection)
- [OWASP Testing Guide - SQL Injection](https://owasp.org/www-project-web-security-testing-guide/latest/4-Web_Application_Security_Testing/07-Input_Validation_Testing/05-Testing_for_SQL_Injection)
- [CWE-89: SQL Injection](https://cwe.mitre.org/data/definitions/89.html)
- [CAPEC-66: SQL Injection](https://capec.mitre.org/data/definitions/66.html)

---

**Version:** 1.0.0  
**Last Updated:** 2024  
**Maintainer:** Sentinel Team
