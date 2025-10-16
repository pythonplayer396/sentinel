# Sentinel Icons

This directory contains icon files for Sentinel in various formats.

## Files

- **sentinel.svg** - Scalable vector graphic (primary icon)
- **sentinel-256.png** - 256x256 PNG icon (to be generated)
- **sentinel-128.png** - 128x128 PNG icon (to be generated)
- **sentinel-64.png** - 64x64 PNG icon (to be generated)
- **sentinel-48.png** - 48x48 PNG icon (to be generated)
- **sentinel-32.png** - 32x32 PNG icon (to be generated)

## Generating PNG Icons from SVG

To generate PNG icons from the SVG file, use ImageMagick or Inkscape:

### Using ImageMagick (convert/magick):
```bash
# Install ImageMagick if needed
sudo apt install imagemagick

# Generate PNG icons
convert -background none sentinel.svg -resize 256x256 sentinel-256.png
convert -background none sentinel.svg -resize 128x128 sentinel-128.png
convert -background none sentinel.svg -resize 64x64 sentinel-64.png
convert -background none sentinel.svg -resize 48x48 sentinel-48.png
convert -background none sentinel.svg -resize 32x32 sentinel-32.png
```

### Using Inkscape:
```bash
# Install Inkscape if needed
sudo apt install inkscape

# Generate PNG icons
inkscape sentinel.svg --export-type=png --export-filename=sentinel-256.png -w 256 -h 256
inkscape sentinel.svg --export-type=png --export-filename=sentinel-128.png -w 128 -h 128
inkscape sentinel.svg --export-type=png --export-filename=sentinel-64.png -w 64 -h 64
inkscape sentinel.svg --export-type=png --export-filename=sentinel-48.png -w 48 -h 48
inkscape sentinel.svg --export-type=png --export-filename=sentinel-32.png -w 32 -h 32
```

### Using rsvg-convert (recommended for Debian packaging):
```bash
# Install librsvg2-bin if needed
sudo apt install librsvg2-bin

# Generate PNG icons
rsvg-convert -w 256 -h 256 sentinel.svg -o sentinel-256.png
rsvg-convert -w 128 -h 128 sentinel.svg -o sentinel-128.png
rsvg-convert -w 64 -h 64 sentinel.svg -o sentinel-64.png
rsvg-convert -w 48 -h 48 sentinel.svg -o sentinel-48.png
rsvg-convert -w 32 -h 32 sentinel.svg -o sentinel-32.png
```

## Icon Design

The Sentinel icon features:
- **Shield**: Represents protection and security
- **Eye**: Symbolizes vigilance and monitoring (sentinel watching)
- **Scanning lines**: Indicates active scanning/analysis
- **Lock**: Represents consent and authorization
- **Corner brackets**: UI element suggesting scanner interface
- **Color scheme**: Cyan/teal (#00d9ff, #00ff88) on dark background (#1a1a2e)

The design follows Kali Linux tool aesthetic with modern, tech-focused styling.
