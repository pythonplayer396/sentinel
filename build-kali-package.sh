#!/bin/bash
# Build Debian package for Kali Linux

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=========================================="
echo "  Sentinel - Kali Linux Package Builder"
echo "=========================================="
echo ""

# Check for required tools
echo "[1/6] Checking dependencies..."
MISSING_DEPS=()

if ! command -v dpkg-buildpackage &> /dev/null; then
    MISSING_DEPS+=("dpkg-dev")
fi

if ! command -v mvn &> /dev/null; then
    MISSING_DEPS+=("maven")
fi

if ! command -v java &> /dev/null; then
    MISSING_DEPS+=("openjdk-17-jdk")
fi

if ! command -v debuild &> /dev/null; then
    MISSING_DEPS+=("devscripts")
fi

if ! command -v dh &> /dev/null; then
    MISSING_DEPS+=("debhelper")
fi

if [ ${#MISSING_DEPS[@]} -ne 0 ]; then
    echo "Error: Missing required dependencies:"
    printf '  - %s\n' "${MISSING_DEPS[@]}"
    echo ""
    echo "Install with:"
    echo "  sudo apt install ${MISSING_DEPS[*]}"
    exit 1
fi

echo "✓ All dependencies found"
echo ""

# Generate icons
echo "[2/6] Generating icons..."
cd debian/icons
if [ ! -f sentinel-256.png ]; then
    chmod +x generate-icons.sh
    ./generate-icons.sh
else
    echo "✓ Icons already generated"
fi
cd ../..
echo ""

# Clean previous builds
echo "[3/6] Cleaning previous builds..."
mvn clean > /dev/null 2>&1 || true
rm -rf debian/sentinel
rm -f ../sentinel_*.deb ../sentinel_*.changes ../sentinel_*.buildinfo
echo "✓ Clean complete"
echo ""

# Build with Maven
echo "[4/6] Building with Maven..."
mvn package -DskipTests
echo "✓ Maven build complete"
echo ""

# Build Debian package
echo "[5/6] Building Debian package..."
dpkg-buildpackage -us -uc -b
echo "✓ Debian package built"
echo ""

# Show results
echo "[6/6] Package build complete!"
echo ""
echo "=========================================="
echo "  Build Results"
echo "=========================================="
echo ""

if [ -f ../sentinel_*.deb ]; then
    DEB_FILE=$(ls -1 ../sentinel_*.deb | head -1)
    echo "Package: $(basename "$DEB_FILE")"
    echo "Size: $(du -h "$DEB_FILE" | cut -f1)"
    echo "Location: $DEB_FILE"
    echo ""
    echo "Package info:"
    dpkg-deb -I "$DEB_FILE" | grep -E "Package:|Version:|Architecture:|Description:" | sed 's/^/  /'
    echo ""
    echo "To install:"
    echo "  sudo dpkg -i $DEB_FILE"
    echo "  sudo apt-get install -f  # Fix dependencies if needed"
    echo ""
    echo "To install system-wide:"
    echo "  sudo cp $DEB_FILE /var/cache/apt/archives/"
    echo "  sudo dpkg -i /var/cache/apt/archives/$(basename "$DEB_FILE")"
else
    echo "Error: Package file not found!"
    exit 1
fi
