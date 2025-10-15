#!/bin/bash
set -e

JAR="sentinel-cli/target/sentinel-cli-1.0.0-SNAPSHOT-shaded.jar"

if [ ! -f "$JAR" ]; then
    echo "Error: Sentinel CLI JAR not found. Run ./scripts/build.sh first"
    exit 1
fi

echo "Running example scan on Juice Shop..."
echo "======================================"

java -jar "$JAR" scan \
    --target http://localhost:3000 \
    --depth 5 \
    --rate 5.0 \
    --safety PASSIVE

echo ""
echo "Scan completed!"
