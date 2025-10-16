# Sentinel - Kali Linux Integration Guide

This guide explains how to build and install Sentinel as a native Kali Linux tool with proper menu integration and logo.

## 📦 Quick Installation

### Option 1: Build Debian Package (Recommended for Kali)

```bash
# Install build dependencies
sudo apt install dpkg-dev debhelper devscripts maven openjdk-17-jdk librsvg2-bin

# Build the package
chmod +x build-kali-package.sh
./build-kali-package.sh

# Install the package
sudo dpkg -i ../sentinel_*.deb
sudo apt-get install -f  # Fix any dependency issues
```

### Option 2: Local Installation (Development)

```bash
# Install to ~/.local/bin (no root required)
chmod +x install-local.sh
./install-local.sh

# Add to PATH if needed
echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

## 🎨 Kali Menu Integration

After installation, Sentinel will appear in:

**Applications → 03-Web Application Analysis → Sentinel Scanner**

The menu entry includes:
- **Name**: Sentinel Scanner
- **Icon**: Custom shield with eye logo (cyan/teal theme)
- **Category**: Security / Web Application Analysis
- **Launch**: Opens Sentinel GUI (when available) or CLI

## 🖼️ Logo and Icons

Sentinel includes a custom logo featuring:
- **Shield**: Protection and security
- **Eye**: Vigilance and monitoring (sentinel watching)
- **Scanning lines**: Active analysis
- **Lock**: Consent and authorization
- **Color scheme**: Cyan/teal on dark background (Kali-style)

### Icon Locations

After installation:
- SVG: `/usr/share/icons/hicolor/scalable/apps/sentinel.svg`
- PNG (256x256): `/usr/share/icons/hicolor/256x256/apps/sentinel.png`
- Pixmap: `/usr/share/pixmaps/sentinel.png`

### Regenerating Icons

```bash
cd debian/icons
chmod +x generate-icons.sh
./generate-icons.sh
```

## 📁 File Structure

```
/usr/
├── bin/
│   ├── sentinel           # CLI wrapper script
│   └── sentinel-gui       # GUI wrapper script
├── share/
│   ├── sentinel/
│   │   └── sentinel-cli.jar
│   ├── applications/
│   │   └── sentinel.desktop
│   ├── icons/hicolor/
│   │   ├── scalable/apps/sentinel.svg
│   │   └── 256x256/apps/sentinel.png
│   ├── pixmaps/
│   │   └── sentinel.png
│   ├── doc/sentinel/
│   │   ├── README.md
│   │   └── examples/
│   └── man/man1/
│       └── sentinel.1.gz
```

## 🚀 Usage

### Command Line

```bash
# Show help
sentinel --help

# Create consent document
sentinel consent create \
  --target https://example.com \
  --org "My Organization" \
  --authorized-by "Your Name" \
  --email your@email.com \
  --file consent.json

# Run a scan
sentinel scan \
  --target https://example.com \
  --consent consent.json \
  --depth 10 \
  --output report.json

# Scan localhost (no consent needed)
sentinel scan \
  --target http://localhost:3000 \
  --depth 5 \
  --safety ACTIVE

# List plugins
sentinel plugin list
```

### GUI (Future)

```bash
# Launch GUI
sentinel-gui

# Or from applications menu:
# Applications → Web Application Analysis → Sentinel Scanner
```

## 🔧 Building from Source

### Prerequisites

```bash
sudo apt install -y \
  openjdk-17-jdk \
  maven \
  dpkg-dev \
  debhelper \
  devscripts \
  librsvg2-bin
```

### Build Steps

1. **Clone repository**
   ```bash
   git clone https://github.com/yourusername/sentinel.git
   cd sentinel
   ```

2. **Generate icons**
   ```bash
   cd debian/icons
   chmod +x generate-icons.sh
   ./generate-icons.sh
   cd ../..
   ```

3. **Build package**
   ```bash
   chmod +x build-kali-package.sh
   ./build-kali-package.sh
   ```

4. **Install**
   ```bash
   sudo dpkg -i ../sentinel_*.deb
   ```

## 📋 Package Information

### Debian Package Details

- **Package name**: `sentinel`
- **Version**: `1.0.0-1kali1`
- **Architecture**: `all` (Java-based)
- **Section**: `utils`
- **Priority**: `optional`
- **Dependencies**: `openjdk-17-jre | openjdk-17-jdk`

### Package Contents

```bash
# List files in package
dpkg -L sentinel

# Show package info
dpkg -s sentinel

# Verify package
dpkg -V sentinel
```

## 🗑️ Uninstallation

### Remove Debian Package

```bash
# Remove package (keep config)
sudo apt remove sentinel

# Remove package and config
sudo apt purge sentinel

# Remove with dependencies
sudo apt autoremove sentinel
```

### Remove Local Installation

```bash
chmod +x uninstall-local.sh
./uninstall-local.sh
```

## 🐛 Troubleshooting

### Icon Not Showing

```bash
# Update icon cache
sudo gtk-update-icon-cache /usr/share/icons/hicolor/

# Update desktop database
sudo update-desktop-database /usr/share/applications/
```

### Menu Entry Not Appearing

```bash
# Verify desktop file
desktop-file-validate /usr/share/applications/sentinel.desktop

# Refresh menu (XFCE)
xfce4-panel --restart

# Refresh menu (GNOME)
killall gnome-shell
```

### Java Version Issues

```bash
# Check Java version
java -version

# Should be 17 or higher
# If not, install:
sudo apt install openjdk-17-jdk

# Set default Java version
sudo update-alternatives --config java
```

### Build Failures

```bash
# Clean build
mvn clean
rm -rf debian/sentinel

# Rebuild
./build-kali-package.sh

# Check build log
less ../sentinel_*.buildinfo
```

## 📚 Additional Resources

- **Documentation**: `/usr/share/doc/sentinel/`
- **Examples**: `/usr/share/doc/sentinel/examples/`
- **Man page**: `man sentinel`
- **Source**: `https://github.com/yourusername/sentinel`

## 🤝 Contributing to Kali

To submit Sentinel to the official Kali Linux repository:

1. **Fork Kali packages repository**
   ```bash
   git clone https://gitlab.com/kalilinux/packages/sentinel.git
   ```

2. **Add Debian packaging**
   - Copy `debian/` directory
   - Ensure compliance with Debian Policy
   - Test on clean Kali installation

3. **Submit merge request**
   - Follow Kali contribution guidelines
   - Include package description
   - Provide testing evidence

4. **Kali team review**
   - Address feedback
   - Update as needed
   - Wait for approval

## 📄 License

Sentinel is released under the MIT License. See [LICENSE](LICENSE) for details.

## ⚠️ Ethical Use

**IMPORTANT**: Sentinel is designed for authorized security testing only.

- ✅ Use only on systems you own or have explicit permission to test
- ✅ Always obtain and document consent before scanning
- ✅ Respect rate limits and robots.txt
- ✅ Report findings responsibly
- ❌ Never use for unauthorized access or malicious purposes

---

**Built with ❤️ for the Kali Linux community**
