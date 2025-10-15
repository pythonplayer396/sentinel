# Sentinel Ethical Charter

## Our Commitment

Sentinel is built on the principle of **protection-first security testing**. This charter defines the ethical boundaries and responsibilities for all users, developers, and contributors.

## Core Principles

### 1. Authorization First

- **NEVER** scan systems without explicit authorization
- **ALWAYS** obtain written consent before testing
- **RESPECT** the scope defined in consent documents
- **DOCUMENT** all scanning activities for audit purposes

### 2. Do No Harm

- **AVOID** any actions that could disrupt services
- **RESPECT** rate limits and server capacity
- **STOP** immediately if issues are detected
- **REPORT** responsibly to system owners

### 3. Privacy & Confidentiality

- **PROTECT** sensitive data discovered during scans
- **REDACT** credentials, tokens, and PII from reports
- **SECURE** scan results and findings
- **RESPECT** data retention policies

### 4. Transparency

- **IDENTIFY** yourself and your scanner (User-Agent)
- **RESPECT** robots.txt and security.txt directives
- **COMMUNICATE** with system owners
- **DISCLOSE** findings responsibly

## User Responsibilities

### Before Scanning

1. ✅ Obtain written authorization
2. ✅ Create and sign a consent document
3. ✅ Define appropriate scope and limits
4. ✅ Coordinate with system owners
5. ✅ Plan for incident response

### During Scanning

1. ✅ Monitor scan progress and impact
2. ✅ Respect configured rate limits
3. ✅ Stop if unexpected behavior occurs
4. ✅ Document all activities
5. ✅ Be available for communication

### After Scanning

1. ✅ Review findings for false positives
2. ✅ Redact sensitive information
3. ✅ Report findings responsibly
4. ✅ Provide remediation guidance
5. ✅ Archive consent and audit logs

## Developer Responsibilities

### Plugin Development

- **DESIGN** for safety - no exploit code
- **TEST** thoroughly before release
- **DOCUMENT** plugin behavior clearly
- **CLASSIFY** safety level accurately
- **VALIDATE** inputs and outputs

### Core Development

- **ENFORCE** consent requirements
- **IMPLEMENT** safety checks
- **AUDIT** code for security issues
- **MAINTAIN** audit trails
- **SUPPORT** responsible disclosure

## Prohibited Activities

### ❌ Never Do This

1. Scan without authorization
2. Attempt to exploit vulnerabilities
3. Modify or delete data
4. Bypass authentication
5. Brute force credentials
6. Exfiltrate sensitive data
7. Disrupt services
8. Ignore rate limits
9. Disable safety features
10. Share unauthorized findings

## Consent Requirements

### Mandatory Elements

Every consent document must include:

- **Target URLs** - Explicit list of authorized targets
- **Organization** - Legal entity authorizing the scan
- **Authorizer** - Individual with authority to grant permission
- **Validity Period** - Start and end dates
- **Scope** - Safety level, rate limits, depth
- **Signature** - Digital or physical signature

### Special Cases

- **Localhost/Loopback** - No consent required (development only)
- **Public Bug Bounties** - Consent via program terms
- **Penetration Tests** - Formal engagement letter required
- **Research** - IRB approval for academic research

## Incident Response

### If You Discover Critical Issues

1. **STOP** the scan immediately
2. **DOCUMENT** the finding
3. **NOTIFY** the system owner promptly
4. **SECURE** evidence appropriately
5. **FOLLOW** responsible disclosure timeline

### If Your Scan Causes Issues

1. **STOP** immediately
2. **NOTIFY** system owner
3. **DOCUMENT** what happened
4. **COOPERATE** with investigation
5. **LEARN** and improve processes

## Enforcement

### Violations

Violations of this charter may result in:

- Revocation of consent
- Legal action
- Community ban
- Reporting to authorities

### Reporting Violations

If you observe misuse of Sentinel:

1. Document the violation
2. Report to project maintainers
3. Preserve evidence
4. Cooperate with investigation

## Legal Disclaimer

**IMPORTANT**: This charter does not constitute legal advice. Users are responsible for:

- Understanding applicable laws
- Obtaining proper authorization
- Complying with regulations
- Accepting liability for their actions

## Affirmation

By using Sentinel, you affirm that:

- ✅ I will only scan authorized targets
- ✅ I will obtain proper consent
- ✅ I will respect safety boundaries
- ✅ I will report findings responsibly
- ✅ I will do no harm

---

**Last Updated**: 2024-01-01  
**Version**: 1.0.0

For questions about this charter, contact the project maintainers.
