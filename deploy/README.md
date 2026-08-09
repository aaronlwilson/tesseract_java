# Deployment

Deploying Tesseract to a Raspberry Pi (or any Linux host) is two independent pieces:

| Piece | Where it runs | Why |
|---|---|---|
| **Backend** (this repo) | directly on the host | UDP output broadcasts to `255.255.255.255`, which does not traverse Docker's bridge network |
| **WebUI** (`tesseract_react`) | Docker container | `aaronlennwilson/tesseract-ui`, nginx serving static files |

## Nothing is cross-compiled

Java bytecode is platform-neutral, and `build.gradle` pulls prebuilt natives for every
target — including `linux-arm64`. The jar built on a Mac runs unmodified on the Pi.

The Pi needs a **JRE 21+, not a JDK and not a dev environment**. Nothing compiles there.
`build.gradle` targets bytecode 21, so an older JVM fails with
`UnsupportedClassVersionError`.

```bash
apt-cache policy openjdk-21-jre    # check what's available first
sudo apt install openjdk-21-jre
```

Raspberry Pi OS based on **Debian 13 (trixie)** has OpenJDK 21 in main. **Bookworm ships
17** — use `bookworm-backports`, or an Eclipse Temurin 21 `linux/aarch64` tarball.

## Deploy the backend

```bash
./deploy/deploy.sh pi@tesseract-pi.local
```

Builds the fat jar, copies it plus `start-backend.sh` to `~/tesseract` on the target, and
seeds `config/tesseract-config.yml` **only if absent** so host-specific edits survive a
redeploy. `data/` is never shipped — playlists and scenes saved on the target would be
overwritten.

Options: `--no-build` to ship an existing jar, `REMOTE_DIR=/opt/tesseract` to change the
destination.

### Without SSH: publish a release and pull it down

When the target isn't reachable over SSH, attach the jar to a **GitHub release**. Release
assets allow up to 2 GB and live outside git history — the jar is ~116 MB, well past the
100 MB ceiling git enforces, so it can neither be committed nor served from GitHub Pages.

```bash
./gradlew fatJar
gh release create v1.0.0 build/libs/TesseractFatJar.jar \
  --title "Tesseract v1.0.0" --notes "Headless-capable build for Raspberry Pi"
```

Then on the target — a direct URL, no download interstitial to work around:

```bash
mkdir -p ~/tesseract/config && cd ~/tesseract
wget https://github.com/aaronlwilson/tesseract_java/releases/latest/download/TesseractFatJar.jar
```

Adding assets to an existing release later: `gh release upload v1.0.0 build/libs/TesseractFatJar.jar --clobber`.
Copy `start-backend.sh` and a `config/tesseract-config.yml` alongside it by hand.

Then on the host:

```bash
cd tesseract
./start-backend.sh              # headless — production
./start-backend.sh --headed     # 3D visualization, needs a display
```

### Bring up new hardware headed first

The fat jar carries `linux/arm64` LWJGL/GLFW natives, so the visualization works on a Pi.
Running headed first is worth the detour: seeing clips render proves the render path
independently of whether the LED wiring is correct, so a dark array later means "UDP or
wiring", not "something in the entire stack".

Check the GPU before involving Java — one command, and it tells you whether OpenGL is even
a concern. A Pi 4 (VideoCore VI / Mesa V3D) should report desktop **GL 2.1**:

```bash
sudo apt install mesa-utils && glxinfo -B | grep -i "OpenGL version"
```

Two things commonly get in the way:

- **`DISPLAY` is unset over SSH.** `TesseractLauncher` auto-selects headless when there is
  no display on a non-macOS host, so headed mode over plain SSH silently runs headless.
  Run from the Pi's own desktop session, or `export DISPLAY=:0`.
- **Wayland.** Pi OS Bookworm defaults to it while LWJGL 3.3.3 ships an X11 GLFW. XWayland
  usually bridges this; `raspi-config` can force an X11 session if not.

## Run on boot

```bash
sudo cp deploy/tesseract-backend.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now tesseract-backend
journalctl -u tesseract-backend -f
```

Edit `User` and the paths in the unit if you deployed somewhere other than
`/home/pi/tesseract`. `WorkingDirectory` matters — the app resolves
`config/tesseract-config.yml` and `data/` relative to it, not to the jar.

## Deploy the WebUI

```bash
docker pull aaronlennwilson/tesseract-ui:latest
docker run -d --restart unless-stopped -p 80:80 aaronlennwilson/tesseract-ui:latest
```

No backend address is needed: the UI dials whichever host served the page, which is correct
whenever the container and backend share a machine. A phone opening `http://tesseract-pi.local/`
reaches the Pi's backend automatically, with no per-device setup.

Set `-e DEFAULT_SERVER_ADDR=other-host.local` only when the backend lives elsewhere. The
Settings panel overrides both, per browser; clearing that field returns to auto-detection.

## Configuration

`config/tesseract-config.yml` on the target, or environment variables named
`TESSERACT_<OPTION>` (camelCase becomes underscores), which take precedence:

```bash
TESSERACT_STAGE_TYPE=SCARED \
TESSERACT_INITIAL_PLAYLIST="All Videos" \
TESSERACT_INITIAL_PLAY_STATE=playing \
  ./start-backend.sh
```

`stageType` accepts `CUBOTRON`, `TESSERACT`, `TESSERACT_WALL`, `DRACO`, `SCARED`.
