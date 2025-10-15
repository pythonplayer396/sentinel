# Sentinel Deployment Guide

## Quick Deployment

### 1. Build from Source

```bash
cd sentinel
mvn clean package
```

### 2. Run Standalone

```bash
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar --help
```

## Installation Methods

### Method 1: Direct JAR Execution

No installation needed. Run directly:

```bash
java -jar sentinel-cli-1.0.0-SNAPSHOT-shaded.jar scan --target http://localhost:3000
```

### Method 2: Shell Alias

Add to `~/.bashrc` or `~/.zshrc`:

```bash
alias sentinel='java -jar /path/to/sentinel-cli-1.0.0-SNAPSHOT-shaded.jar'
```

Then use:

```bash
sentinel scan --target http://localhost:3000
```

### Method 3: System-wide Installation

```bash
sudo cp sentinel-cli/target/sentinel-cli-*-shaded.jar /usr/local/bin/sentinel.jar
sudo tee /usr/local/bin/sentinel > /dev/null << 'EOF'
#!/bin/bash
java -jar /usr/local/bin/sentinel.jar "$@"
EOF
sudo chmod +x /usr/local/bin/sentinel
```

Then use:

```bash
sentinel scan --target http://localhost:3000
```

### Method 4: Using Makefile

```bash
make install
```

Installs to `~/.local/bin/sentinel`

## Docker Deployment

### Build Docker Image

```dockerfile
FROM openjdk:17-slim

WORKDIR /app
COPY sentinel-cli/target/sentinel-cli-*-shaded.jar /app/sentinel.jar

ENTRYPOINT ["java", "-jar", "/app/sentinel.jar"]
CMD ["--help"]
```

Build:

```bash
docker build -t sentinel:latest .
```

Run:

```bash
docker run --rm sentinel:latest scan --target http://host.docker.internal:3000
```

## Configuration

### Environment Variables

```bash
export SENTINEL_HOME=/opt/sentinel
export SENTINEL_CONSENT_DIR=$SENTINEL_HOME/consent
export SENTINEL_PLUGINS_DIR=$SENTINEL_HOME/plugins
export SENTINEL_LOG_LEVEL=INFO
```

### Configuration File

Create `~/.sentinel/config.yml`:

```yaml
sentinel:
  consentDir: ~/.sentinel/consent
  pluginsDir: ~/.sentinel/plugins
  logLevel: INFO
  
defaults:
  rateLimit: 5.0
  maxDepth: 10
  safetyLevel: PASSIVE
```

## Production Deployment

### System Requirements

- **CPU**: 2+ cores recommended
- **RAM**: 2GB minimum, 4GB recommended
- **Disk**: 1GB for application, additional for logs/results
- **Network**: Outbound HTTPS access

### Security Hardening

1. **Run as non-root user**:

```bash
useradd -r -s /bin/false sentinel
sudo -u sentinel java -jar sentinel.jar
```

2. **Restrict file permissions**:

```bash
chmod 700 ~/.sentinel
chmod 600 ~/.sentinel/consent/*.json
```

3. **Enable audit logging**:

```yaml
# logback.xml
<appender name="AUDIT" class="ch.qos.logback.core.FileAppender">
  <file>/var/log/sentinel/audit.log</file>
</appender>
```

### Systemd Service

Create `/etc/systemd/system/sentinel.service`:

```ini
[Unit]
Description=Sentinel Scanner Service
After=network.target

[Service]
Type=simple
User=sentinel
WorkingDirectory=/opt/sentinel
ExecStart=/usr/bin/java -jar /opt/sentinel/sentinel.jar worker
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:

```bash
sudo systemctl enable sentinel
sudo systemctl start sentinel
```

## Distributed Deployment

### Architecture

```
┌─────────────┐
│  Coordinator│
└──────┬──────┘
       │
   ┌───┴───┐
   │ Queue │ (RabbitMQ/Kafka)
   └───┬───┘
       │
   ┌───┴────────────┬────────────┐
   │                │            │
┌──▼───┐      ┌────▼──┐    ┌───▼───┐
│Worker│      │Worker │    │Worker │
└──────┘      └───────┘    └───────┘
```

### Worker Node Setup

1. **Install Sentinel**:

```bash
scp sentinel-cli-*-shaded.jar worker1:/opt/sentinel/
```

2. **Configure worker**:

```yaml
worker:
  id: worker-1
  coordinator: http://coordinator:8080
  concurrency: 4
```

3. **Start worker**:

```bash
java -jar sentinel.jar worker --config worker.yml
```

## Monitoring

### Health Check Endpoint

```bash
curl http://localhost:8080/health
```

### Metrics

Enable Prometheus metrics:

```yaml
metrics:
  enabled: true
  port: 9090
```

### Log Aggregation

Forward logs to centralized system:

```bash
# Filebeat
filebeat.inputs:
- type: log
  paths:
    - /var/log/sentinel/*.log
```

## Backup & Recovery

### Backup Consent Documents

```bash
tar -czf consent-backup-$(date +%Y%m%d).tar.gz ~/.sentinel/consent/
```

### Backup Scan Results

```bash
tar -czf results-backup-$(date +%Y%m%d).tar.gz ~/.sentinel/results/
```

### Restore

```bash
tar -xzf consent-backup-20240101.tar.gz -C ~/.sentinel/
```

## Troubleshooting

### Issue: Out of Memory

**Solution**: Increase heap size

```bash
java -Xmx4g -jar sentinel.jar scan --target https://example.com
```

### Issue: Connection Timeout

**Solution**: Adjust timeout settings

```bash
sentinel scan --target https://example.com --timeout 60
```

### Issue: Rate Limit Errors

**Solution**: Reduce rate limit

```bash
sentinel scan --target https://example.com --rate 2.0
```

## Performance Tuning

### JVM Options

```bash
java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -Xmx4g \
     -Xms2g \
     -jar sentinel.jar
```

### Concurrency Tuning

```yaml
scan:
  concurrency: 4        # Crawler threads
  pluginThreads: 8      # Plugin execution threads
  httpConnections: 20   # HTTP connection pool
```

## Upgrade Guide

### From 1.0.x to 1.1.x

1. Backup configuration and data
2. Download new version
3. Run migration script (if needed)
4. Test with dry-run
5. Deploy to production

## Support

For deployment issues:
- GitHub Issues
- Documentation: docs/
- Email: support@sentinel-scanner.org
