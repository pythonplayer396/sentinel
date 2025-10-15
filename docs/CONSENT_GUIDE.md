# Consent Management Guide

## Overview

Sentinel requires explicit consent for all security scans on non-local targets. This guide explains how to create, manage, and validate consent documents.

## Why Consent?

Consent documents serve multiple purposes:

1. **Legal Protection** - Documented authorization for security testing
2. **Audit Trail** - Tamper-evident record of permissions
3. **Scope Definition** - Clear boundaries for testing activities
4. **Compliance** - Meets regulatory requirements

## Consent Document Structure

### Required Fields

```json
{
  "consentId": "uuid-generated",
  "authorizedTargets": [
    "https://example.com",
    "https://*.example.com"
  ],
  "organizationName": "Example Corp",
  "authorizedBy": "John Doe",
  "authorizedByEmail": "john@example.com",
  "validFrom": "2024-01-01T00:00:00Z",
  "validUntil": "2024-12-31T23:59:59Z",
  "scope": {
    "maxSafetyLevel": "ACTIVE",
    "allowActiveScanning": true,
    "allowExpertMode": false,
    "allowAuthenticatedScanning": true,
    "allowHeadlessBrowser": true,
    "maxRequestsPerSecond": 5,
    "maxCrawlDepth": 20,
    "respectRobotsTxt": true
  },
  "signature": "optional-digital-signature",
  "signedAt": "2024-01-01T00:00:00Z",
  "notes": "Annual security assessment"
}
```

## Creating Consent Documents

### Using CLI

```bash
sentinel consent create \
  --target https://example.com \
  --org "Example Corp" \
  --authorized-by "John Doe" \
  --email john@example.com \
  --days 365 \
  --scope standard \
  --file consent.json
```

### Scope Presets

#### Safe (Default)

```bash
--scope safe
```

- Safety Level: PASSIVE only
- Active Scanning: No
- Expert Mode: No
- Rate Limit: 5 req/s
- Max Depth: 10

#### Standard

```bash
--scope standard
```

- Safety Level: ACTIVE
- Active Scanning: Yes
- Expert Mode: No
- Rate Limit: 5 req/s
- Max Depth: 20

#### Expert

```bash
--scope expert
```

- Safety Level: EXPERT
- Active Scanning: Yes
- Expert Mode: Yes
- Rate Limit: 10 req/s
- Max Depth: 50

### Manual Creation

Create a JSON file with the required structure:

```json
{
  "authorizedTargets": ["https://example.com"],
  "organizationName": "Example Corp",
  "authorizedBy": "John Doe",
  "authorizedByEmail": "john@example.com",
  "validFrom": "2024-01-01T00:00:00Z",
  "validUntil": "2024-12-31T23:59:59Z",
  "scope": {
    "maxSafetyLevel": "ACTIVE",
    "allowActiveScanning": true,
    "allowExpertMode": false,
    "allowAuthenticatedScanning": true,
    "allowHeadlessBrowser": true,
    "maxRequestsPerSecond": 5,
    "maxCrawlDepth": 20,
    "respectRobotsTxt": true
  },
  "notes": "Security assessment"
}
```

## Target Patterns

### Exact Match

```json
"authorizedTargets": ["https://example.com"]
```

Only `https://example.com` is authorized.

### Wildcard Subdomain

```json
"authorizedTargets": ["https://*.example.com"]
```

Authorizes:
- `https://www.example.com`
- `https://api.example.com`
- `https://app.example.com`

But NOT:
- `https://example.com` (use both if needed)
- `http://*.example.com` (different scheme)

### Multiple Targets

```json
"authorizedTargets": [
  "https://example.com",
  "https://*.example.com",
  "https://staging.example.net"
]
```

## Validating Consent

### Using CLI

```bash
sentinel consent validate --file consent.json
```

Output:
```
Validating consent document: consent.json
  Consent ID: abc-123-def
  Organization: Example Corp
  Authorized by: John Doe
  Valid from: 2024-01-01T00:00:00Z
  Valid until: 2024-12-31T23:59:59Z
  Targets: 2
✓ Consent is VALID
```

### Programmatic Validation

```java
ConsentManager manager = new ConsentManager(consentPath);
ConsentDocument consent = manager.loadConsentFromFile(path);

if (consent.isValid()) {
    System.out.println("Consent is valid");
} else {
    System.out.println("Consent is expired or not yet valid");
}

URI target = new URI("https://example.com");
if (consent.isTargetAuthorized(target)) {
    System.out.println("Target is authorized");
}
```

## Consent Lifecycle

### 1. Creation

```bash
sentinel consent create \
  --target https://example.com \
  --org "Example Corp" \
  --authorized-by "John Doe" \
  --file consent.json
```

### 2. Review & Sign

Review the generated consent document and obtain necessary signatures.

### 3. Archive

Consent documents are automatically archived:

```
~/.sentinel/consent/
  consent_abc-123_2024-01-01T00-00-00Z.json
  consent_def-456_2024-01-15T00-00-00Z.json
```

### 4. Use in Scans

```bash
sentinel scan \
  --target https://example.com \
  --consent consent.json
```

### 5. Expiration

Consent documents expire automatically. Scans will fail if consent is expired.

### 6. Revocation

```bash
# Remove consent file
rm consent.json

# Or programmatically
manager.revokeConsent(consentId);
```

## Special Cases

### Localhost/Loopback

No consent required for local testing:

```bash
# No consent needed
sentinel scan --target http://localhost:3000
sentinel scan --target http://127.0.0.1:8080
```

### Bug Bounty Programs

For public bug bounty programs:

```json
{
  "organizationName": "Example Corp Bug Bounty",
  "authorizedBy": "Bug Bounty Program",
  "notes": "Authorized via bug bounty program terms at https://example.com/security"
}
```

### Penetration Testing

For formal penetration tests:

```json
{
  "organizationName": "Example Corp",
  "authorizedBy": "CISO Name",
  "notes": "Penetration test engagement #2024-001. See SOW dated 2024-01-01."
}
```

## Best Practices

### 1. Principle of Least Privilege

```bash
# ✅ Good: Minimal scope
--scope safe --days 7

# ❌ Avoid: Excessive permissions
--scope expert --days 365
```

### 2. Specific Targets

```json
// ✅ Good: Specific targets
"authorizedTargets": ["https://staging.example.com"]

// ❌ Avoid: Overly broad
"authorizedTargets": ["https://*.com"]
```

### 3. Time-Bound

```bash
# ✅ Good: Limited duration
--days 30

# ❌ Avoid: Indefinite
--days 3650
```

### 4. Document Purpose

```json
"notes": "Q1 2024 security assessment per InfoSec-2024-001"
```

### 5. Secure Storage

```bash
# Store consent documents securely
chmod 600 consent.json

# Don't commit to version control
echo "*.consent.json" >> .gitignore
```

## Compliance

### Audit Trail

All consent usage is logged:

```
[2024-01-01 10:00:00] Consent abc-123 validated for https://example.com
[2024-01-01 10:00:05] Scan started with consent abc-123
[2024-01-01 10:15:30] Scan completed with consent abc-123
```

### Retention

Consent archives are retained indefinitely for audit purposes.

### Reporting

Include consent information in scan reports:

```json
{
  "scanMetadata": {
    "consentId": "abc-123",
    "authorizedBy": "John Doe",
    "organization": "Example Corp"
  }
}
```

## Troubleshooting

### "No valid consent" Error

```
ERROR: No valid consent for target: https://example.com
```

**Solutions**:
1. Create consent document
2. Check target URL matches exactly
3. Verify consent is not expired
4. Check consent file path

### Target Not Authorized

```
ERROR: Target not authorized by consent
```

**Solutions**:
1. Add target to authorizedTargets list
2. Use wildcard pattern if appropriate
3. Create separate consent for target

### Expired Consent

```
ERROR: Consent is expired
```

**Solutions**:
1. Create new consent document
2. Extend existing consent (create new with updated dates)

## Templates

### Development/Testing

```json
{
  "authorizedTargets": ["http://localhost:*"],
  "organizationName": "Development",
  "authorizedBy": "Developer",
  "validFrom": "2024-01-01T00:00:00Z",
  "validUntil": "2024-12-31T23:59:59Z",
  "scope": {
    "maxSafetyLevel": "EXPERT",
    "allowActiveScanning": true,
    "allowExpertMode": true,
    "allowAuthenticatedScanning": true,
    "allowHeadlessBrowser": true,
    "maxRequestsPerSecond": 10,
    "maxCrawlDepth": 50,
    "respectRobotsTxt": false
  },
  "notes": "Development testing"
}
```

### Production Assessment

```json
{
  "authorizedTargets": ["https://example.com"],
  "organizationName": "Example Corp",
  "authorizedBy": "CISO",
  "authorizedByEmail": "ciso@example.com",
  "validFrom": "2024-01-01T00:00:00Z",
  "validUntil": "2024-01-31T23:59:59Z",
  "scope": {
    "maxSafetyLevel": "ACTIVE",
    "allowActiveScanning": true,
    "allowExpertMode": false,
    "allowAuthenticatedScanning": true,
    "allowHeadlessBrowser": true,
    "maxRequestsPerSecond": 3,
    "maxCrawlDepth": 15,
    "respectRobotsTxt": true
  },
  "notes": "Monthly security scan - January 2024"
}
```

## FAQ

**Q: Can I scan without consent?**  
A: Only localhost/loopback addresses. All other targets require consent.

**Q: How do I extend an expired consent?**  
A: Create a new consent document with updated dates.

**Q: Can consent be revoked?**  
A: Yes, delete the consent file or call `revokeConsent()`.

**Q: Is digital signature required?**  
A: Optional but recommended for audit purposes.

**Q: Can I use one consent for multiple scans?**  
A: Yes, as long as it's valid and covers the targets.

## Resources

- [Ethical Charter](../ETHICAL_CHARTER.md)
- [CLI Reference](CLI_REFERENCE.md)
- [API Documentation](API_REFERENCE.md)
