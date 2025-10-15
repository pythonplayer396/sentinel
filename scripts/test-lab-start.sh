#!/bin/bash
set -e

echo "Starting Sentinel Test Lab..."
echo "=============================="

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "Error: Docker is not installed"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "Error: Docker Compose is not installed"
    exit 1
fi

# Start services
echo "Starting OWASP test applications..."
docker-compose up -d

echo ""
echo "Waiting for services to start..."
sleep 10

# Check status
echo ""
echo "Service Status:"
echo "==============="
docker-compose ps

echo ""
echo "Test Targets:"
echo "============="
echo "  Juice Shop: http://localhost:3000"
echo "  WebGoat:    http://localhost:8080/WebGoat"
echo "  DVWA:       http://localhost:8081"
echo ""
echo "To stop: docker-compose down"
