# Changelog

All notable changes to Sentinel will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial release of Sentinel scanner
- Core scanning engine with crawler
- Plugin API and plugin manager
- Consent management system
- CLI interface with picocli
- Built-in detection plugins:
  - Security Headers Plugin
  - Information Disclosure Plugin
  - Reflection Detector Plugin
- Docker Compose test lab with OWASP targets
- Comprehensive documentation
- Example consent templates
- Build and test scripts

### Security
- Mandatory consent for non-local targets
- Rate limiting with configurable limits
- Audit trail for all scan operations
- Safe-by-default plugin execution

## [1.0.0] - TBD

Initial public release.

### Core Features
- Protection-first design philosophy
- Multi-level safety system (PASSIVE/ACTIVE/EXPERT)
- Modular plugin architecture
- Enterprise consent management
- Rate-limited HTTP client
- Robots.txt-aware crawler
- Same-origin scope control

### CLI Commands
- `sentinel scan` - Execute security scans
- `sentinel consent create` - Create consent documents
- `sentinel consent validate` - Validate consent
- `sentinel plugin list` - List available plugins

### Documentation
- README with quick start
- Ethical Charter
- Plugin Development Guide
- Consent Management Guide
- Contributing Guidelines

### Test Infrastructure
- OWASP Juice Shop integration
- OWASP WebGoat integration
- DVWA integration
- Automated test scripts

---

## Release Notes

### Version 1.0.0 (Planned)

**Highlights:**
- First stable release
- Production-ready core engine
- Comprehensive plugin system
- Enterprise consent framework

**Breaking Changes:**
- None (initial release)

**Known Issues:**
- GUI not yet implemented
- Headless browser support incomplete
- Bulk scanning features pending
- PDF reporting not yet available

**Upgrade Notes:**
- N/A (initial release)

---

## Future Releases

### Version 1.1.0 (Planned)
- JavaFX GUI implementation
- Selenium WebDriver integration
- HtmlUnit support
- Authentication flows

### Version 1.2.0 (Planned)
- Bulk scanning with queue management
- Distributed worker nodes
- Scheduled recurring scans
- Webhook integrations

### Version 1.3.0 (Planned)
- HTML report generation
- PDF report generation
- Custom report templates
- SIEM connectors

### Version 2.0.0 (Future)
- Advanced fingerprinting
- Machine learning for FP reduction
- Cloud-native deployment
- Enterprise SSO integration
