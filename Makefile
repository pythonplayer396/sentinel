.PHONY: help build test clean run-scan start-lab stop-lab plugins

help:
	@echo "Sentinel Scanner - Makefile"
	@echo "============================"
	@echo ""
	@echo "Available targets:"
	@echo "  make build       - Build the project"
	@echo "  make test        - Run tests"
	@echo "  make clean       - Clean build artifacts"
	@echo "  make start-lab   - Start Docker test lab"
	@echo "  make stop-lab    - Stop Docker test lab"
	@echo "  make run-scan    - Run example scan"
	@echo "  make plugins     - List available plugins"
	@echo ""

build:
	@echo "Building Sentinel..."
	mvn clean package -DskipTests

test:
	@echo "Running tests..."
	mvn test

clean:
	@echo "Cleaning..."
	mvn clean
	docker-compose down -v 2>/dev/null || true

start-lab:
	@echo "Starting test lab..."
	docker-compose up -d
	@echo "Waiting for services..."
	@sleep 10
	@echo "Test lab ready!"
	@echo "  Juice Shop: http://localhost:3000"
	@echo "  WebGoat:    http://localhost:8080/WebGoat"
	@echo "  DVWA:       http://localhost:8081"

stop-lab:
	@echo "Stopping test lab..."
	docker-compose down

run-scan: build
	@echo "Running example scan..."
	java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar scan \
		--target http://localhost:3000 \
		--depth 5 \
		--rate 5.0

plugins: build
	@echo "Available plugins:"
	java -jar sentinel-cli/target/sentinel-cli-*-shaded.jar plugin list

install: build
	@echo "Installing Sentinel CLI..."
	@mkdir -p ~/.local/bin
	@cp sentinel-cli/target/sentinel-cli-*-shaded.jar ~/.local/bin/sentinel.jar
	@echo '#!/bin/bash' > ~/.local/bin/sentinel
	@echo 'java -jar ~/.local/bin/sentinel.jar "$$@"' >> ~/.local/bin/sentinel
	@chmod +x ~/.local/bin/sentinel
	@echo "Installed! Add ~/.local/bin to PATH if needed"
	@echo "Usage: sentinel --help"
