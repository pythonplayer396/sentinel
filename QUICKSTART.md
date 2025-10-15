# Sentinel Quick Start Guide

Get up and running with Sentinel in 5 minutes!

## Prerequisites

- Java 17 or later
- Maven 3.8 or later
- Docker (optional, for test lab)

## Step 1: Build Sentinel

```bash
cd sentinel
mvn clean package
```

## Step 2: Start the Test Lab

```bash
docker-compose up -d
```

Test targets available at:
- **Juice Shop**: http://localhost:3000
- **WebGoat**: http://localhost:8080/WebGoat

## Step 3: Run Your First Scan

```bash
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar scan --target http://localhost:3000
```

## Step 4: Create Consent for External Targets

```bash
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar consent create \
  --target https://example.com \
  --org "My Company" \
  --authorized-by "Your Name" \
  --file consent.json
```

## Next Steps

- Read the [README](README.md)
- Review the [Ethical Charter](ETHICAL_CHARTER.md)
- Explore [Plugin Development](docs/PLUGIN_DEVELOPMENT.md)
