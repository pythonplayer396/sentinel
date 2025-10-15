#!/bin/bash
set -e

echo "Building Sentinel Scanner..."
echo "=============================="

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "Error: Java 17 or later is required"
    exit 1
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed"
    exit 1
fi

# Clean and build
echo "Running Maven build..."
mvn clean package -DskipTests

echo ""
echo "Build completed successfully!"
echo ""
echo "CLI JAR: sentinel-cli/target/sentinel-cli-1.0.0-SNAPSHOT-shaded.jar"
echo ""
echo "To run:"
echo "  java -jar sentinel-cli/target/sentinel-cli-1.0.0-SNAPSHOT-shaded.jar --help"
