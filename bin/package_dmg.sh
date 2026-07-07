#!/bin/bash
# Packages Tesseract Desktop as a macOS DMG using jpackage (JDK 21+).
# Usage: ./bin/package_dmg.sh [version]
# Output: build/dmg/Tesseract Desktop-<version>.dmg
#
# Optional icon: place a 1024x1024 PNG at pkg/macos/icon.png

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT_DIR"

VERSION="${1:-1.0.0}"
FAT_JAR="build/libs/TesseractFatJar.jar"
STAGING_DIR="build/dmg-staging"
OUTPUT_DIR="build/dmg"
ICON_PNG="pkg/macos/icon.png"
ICON_ICNS="$STAGING_DIR/icon.icns"

if [ ! -f "$FAT_JAR" ]; then
  echo "ERROR: Fat JAR not found at $FAT_JAR — run ./gradlew fatJar first"
  exit 1
fi

# Clean and prepare staging
rm -rf "$STAGING_DIR"
mkdir -p "$STAGING_DIR" "$OUTPUT_DIR"

# Stage the fat JAR
cp "$FAT_JAR" "$STAGING_DIR/"

# Convert PNG icon to .icns if provided
ICON_ARGS=()
if [ -f "$ICON_PNG" ]; then
  echo "Converting $ICON_PNG to .icns..."
  ICONSET="$STAGING_DIR/icon.iconset"
  mkdir -p "$ICONSET"
  sips -z 16 16     "$ICON_PNG" --out "$ICONSET/icon_16x16.png"     > /dev/null
  sips -z 32 32     "$ICON_PNG" --out "$ICONSET/icon_16x16@2x.png"  > /dev/null
  sips -z 32 32     "$ICON_PNG" --out "$ICONSET/icon_32x32.png"     > /dev/null
  sips -z 64 64     "$ICON_PNG" --out "$ICONSET/icon_32x32@2x.png"  > /dev/null
  sips -z 128 128   "$ICON_PNG" --out "$ICONSET/icon_128x128.png"   > /dev/null
  sips -z 256 256   "$ICON_PNG" --out "$ICONSET/icon_128x128@2x.png"> /dev/null
  sips -z 256 256   "$ICON_PNG" --out "$ICONSET/icon_256x256.png"   > /dev/null
  sips -z 512 512   "$ICON_PNG" --out "$ICONSET/icon_256x256@2x.png"> /dev/null
  sips -z 512 512   "$ICON_PNG" --out "$ICONSET/icon_512x512.png"   > /dev/null
  sips -z 1024 1024 "$ICON_PNG" --out "$ICONSET/icon_512x512@2x.png"> /dev/null
  iconutil -c icns "$ICONSET" -o "$ICON_ICNS"
  ICON_ARGS=(--icon "$ICON_ICNS")
fi

# Remove any existing DMG with the same name so jpackage doesn't fail
rm -f "$OUTPUT_DIR/Tesseract Desktop-${VERSION}.dmg"

echo "Running jpackage..."
jpackage \
  --type         dmg \
  --name         "Tesseract Desktop" \
  --app-version  "$VERSION" \
  --input        "$STAGING_DIR" \
  --main-jar     TesseractFatJar.jar \
  --main-class   app.TesseractLauncher \
  --dest         "$OUTPUT_DIR" \
  --java-options -XstartOnFirstThread \
  --java-options -Xmx512m \
  --vendor       "Fryslie" \
  --description  "Kinetic LED and Flame FX control system" \
  "${ICON_ARGS[@]}"

echo ""
echo "✔ DMG ready: $OUTPUT_DIR/Tesseract Desktop-${VERSION}.dmg"