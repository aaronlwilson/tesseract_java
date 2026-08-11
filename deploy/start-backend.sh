#!/usr/bin/env bash
set -euo pipefail

# Runs the Tesseract backend on a deployment host. Lives next to TesseractFatJar.jar,
# placed there by deploy/deploy.sh.
#
# Headless is the default and the production mode: no window, no OpenGL, no X server —
# it uses libGDX's headless backend and outputs only UDP to the LED controllers.
#
# Pass --headed to get the 3D visualization instead, which is worth doing during bring-up
# on new hardware: seeing clips render confirms the render path independently of whether
# the LED wiring is right.
#
#   ./start-backend.sh
#   ./start-backend.sh --headed
#
# Configuration comes from ./config/tesseract-config.yml. Any option can be overridden with
# an environment variable named TESSERACT_<OPTION> (camelCase becomes underscores), which
# takes precedence over the file:
#
#   TESSERACT_STAGE_TYPE=SCARED ./start-backend.sh
#   TESSERACT_INITIAL_PLAYLIST="All Videos" TESSERACT_INITIAL_PLAY_STATE=playing ./start-backend.sh

# The app resolves config/tesseract-config.yml and data/ relative to the working directory,
# so always run from the script's own directory regardless of where it was invoked.
cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Pass the mode through explicitly rather than by omission. The launcher falls back to
# display auto-detection only when neither flag is given, and DISPLAY is unset in every
# plain SSH session — so omitting the flag here would silently run headless.
mode_flag="--headless"
for arg in "$@"; do
  case "$arg" in
    --headed)   mode_flag="--headed"; shift;;
    --headless) mode_flag="--headless"; shift;;
  esac
done

if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: java not found. Install a JRE 21+ (see deploy/README.md)." >&2
  exit 1
fi

# build.gradle targets bytecode 21, so an older JVM dies with UnsupportedClassVersionError.
# Catch it here with a clear message instead.
java_major="$(java -version 2>&1 | head -1 | sed -E 's/.*version "([0-9]+).*/\1/')"
if [[ "$java_major" =~ ^[0-9]+$ ]] && (( java_major < 21 )); then
  echo "ERROR: Java ${java_major} found, but 21+ is required." >&2
  echo "       apt-cache policy openjdk-21-jre" >&2
  exit 1
fi

# -XstartOnFirstThread is a macOS-only requirement for GLFW and must not be passed on Linux.
if [[ "$(uname -s)" == "Darwin" && "$mode_flag" == "--headed" ]]; then
  exec java -XstartOnFirstThread -jar TesseractFatJar.jar --headed
fi

exec java -jar TesseractFatJar.jar "$mode_flag"
