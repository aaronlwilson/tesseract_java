#!/usr/bin/env bash
set -euo pipefail

# Builds the fat jar and ships it to a Tesseract host (Raspberry Pi, or any Linux box).
#
# Nothing is cross-compiled and nothing compiles on the target. Java bytecode is
# platform-neutral, and build.gradle pulls prebuilt natives for every target including
# linux-arm64 — so the jar built here runs as-is on the Pi, which needs only a JRE 21+
# (build.gradle targets bytecode 21; an older JVM fails with UnsupportedClassVersionError).
#
# Usage:
#   ./deploy/deploy.sh pi@tesseract-pi.local
#   ./deploy/deploy.sh pi@tesseract-pi.local --no-build      # ship the existing jar
#   REMOTE_DIR=/opt/tesseract ./deploy/deploy.sh pi@raspberrypi.local

script_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
repo_root="$( cd "${script_path}/.." && pwd )"

remote_dir="${REMOTE_DIR:-tesseract}"
do_build=true
target=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-build) do_build=false; shift;;
    -h|--help)  sed -n '3,15p' "${BASH_SOURCE[0]}"; exit 0;;
    -*)         echo "Unknown option: $1" >&2; exit 1;;
    *)          target="$1"; shift;;
  esac
done

if [[ -z "$target" ]]; then
  echo "Usage: $0 <user@host> [--no-build]" >&2
  echo "Example: $0 pi@tesseract-pi.local" >&2
  exit 1
fi

jar="${repo_root}/build/libs/TesseractFatJar.jar"

if [[ $do_build == true ]]; then
  echo "==> Building fat jar"
  (cd "$repo_root" && ./gradlew fatJar)
fi

if [[ ! -f "$jar" ]]; then
  echo "ERROR: $jar not found. Drop --no-build, or run ./gradlew fatJar first." >&2
  exit 1
fi

echo "==> Creating ${target}:${remote_dir}"
ssh "$target" "mkdir -p '${remote_dir}/config'"

echo "==> Copying jar ($(du -h "$jar" | cut -f1))"
scp "$jar" "${target}:${remote_dir}/"

echo "==> Copying start script"
scp "${script_path}/start-backend.sh" "${target}:${remote_dir}/"
ssh "$target" "chmod +x '${remote_dir}/start-backend.sh'"

# Config is only seeded when absent, so host-specific edits on the target survive a
# redeploy. Delete it there (or edit it in place) to change the deployed configuration.
if ssh "$target" "test -f '${remote_dir}/config/tesseract-config.yml'"; then
  echo "==> Config already present on target, leaving it alone"
else
  echo "==> Seeding config/tesseract-config.yml"
  scp "${repo_root}/config/tesseract-config.yml" "${target}:${remote_dir}/config/"
fi

# Note: data/ is deliberately not shipped. The app persists playlists and scenes under
# ./data/ at runtime, and overwriting that would discard edits made on the target.

cat <<EOF

==> Done. On ${target}:

    cd ${remote_dir}
    ./start-backend.sh                 # headless (production)
    ./start-backend.sh --headed        # with the 3D visualization, needs a display

  First time only — install a JRE if 'java -version' is missing or below 21:

    apt-cache policy openjdk-21-jre    # check availability first
    sudo apt install openjdk-21-jre    # Debian 13/trixie-based Pi OS has it in main
                                       # Bookworm ships 17; use backports or Temurin

  To run it on boot, see ${script_path}/README.md (systemd unit included).
EOF
