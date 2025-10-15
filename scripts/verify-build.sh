#!/bin/bash
set -e

echo "╔════════════════════════════════════════════════════════════╗"
echo "║  Sentinel Build Verification                               ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check functions
check_java() {
    echo -n "Checking Java version... "
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 17 ]; then
            echo -e "${GREEN}✓${NC} Java $JAVA_VERSION"
            return 0
        else
            echo -e "${RED}✗${NC} Java 17+ required (found $JAVA_VERSION)"
            return 1
        fi
    else
        echo -e "${RED}✗${NC} Java not found"
        return 1
    fi
}

check_maven() {
    echo -n "Checking Maven... "
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version | head -n1 | awk '{print $3}')
        echo -e "${GREEN}✓${NC} Maven $MVN_VERSION"
        return 0
    else
        echo -e "${RED}✗${NC} Maven not found"
        return 1
    fi
}

check_docker() {
    echo -n "Checking Docker... "
    if command -v docker &> /dev/null; then
        echo -e "${GREEN}✓${NC} Docker installed"
        return 0
    else
        echo -e "${YELLOW}⚠${NC} Docker not found (optional)"
        return 0
    fi
}

# Run checks
echo "Prerequisites:"
echo "─────────────────────────────────────────────────────────────"
check_java || exit 1
check_maven || exit 1
check_docker
echo ""

# Build project
echo "Building Project:"
echo "─────────────────────────────────────────────────────────────"
mvn clean package -DskipTests -q
echo -e "${GREEN}✓${NC} Build completed"
echo ""

# Verify artifacts
echo "Verifying Artifacts:"
echo "─────────────────────────────────────────────────────────────"

verify_jar() {
    if [ -f "$1" ]; then
        SIZE=$(du -h "$1" | cut -f1)
        echo -e "${GREEN}✓${NC} $2 ($SIZE)"
        return 0
    else
        echo -e "${RED}✗${NC} $2 not found"
        return 1
    fi
}

verify_jar "sentinel-plugin-api/target/sentinel-plugin-api-1.0.0-SNAPSHOT.jar" "Plugin API"
verify_jar "sentinel-core/target/sentinel-core-1.0.0-SNAPSHOT.jar" "Core Engine"
verify_jar "sentinel-plugins/target/sentinel-plugins-1.0.0-SNAPSHOT.jar" "Built-in Plugins"
verify_jar "sentinel-cli/target/sentinel-cli-1.0.0-SNAPSHOT-shaded.jar" "CLI (Shaded)"

echo ""

# Test CLI
echo "Testing CLI:"
echo "─────────────────────────────────────────────────────────────"
CLI_JAR="sentinel-cli/target/sentinel-cli-1.0.0-SNAPSHOT-shaded.jar"

if java -jar "$CLI_JAR" --version &> /dev/null; then
    echo -e "${GREEN}✓${NC} CLI executable"
else
    echo -e "${RED}✗${NC} CLI not executable"
    exit 1
fi

if java -jar "$CLI_JAR" plugin list &> /dev/null; then
    echo -e "${GREEN}✓${NC} Plugins loadable"
else
    echo -e "${RED}✗${NC} Plugin loading failed"
    exit 1
fi

echo ""

# Summary
echo "Summary:"
echo "─────────────────────────────────────────────────────────────"
echo -e "${GREEN}✓${NC} All checks passed!"
echo ""
echo "Next steps:"
echo "  1. Start test lab:  docker-compose up -d"
echo "  2. Run example:     ./scripts/run-example-scan.sh"
echo "  3. List plugins:    java -jar $CLI_JAR plugin list"
echo ""
echo "Documentation:"
echo "  - README.md"
echo "  - QUICKSTART.md"
echo "  - docs/PLUGIN_DEVELOPMENT.md"
echo ""
