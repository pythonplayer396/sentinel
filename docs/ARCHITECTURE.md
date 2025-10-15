# Sentinel Architecture

## Overview

Sentinel is built with a modular, layered architecture that prioritizes safety, extensibility, and maintainability.

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     User Interfaces                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   CLI        │  │   GUI        │  │   API        │      │
│  │  (picocli)   │  │  (JavaFX)    │  │  (REST)      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                     Core Engine                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Scan Engine                             │   │
│  │  - Orchestration                                     │   │
│  │  - Workflow Management                               │   │
│  └──────────────────────────────────────────────────────┘   │
│                            │                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Crawler    │  │   Plugin     │  │   Consent    │      │
│  │   Manager    │  │   Executor   │  │   Manager    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                            │                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   HTTP       │  │   Session    │  │   Report     │      │
│  │   Client     │  │   Manager    │  │   Generator  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                     Plugin System                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Plugin Manager                          │   │
│  │  - Loading & Lifecycle                               │   │
│  │  - Sandboxing & Isolation                            │   │
│  └──────────────────────────────────────────────────────┘   │
│                            │                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Security   │  │   Info       │  │   Custom     │      │
│  │   Headers    │  │   Disclosure │  │   Plugins    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                  Infrastructure Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Storage    │  │   Logging    │  │   Config     │      │
│  │  (SQLite/PG) │  │  (SLF4J)     │  │  (YAML)      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

## Module Structure

### sentinel-plugin-api

**Purpose**: Stable API for plugin development

**Key Components**:
- `ScannerPlugin` - Main plugin interface
- `PluginContext` - Execution context
- `PluginFinding` - Structured findings
- `SafetyLevel` - Safety classification

**Dependencies**: None (minimal)

### sentinel-core

**Purpose**: Core scanning engine and infrastructure

**Key Components**:
- **consent/** - Permission management
- **crawler/** - Web crawling engine
- **http/** - HTTP client wrapper
- **plugin/** - Plugin management
- **scanner/** - Scan orchestration

**Dependencies**: plugin-api, Apache HttpClient, Jsoup

### sentinel-plugins

**Purpose**: Built-in detection modules

**Plugins**:
- SecurityHeadersPlugin
- InformationDisclosurePlugin
- ReflectionDetectorPlugin

**Dependencies**: plugin-api

### sentinel-cli

**Purpose**: Command-line interface

**Commands**:
- scan - Execute scans
- consent - Manage consent
- plugin - Manage plugins
- report - Generate reports

**Dependencies**: core, plugins, picocli

### sentinel-gui

**Purpose**: JavaFX graphical interface

**Status**: Structure ready, implementation pending

**Dependencies**: core, plugins, JavaFX

### sentinel-worker

**Purpose**: Distributed worker nodes

**Status**: Structure ready, implementation pending

**Dependencies**: core, plugins

## Data Flow

### Scan Execution Flow

```
1. User initiates scan
   ↓
2. Validate consent
   ↓
3. Initialize HTTP client with rate limiter
   ↓
4. Start crawler
   ↓
5. For each discovered page:
   a. Fetch page
   b. Extract links/forms
   c. Create plugin context
   d. Execute applicable plugins
   e. Collect findings
   ↓
6. Generate report
   ↓
7. Archive results
```

### Plugin Execution Flow

```
1. Plugin Manager loads plugins
   ↓
2. Scan Engine requests applicable plugins
   ↓
3. For each page:
   a. Create sanitized context
   b. Plugin Executor runs plugin
   c. Enforce timeout
   d. Collect findings
   e. Handle errors
   ↓
4. Aggregate findings
   ↓
5. Apply confidence thresholds
```

## Key Design Patterns

### 1. Strategy Pattern

**Used in**: Plugin system

Plugins implement `ScannerPlugin` interface, allowing runtime selection and execution.

### 2. Builder Pattern

**Used in**: Configuration, findings, consent

Provides fluent API for complex object construction.

### 3. Factory Pattern

**Used in**: HTTP client, crawler creation

Centralizes object creation with proper initialization.

### 4. Observer Pattern

**Used in**: Scan progress reporting

Allows UI/CLI to monitor scan progress.

### 5. Singleton Pattern

**Used in**: Plugin manager, consent manager

Ensures single instance for shared resources.

## Safety Mechanisms

### 1. Consent Enforcement

```java
// Before any scan
if (!consentManager.hasValidConsent(target)) {
    throw new ScanException("No valid consent");
}
```

### 2. Rate Limiting

```java
// Before each request
rateLimiter.acquire();
httpClient.execute(request);
```

### 3. Plugin Sandboxing

```java
// Execute with timeout
Future<List<PluginFinding>> future = executor.submit(() -> 
    plugin.run(context)
);
findings = future.get(timeout, TimeUnit.SECONDS);
```

### 4. Safety Level Checks

```java
// Filter plugins by safety level
if (plugin.getSafetyLevel().requiresMorePermissionsThan(maxLevel)) {
    skip(plugin);
}
```

## Concurrency Model

### Thread Pools

- **Crawler**: Configurable thread pool (default: 2)
- **Plugin Executor**: Fixed thread pool (default: 4)
- **HTTP Client**: Connection pool managed by Apache HttpClient

### Synchronization

- **Visited URLs**: ConcurrentHashMap.newKeySet()
- **Findings**: Thread-safe collections
- **Consent Store**: ConcurrentHashMap

## Error Handling

### Levels

1. **Fatal**: Stop scan immediately
2. **Error**: Log and continue
3. **Warning**: Log and proceed
4. **Info**: Informational only

### Recovery Strategies

- **Network errors**: Retry with exponential backoff
- **Plugin errors**: Isolate and continue
- **Timeout**: Cancel and move to next
- **Rate limit**: Throttle automatically

## Performance Considerations

### Optimization Strategies

1. **Connection pooling** - Reuse HTTP connections
2. **Concurrent crawling** - Parallel page fetching
3. **Plugin parallelization** - Run plugins concurrently
4. **Lazy loading** - Load plugins on demand
5. **Response caching** - Cache robots.txt, etc.

### Resource Limits

- **Memory**: Truncate large responses
- **CPU**: Plugin execution timeouts
- **Network**: Rate limiting
- **Disk**: Rotating log files

## Security Considerations

### Input Validation

- URL validation and normalization
- Parameter sanitization
- Header validation

### Output Sanitization

- Redact credentials from logs
- Escape HTML in reports
- Truncate sensitive data

### Cryptography

- Consent document signing (optional)
- Credential encryption (AES-256)
- Secure random for IDs

## Extensibility Points

### 1. Custom Plugins

Implement `ScannerPlugin` interface.

### 2. Custom Reports

Implement report generator interface.

### 3. Custom Storage

Implement storage backend interface.

### 4. Custom Authentication

Implement auth flow interface.

## Deployment Models

### Standalone

Single JAR with embedded dependencies.

### Distributed

- Central coordinator
- Multiple worker nodes
- Shared storage (PostgreSQL)
- Message queue (RabbitMQ/Kafka)

### Cloud-Native

- Container-based deployment
- Kubernetes orchestration
- Auto-scaling workers
- Cloud storage integration

## Monitoring & Observability

### Logging

- Structured logging with SLF4J
- Configurable log levels
- Rotating file appenders

### Metrics

- Scan duration
- Pages crawled
- Findings count
- Plugin execution time

### Audit Trail

- Consent usage
- Scan operations
- Configuration changes
- Finding modifications

## Future Architecture Enhancements

### Phase 2

- WebSocket for real-time updates
- GraphQL API
- Redis caching layer
- Elasticsearch for findings

### Phase 3

- Microservices architecture
- Event-driven processing
- Stream processing (Kafka)
- Time-series metrics (Prometheus)

## References

- [Plugin Development Guide](PLUGIN_DEVELOPMENT.md)
- [Consent Guide](CONSENT_GUIDE.md)
- [API Documentation](API_REFERENCE.md)
