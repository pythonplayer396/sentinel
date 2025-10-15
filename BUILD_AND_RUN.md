# Build and Run Guide

## Prerequisites

- **Java 17 or later** (OpenJDK recommended)
- **Maven 3.8 or later**
- **Docker** (optional, for test lab)

## Quick Start (5 Minutes)

### Step 1: Build

```bash
cd /home/darkwall/tool/sentinel
mvn clean package
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 45.123 s
```

### Step 2: Verify

```bash
./scripts/verify-build.sh
```

### Step 3: Start Test Lab

```bash
docker-compose up -d
```

Wait 30 seconds for services to start.

### Step 4: Run First Scan

```bash
java -jar sentinel-cli/target/sentinel-cli-1.0.0-SNAPSHOT-shaded.jar scan \
  --target http://localhost:3000 \
  --depth 5
```

## Detailed Build Instructions

### Clean Build

```bash
mvn clean package
```

### Build Without Tests

```bash
mvn clean package -DskipTests
```

### Build Specific Module

```bash
cd sentinel-cli
mvn clean package
```

### Build with Verbose Output

```bash
mvn clean package -X
```

## Running the Scanner

### Basic Scan

```bash
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar scan \
  --target http://localhost:3000
```

### Scan with Options

```bash
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar scan \
  --target http://localhost:3000 \
  --depth 20 \
  --rate 10.0 \
  --safety ACTIVE
```

### Create Consent

```bash
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar consent create \
  --target https://example.com \
  --org "My Company" \
  --authorized-by "Your Name" \
  --email your@email.com \
  --days 30 \
  --file consent.json
```

### List Plugins

```bash
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar plugin list
```

## Using Make Commands

### Build

```bash
make build
```

### Start Test Lab

```bash
make start-lab
```

### Run Example Scan

```bash
make run-scan
```

### List Plugins

```bash
make plugins
```

### Install System-wide

```bash
make install
```

Then use:

```bash
sentinel scan --target http://localhost:3000
```

## Test Lab Targets

After running `docker-compose up -d`:

- **OWASP Juice Shop**: http://localhost:3000
- **OWASP WebGoat**: http://localhost:8080/WebGoat
- **DVWA**: http://localhost:8081

### Example Scans

```bash
# Scan Juice Shop
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar scan \
  --target http://localhost:3000 --depth 10

# Scan WebGoat
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar scan \
  --target http://localhost:8080/WebGoat --depth 5

# Scan DVWA
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar scan \
  --target http://localhost:8081 --depth 5
```

## Troubleshooting

### Build Fails

**Issue**: Maven build fails

**Solution**:
```bash
# Clean everything
mvn clean
rm -rf ~/.m2/repository/com/sentinel

# Rebuild
mvn clean install
```

### Java Version Error

**Issue**: "Unsupported class file major version"

**Solution**: Ensure Java 17+
```bash
java -version
# Should show version 17 or higher
```

### Docker Services Not Starting

**Issue**: Test lab containers fail to start

**Solution**:
```bash
# Check Docker
docker ps

# View logs
docker-compose logs

# Restart
docker-compose down
docker-compose up -d
```

### Permission Denied

**Issue**: Cannot execute scripts

**Solution**:
```bash
chmod +x scripts/*.sh
```

### Out of Memory

**Issue**: Build runs out of memory

**Solution**:
```bash
export MAVEN_OPTS="-Xmx2g"
mvn clean package
```

## Development Workflow

### 1. Make Changes

Edit source files in your IDE.

### 2. Build Module

```bash
cd sentinel-core
mvn clean package
```

### 3. Run Tests

```bash
mvn test
```

### 4. Build All

```bash
cd ..
mvn clean package
```

### 5. Test Changes

```bash
java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar scan \
  --target http://localhost:3000
```

## IDE Setup

### IntelliJ IDEA

1. Open project: `File > Open > sentinel/pom.xml`
2. Import as Maven project
3. Set JDK to 17+
4. Build project: `Build > Build Project`

### Eclipse

1. Import: `File > Import > Maven > Existing Maven Projects`
2. Select sentinel directory
3. Set JDK to 17+
4. Build: `Project > Build All`

### VS Code

1. Open folder: sentinel
2. Install Java Extension Pack
3. Maven will auto-detect
4. Build: `Terminal > Run Task > maven: package`

## Running Tests

### All Tests

```bash
mvn test
```

### Specific Test Class

```bash
mvn test -Dtest=SecurityHeadersPluginTest
```

### Integration Tests

```bash
# Start test lab first
docker-compose up -d

# Run integration tests
mvn verify
```

### Skip Tests

```bash
mvn clean package -DskipTests
```

## Packaging

### Create Shaded JAR

Already done by default:
```bash
mvn clean package
```

Output: `sentinel-cli/target/sentinel-cli-1.0.0-SNAPSHOT-shaded.jar`

### Create Distribution

```bash
mvn clean package
tar -czf sentinel-1.0.0.tar.gz \
  sentinel-cli/target/sentinel-cli-*-shaded.jar \
  README.md \
  QUICKSTART.md \
  examples/ \
  docker-compose.yml
```

## Next Steps

1. **Read Documentation**
   - [README.md](README.md)
   - [QUICKSTART.md](QUICKSTART.md)
   - [docs/PLUGIN_DEVELOPMENT.md](docs/PLUGIN_DEVELOPMENT.md)

2. **Try Examples**
   - Scan test lab targets
   - Create consent documents
   - Explore plugins

3. **Develop Plugins**
   - Follow plugin development guide
   - Use built-in plugins as examples
   - Test with test lab

4. **Deploy**
   - See [DEPLOYMENT.md](DEPLOYMENT.md)
   - Configure for your environment
   - Set up monitoring

## Support

- **Issues**: GitHub Issues
- **Documentation**: docs/
- **Examples**: examples/
- **Community**: GitHub Discussions
