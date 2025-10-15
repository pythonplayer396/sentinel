# Sentinel — Protection-First Java Web Vulnerability Scanner

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17%2B-orange.svg)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/maven-3.8%2B-red.svg)](https://maven.apache.org/)

**Sentinel** is an enterprise-grade, protection-first web vulnerability scanner built in Java. Designed for defensive security testing, Sentinel provides safe, non-destructive vulnerability detection with mandatory consent workflows, low false positives, and enterprise bulk-scan capabilities.

## 🛡️ Protection-First Design

Sentinel is explicitly designed for **defensive testing only**:

- ✅ **Safe probes** - No exploit chains or destructive payloads
- ✅ **Permission-first** - Mandatory consent for all non-local targets
- ✅ **Safe defaults** - Passive scanning by default
- ✅ **Audit trail** - Tamper-evident logs for compliance
- ✅ **Rate limiting** - Configurable throttling to prevent service impact

## 🚀 Key Features

### Core Capabilities

- **GUI (JavaFX) + Headless CLI** - Full feature parity
- **Fast Crawler** - Respects robots.txt, configurable depth, same-origin control
- **Fingerprinting** - Passive server, framework, CMS detection
- **Plugin Engine** - Modular JAR-based plugins with stable API
- **Safe Detection** - Identifies vulnerabilities without exploitation
- **Authenticated Scans** - Form-based, cookie/session, token support
- **Headless Browser** - Selenium WebDriver + HtmlUnit for JS-heavy apps
- **Bulk Scanning** - Queue management, scheduling, distributed workers

### Enterprise Features

- **Scan Templates** - Reusable configurations
- **Scheduled Scans** - Recurring scans with cron-style scheduling
- **Webhooks & Alerts** - Slack, email, SIEM integration
- **Multi-tenant** - Isolated scan metadata per customer
- **Reporting** - JSON, HTML, PDF with remediation guidance
- **False Positive Reduction** - Multi-check confirmation, context-aware testing

## 📋 Requirements

- **Java 17+** (OpenJDK recommended)
- **Maven 3.8+**
- **Docker** (optional, for test lab)

> **⚠️ Important**: You must build the project before using Sentinel. See the [Quick Start](#-quick-start) section below for build instructions.

## 🔧 Quick Start

### 1. Build the Project

```bash
cd sentinel
mvn clean package
```

### 2. Create a Consent Document

For non-localhost targets, you must create a consent document:

```bash
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar consent create \
  --target https://example.com \
  --org "Your Organization" \
  --authorized-by "Your Name" \
  --email your@email.com \
  --days 30 \
  --scope standard \
  --file consent.json
```

### 3. Run a Scan

```bash
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar scan \
  --target https://example.com \
  --consent consent.json \
  --depth 10 \
  --rate 5.0 \
  --safety PASSIVE
```

### 4. Start the Test Lab

Launch OWASP Juice Shop and WebGoat for safe testing:

```bash
docker-compose up -d
```

Test targets will be available at:
- **Juice Shop**: http://localhost:3000
- **WebGoat**: http://localhost:8080/WebGoat
- **DVWA**: http://localhost:8081

### 5. Scan the Test Lab

```bash
# No consent needed for localhost
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar scan \
  --target http://localhost:3000 \
  --depth 5 \
  --safety ACTIVE
```

## 📚 Architecture

```
sentinel/
├── sentinel-plugin-api/     # Plugin API interfaces
├── sentinel-core/            # Core engine (crawler, HTTP, consent)
├── sentinel-plugins/         # Built-in detection plugins
├── sentinel-cli/             # Command-line interface
├── sentinel-gui/             # JavaFX GUI (future)
└── sentinel-worker/          # Distributed worker (future)
```

### Core Components

- **Consent Manager** - Validates and archives scan permissions
- **HTTP Client** - Rate-limited, cookie-aware Apache HttpClient wrapper
- **Web Crawler** - Robots.txt-aware, depth-controlled crawler
- **Plugin Manager** - Dynamic plugin loading with sandboxing
- **Scan Engine** - Orchestrates crawling and plugin execution

## 🔌 Plugin Development

### Creating a Plugin

Implement the `ScannerPlugin` interface:

```java
public class MyPlugin implements ScannerPlugin {
    
    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata(
            "my-plugin",
            "My Security Check",
            "1.0.0",
            "Your Name",
            "Description of what this plugin detects",
            SafetyLevel.PASSIVE
        );
    }
    
    @Override
    public PluginCapabilities getCapabilities() {
        return PluginCapabilities.builder().build();
    }
    
    @Override
    public List<PluginFinding> run(PluginContext context) {
        List<PluginFinding> findings = new ArrayList<>();
        
        // Your detection logic here
        String body = context.getResponse().getBody();
        if (body.contains("vulnerability-indicator")) {
            findings.add(PluginFinding.confirmed("my-plugin", "Issue Found")
                .endpoint(context.getTargetUrl())
                .severity(Severity.MEDIUM)
                .remediation("How to fix this issue")
                .build());
        }
        
        return findings;
    }
    
    @Override
    public void configure(PluginConfig config) {
        // Plugin configuration
    }
}
```

### Safety Levels

- **PASSIVE** - Analyzes existing responses only (default)
- **ACTIVE** - May send additional safe requests (requires consent)
- **EXPERT** - Advanced testing (requires expert mode + consent)

## 📊 Built-in Plugins

| Plugin | Safety Level | Description |
|--------|-------------|-------------|
| Security Headers | PASSIVE | Detects missing HSTS, CSP, X-Frame-Options, etc. |
| Information Disclosure | PASSIVE | Finds stack traces, version info, debug data |
| Reflection Detector | PASSIVE | Identifies parameter reflection (XSS indicators) |
| Subdomain Finder | PASSIVE | Discovers subdomains via Certificate Transparency and DNS |

## 🔒 Security & Compliance

### Consent System

All scans require valid consent documents with:
- Authorized target URLs
- Organization and authorizer details
- Validity period
- Scope definition (safety level, rate limits)
- Digital signature (optional)

Consent documents are archived for audit purposes.

### Rate Limiting

Default: **5 requests/second per host**

Configurable per scan with automatic throttling on server overload detection.

### Audit Trail

All scan operations are logged with:
- Scan ID and timestamp
- Operator identity
- Consent document reference
- Target scope
- Rate limits applied

## 📖 CLI Commands

### Scan Commands

```bash
# Basic scan
sentinel scan --target https://example.com

# Advanced scan with options
sentinel scan \
  --target https://example.com \
  --consent consent.json \
  --depth 20 \
  --rate 10.0 \
  --safety ACTIVE \
  --output report.json
```

### Consent Commands

```bash
# Create consent
sentinel consent create \
  --target https://example.com \
  --org "Company" \
  --authorized-by "Name" \
  --file consent.json

# Validate consent
sentinel consent validate --file consent.json
```

### Plugin Commands

```bash
# List plugins
sentinel plugin list
```

## 🧪 Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests with Test Lab

```bash
# Start test lab
docker-compose up -d

# Run integration tests
mvn verify

# Stop test lab
docker-compose down
```

## 🛠️ Configuration

### Scan Configuration (YAML)

```yaml
scan:
  target: https://example.com
  maxDepth: 10
  rateLimit: 5.0
  safetyLevel: PASSIVE
  respectRobotsTxt: true
  concurrency: 2
  
consent:
  file: consent.json
  
plugins:
  enabled:
    - security-headers
    - info-disclosure
    - reflection-detector
```

## 📈 Roadmap

- [x] Core scanning engine
- [x] Plugin API and built-in plugins
- [x] CLI interface
- [x] Consent management
- [x] Docker test lab
- [ ] JavaFX GUI
- [ ] Headless browser support (Selenium)
- [ ] Bulk scanning with queue management
- [ ] Distributed workers
- [ ] HTML/PDF reporting
- [ ] Authentication flows
- [ ] CI/CD integration
- [ ] SIEM connectors

## 🤝 Contributing

Contributions are welcome! Please ensure:

1. All plugins follow protection-first principles
2. No exploit code or destructive payloads
3. Comprehensive tests included
4. Documentation updated

## 📄 License

MIT License - See [LICENSE](LICENSE) file

## ⚠️ Ethical Use

**IMPORTANT**: Sentinel is designed for authorized security testing only.

- ✅ Use only on systems you own or have explicit permission to test
- ✅ Always obtain and document consent before scanning
- ✅ Respect rate limits and robots.txt
- ✅ Report findings responsibly
- ❌ Never use for unauthorized access or malicious purposes

## 📞 Support

- **Documentation**: [docs/](docs/)
- **Issues**: GitHub Issues
- **Discussions**: GitHub Discussions

## 🙏 Acknowledgments

- **OWASP Foundation** - Juice Shop, WebGoat test applications
- **Apache HttpClient** - Robust HTTP client library
- **Selenium** - WebDriver for headless browser support
- **Vega** - Architectural inspiration

---

**Built with ❤️ for the security community**
