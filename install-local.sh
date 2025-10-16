#!/bin/bash
# Install Sentinel locally without building a Debian package
# Useful for development and testing

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=========================================="
echo "  Sentinel - Local Installation"
echo "=========================================="
echo ""

# Check for Java
if ! command -v java &> /dev/null; then
    echo "Error: Java 17+ is required but not found."
    echo "Install with: sudo apt install openjdk-17-jdk"
    exit 1
fi

# Check for Maven
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is required but not found."
    echo "Install with: sudo apt install maven"
    exit 1
fi

# Build project
echo "[1/4] Building project with Maven..."
mvn clean package -DskipTests
echo "✓ Build complete"
echo ""

# Create local bin directory
echo "[2/4] Setting up local installation..."
mkdir -p ~/.local/bin
mkdir -p ~/.local/share/sentinel
mkdir -p ~/.local/share/applications
mkdir -p ~/.local/share/icons/hicolor/scalable/apps
mkdir -p ~/.local/share/icons/hicolor/256x256/apps

# Copy JAR files
echo "[3/4] Installing files..."
cp sentinel-cli/target/sentinel-cli-*-shaded.jar ~/.local/share/sentinel/sentinel-cli.jar

# Create wrapper scripts
cat > ~/.local/bin/sentinel << 'EOF'
#!/bin/bash
SENTINEL_JAR="$HOME/.local/share/sentinel/sentinel-cli.jar"
exec java -jar "$SENTINEL_JAR" "$@"
EOF

cat > ~/.local/bin/sentinel-gui << 'EOF'
#!/bin/bash
SENTINEL_GUI_JAR="$HOME/.local/share/sentinel/sentinel-gui.jar"
if [ ! -f "$SENTINEL_GUI_JAR" ]; then
    echo "Error: Sentinel GUI is not yet available."
    echo "Use 'sentinel' command for CLI interface."
    exit 1
fi
exec java -jar "$SENTINEL_GUI_JAR" "$@"
EOF

chmod +x ~/.local/bin/sentinel
chmod +x ~/.local/bin/sentinel-gui

# Generate and copy icons
if [ -f debian/icons/sentinel.svg ]; then
    cp debian/icons/sentinel.svg ~/.local/share/icons/hicolor/scalable/apps/
    
    # Generate PNG if possible
    if command -v rsvg-convert &> /dev/null && [ ! -f debian/icons/sentinel-256.png ]; then
        cd debian/icons
        rsvg-convert -w 256 -h 256 sentinel.svg -o sentinel-256.png
        cd ../..
    fi
    
    if [ -f debian/icons/sentinel-256.png ]; then
        cp debian/icons/sentinel-256.png ~/.local/share/icons/hicolor/256x256/apps/sentinel.png
    fi
fi

# Copy desktop file
if [ -f debian/sentinel.desktop ]; then
    cp debian/sentinel.desktop ~/.local/share/applications/
fi

echo "✓ Installation complete"
echo ""

# Update icon cache
if command -v gtk-update-icon-cache &> /dev/null; then
    gtk-update-icon-cache ~/.local/share/icons/hicolor/ 2>/dev/null || true
fi

# Update desktop database
if command -v update-desktop-database &> /dev/null; then
    update-desktop-database ~/.local/share/applications/ 2>/dev/null || true
fi

echo "[4/4] Verifying installation..."
echo ""
echo "=========================================="
echo "  Installation Complete!"
echo "=========================================="
echo ""
echo "Sentinel has been installed to:"
echo "  Binary: ~/.local/bin/sentinel"
echo "  JAR: ~/.local/share/sentinel/sentinel-cli.jar"
echo ""

# Check if ~/.local/bin is in PATH
if [[ ":$PATH:" == *":$HOME/.local/bin:"* ]]; then
    echo "✓ ~/.local/bin is in your PATH"
    echo ""
    echo "You can now run:"
    echo "  sentinel --help"
else
    echo "⚠ Warning: ~/.local/bin is not in your PATH"
    echo ""
    echo "Add it by running:"
    echo "  echo 'export PATH=\"\$HOME/.local/bin:\$PATH\"' >> ~/.bashrc"
    echo "  source ~/.bashrc"
    echo ""
    echo "Or run Sentinel with full path:"
    echo "  ~/.local/bin/sentinel --help"
fi

echo ""
echo "Quick start:"
echo "  sentinel scan --target http://localhost:3000 --depth 5"
echo "  sentinel consent create --target https://example.com --org \"My Org\" --file consent.json"
echo ""
