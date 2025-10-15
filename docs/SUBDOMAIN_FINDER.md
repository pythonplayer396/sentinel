# Subdomain Finder Plugin

## Overview

The **Subdomain Finder Plugin** is a passive reconnaissance tool that discovers subdomains for a target domain using multiple free data sources. It requires **no API keys** to function and operates entirely within Sentinel's protection-first philosophy.

## Features

### 🔍 Discovery Methods

1. **Certificate Transparency Logs** (crt.sh)
   - Queries public CT logs for SSL/TLS certificates
   - Discovers subdomains from certificate Subject Alternative Names (SANs)
   - Completely free, no authentication required
   - Historical data going back years

2. **DNS Enumeration**
   - Tests common subdomain names (www, mail, api, etc.)
   - Built-in wordlist of ~100 common subdomains
   - Concurrent DNS resolution for speed
   - Respects rate limits

3. **Wildcard Detection**
   - Automatically detects wildcard DNS configurations
   - Prevents false positives
   - Alerts when wildcard DNS is detected

4. **Subdomain Takeover Detection**
   - Checks discovered subdomains for takeover vulnerabilities
   - Detects dangling DNS records
   - Identifies common cloud service patterns (S3, GitHub Pages, etc.)

## Safety Level

**PASSIVE** - This plugin only queries public data sources and performs standard DNS lookups. No consent is required for localhost targets.

## Configuration

### Basic Configuration

```yaml
subdomain-finder:
  max-subdomains: 1000          # Maximum subdomains to discover
  timeout-seconds: 30            # Timeout for API calls
  enable-crtsh: true            # Use Certificate Transparency
  enable-dns-enum: true         # Use DNS enumeration
  enable-wildcard-detection: true  # Detect wildcard DNS
```

### Advanced Configuration (Optional API Keys)

While the plugin works perfectly without API keys, you can optionally configure additional data sources:

```yaml
subdomain-finder:
  api-keys:
    securitytrails: "your-key"  # Optional: SecurityTrails API
    virustotal: "your-key"      # Optional: VirusTotal API
```

**Note**: API keys are **completely optional**. The plugin discovers plenty of subdomains using free sources.

## Usage

### Basic Scan

```bash
# Scan a domain for subdomains
sentinel scan --target https://example.com

# The subdomain finder will automatically run as part of the scan
```

### With Custom Configuration

```bash
# Use custom subdomain finder settings
sentinel scan \
  --target https://example.com \
  --config examples/plugin-configs/subdomain-finder.yml
```

### View Plugin Information

```bash
# List all plugins including subdomain finder
sentinel plugin list
```

## Output

### Finding Types

The plugin generates the following types of findings:

1. **Subdomains Discovered** (INFO)
   - Lists all discovered subdomains
   - Includes evidence of discovery method
   - Limited to first 50 in display (full list in JSON)

2. **Wildcard DNS Detected** (INFO)
   - Alerts when domain uses wildcard DNS
   - Warns about potential false positives

3. **Potential Subdomain Takeover** (HIGH)
   - Identifies subdomains vulnerable to takeover
   - Includes evidence from HTTP response
   - Provides remediation guidance

### Example Output

```
Finding: Subdomains Discovered
  Severity: INFO
  Confidence: CONFIRMED
  Description: Discovered 47 subdomains for example.com
  Evidence:
    - www.example.com
    - mail.example.com
    - api.example.com
    - dev.example.com
    ... and 43 more

Finding: Potential Subdomain Takeover
  Severity: HIGH
  Confidence: LIKELY
  Endpoint: http://old.example.com
  Description: Subdomain may be vulnerable to takeover
  Evidence: "NoSuchBucket" found in HTTP response
  Remediation: Verify subdomain ownership and remove dangling DNS records
```

## Data Sources

### Free Sources (No Authentication)

1. **crt.sh** - Certificate Transparency Logs
   - URL: https://crt.sh
   - Rate Limit: Reasonable use
   - Coverage: Excellent for HTTPS sites

2. **DNS Resolution** - Standard DNS queries
   - Uses system DNS resolver
   - No rate limits (built-in throttling)
   - Coverage: Active subdomains only

### Optional Enhanced Sources

1. **SecurityTrails** (Optional)
   - Free Tier: 50 queries/month
   - Sign up: https://securitytrails.com/
   - Coverage: Historical DNS data

2. **VirusTotal** (Optional)
   - Free Tier: 4 requests/minute
   - Sign up: https://www.virustotal.com/
   - Coverage: Passive DNS data

## Performance

- **Speed**: 30-60 seconds for typical domain
- **Concurrency**: 10 threads for DNS enumeration, 20 for verification
- **Memory**: Minimal (< 50MB for 1000 subdomains)
- **Network**: ~100-500 DNS queries depending on configuration

## Limitations

1. **Passive Only**: Only discovers publicly known subdomains
2. **No Brute Force**: Does not perform exhaustive subdomain brute-forcing
3. **DNS Required**: Subdomains must have DNS records to be discovered
4. **Rate Limits**: Respects API rate limits (may miss some results)

## Security Considerations

### Safe by Design

- ✅ Only queries public data sources
- ✅ No authentication required
- ✅ No destructive operations
- ✅ Respects rate limits
- ✅ Timeout protection

### Privacy

- ✅ No data sent to third parties (except public APIs)
- ✅ No tracking or analytics
- ✅ All queries logged for audit trail

## Troubleshooting

### No Subdomains Found

**Possible Causes:**
- Domain has no SSL certificates in CT logs
- All subdomains use non-standard names
- DNS resolution issues

**Solutions:**
- Check if domain has HTTPS enabled
- Verify DNS resolution is working
- Try with a well-known domain (e.g., google.com) to test

### Timeout Errors

**Possible Causes:**
- Slow network connection
- crt.sh API is slow/down
- DNS resolver is slow

**Solutions:**
- Increase `timeout-seconds` in configuration
- Disable `enable-crtsh` temporarily
- Check network connectivity

### Too Many Results

**Possible Causes:**
- Wildcard DNS configuration
- Large organization with many subdomains

**Solutions:**
- Increase `max-subdomains` limit
- Review wildcard DNS warning
- Filter results in post-processing

## Examples

### Example 1: Basic Subdomain Discovery

```bash
sentinel scan --target https://example.com
```

### Example 2: High-Speed Scan

```yaml
subdomain-finder:
  timeout-seconds: 10
  enable-dns-enum: false  # Skip DNS enum for speed
  enable-crtsh: true      # Only use CT logs
```

### Example 3: Comprehensive Scan

```yaml
subdomain-finder:
  max-subdomains: 5000
  timeout-seconds: 60
  enable-crtsh: true
  enable-dns-enum: true
  enable-wildcard-detection: true
```

## Integration

### Programmatic Usage

```java
// Create and configure plugin
SubdomainFinderPlugin plugin = new SubdomainFinderPlugin();

Map<String, Object> config = new HashMap<>();
config.put("max-subdomains", 500);
config.put("enable-crtsh", true);

plugin.configure(new PluginConfig(config));
plugin.initialize();

// Run plugin
PluginContext context = createContext(targetUrl, request, response);
List<PluginFinding> findings = plugin.run(context);

// Process findings
for (PluginFinding finding : findings) {
    System.out.println(finding.getTitle() + ": " + finding.getDescription());
}
```

### CI/CD Integration

```bash
# In your CI/CD pipeline
sentinel scan \
  --target https://staging.example.com \
  --output subdomain-report.json \
  --format json

# Parse results
jq '.findings[] | select(.pluginId == "subdomain-finder")' subdomain-report.json
```

## FAQ

**Q: Do I need API keys?**  
A: No! The plugin works great with just free sources (crt.sh and DNS).

**Q: How long does it take?**  
A: Typically 30-60 seconds for most domains.

**Q: Will it find all subdomains?**  
A: It finds publicly known subdomains. Private/internal subdomains won't be discovered.

**Q: Is it safe to use in production?**  
A: Yes! It only performs passive reconnaissance using public data.

**Q: Can I use it for bug bounties?**  
A: Yes, but always follow the program's rules and get proper authorization.

**Q: Does it work offline?**  
A: No, it requires internet access to query CT logs and DNS.

## Contributing

To improve the subdomain finder:

1. Add new data sources (must be free/public)
2. Improve subdomain takeover detection patterns
3. Optimize DNS resolution performance
4. Add more common subdomain names to wordlist

See [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

## License

MIT License - See [LICENSE](../LICENSE)

---

**Built with ❤️ for the security community**
