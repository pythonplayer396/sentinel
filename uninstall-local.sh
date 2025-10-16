#!/bin/bash
# Uninstall local Sentinel installation

set -e

echo "=========================================="
echo "  Sentinel - Local Uninstallation"
echo "=========================================="
echo ""

# Remove files
echo "Removing Sentinel files..."

rm -f ~/.local/bin/sentinel
rm -f ~/.local/bin/sentinel-gui
rm -rf ~/.local/share/sentinel
rm -f ~/.local/share/applications/sentinel.desktop
rm -f ~/.local/share/icons/hicolor/scalable/apps/sentinel.svg
rm -f ~/.local/share/icons/hicolor/256x256/apps/sentinel.png
rm -f ~/.local/share/icons/hicolor/128x128/apps/sentinel.png
rm -f ~/.local/share/icons/hicolor/64x64/apps/sentinel.png
rm -f ~/.local/share/icons/hicolor/48x48/apps/sentinel.png
rm -f ~/.local/share/icons/hicolor/32x32/apps/sentinel.png

echo "✓ Files removed"
echo ""

# Update caches
if command -v gtk-update-icon-cache &> /dev/null; then
    gtk-update-icon-cache ~/.local/share/icons/hicolor/ 2>/dev/null || true
fi

if command -v update-desktop-database &> /dev/null; then
    update-desktop-database ~/.local/share/applications/ 2>/dev/null || true
fi

echo "=========================================="
echo "  Uninstallation Complete!"
echo "=========================================="
echo ""
echo "Sentinel has been removed from your system."
echo ""
echo "Note: User data in ~/.sentinel/ was not removed."
echo "To remove all data, run:"
echo "  rm -rf ~/.sentinel"
echo ""
