#!/bin/bash
# Generate PNG icons from SVG

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Check for required tools
if command -v rsvg-convert &> /dev/null; then
    CONVERTER="rsvg-convert"
    echo "Using rsvg-convert..."
elif command -v inkscape &> /dev/null; then
    CONVERTER="inkscape"
    echo "Using inkscape..."
elif command -v convert &> /dev/null; then
    CONVERTER="imagemagick"
    echo "Using ImageMagick..."
else
    echo "Error: No suitable SVG converter found."
    echo "Please install one of: librsvg2-bin, inkscape, or imagemagick"
    echo ""
    echo "  sudo apt install librsvg2-bin"
    exit 1
fi

# Generate icons
SIZES=(256 128 64 48 32)

for size in "${SIZES[@]}"; do
    echo "Generating ${size}x${size} icon..."
    
    case $CONVERTER in
        rsvg-convert)
            rsvg-convert -w $size -h $size sentinel.svg -o sentinel-${size}.png
            ;;
        inkscape)
            inkscape sentinel.svg --export-type=png --export-filename=sentinel-${size}.png -w $size -h $size
            ;;
        imagemagick)
            convert -background none sentinel.svg -resize ${size}x${size} sentinel-${size}.png
            ;;
    esac
done

echo ""
echo "✓ Icon generation complete!"
echo ""
echo "Generated files:"
ls -lh sentinel-*.png
