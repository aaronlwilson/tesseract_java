# Deployment

Deploying Tesseract to a Raspberry Pi (or any Linux host) is two independent pieces:

| Piece | Where it runs | Why |
|---|---|---|
| **Backend** (this repo) | directly on the host | UDP output broadcasts to `255.255.255.255`, which does not traverse Docker's bridge network |
| **WebUI** (`tesseract_react`) | Docker container | `aaronlennwilson/tesseract-ui`, nginx serving static files |

## The target must be 64-bit

Check this before anything else. 32-bit ARM (armhf) is a dead end for this project, not
merely a slower path:

| Component | armhf (32-bit) | arm64 (64-bit) |
|---|---|---|
| LibGDX / LWJGL graphics | works — `libgdxarm.so` and LWJGL `linux/arm32` are bundled | works |
| **Video clips** (JavaCV/FFmpeg) | **impossible** — `linux-armhf` dropped in JavaCV 1.5.12+, see `build.gradle:70` | works |
| WebUI container | won't run — `aaronlennwilson/tesseract-ui` is `linux/arm64` only | works |

The video row is upstream removal, not a missing dependency line. There is no version of
this project that decodes video on armhf. Because graphics *do* work there, a 32-bit host
starts up and renders — the failure surfaces later, as clips that never appear.

```bash
uname -m                    # aarch64 = good; armv7l = 32-bit
dpkg --print-architecture   # arm64 = good; armhf = 32-bit
```

`/etc/os-release` is also a giveaway: `Raspbian GNU/Linux` is always the 32-bit port, while
the 64-bit build reports `Debian GNU/Linux`. To fix, reimage with Raspberry Pi Imager and
select **Raspberry Pi OS (64-bit)** — supported on Pi 3 and newer. Take the full desktop
image rather than Lite if you plan to run headed during bring-up.

## Nothing is cross-compiled

Java bytecode is platform-neutral, and `build.gradle` pulls prebuilt natives for every
64-bit target — including `linux-arm64`. The jar built on a Mac runs unmodified on the Pi.

The Pi needs a **JRE 21+, not a JDK and not a dev environment**. Nothing compiles there.
`build.gradle` targets bytecode 21, so an older JVM fails with
`UnsupportedClassVersionError`.

```bash
apt-cache policy openjdk-21-jre    # check what's available first
sudo apt install openjdk-21-jre
```

What each Raspberry Pi OS generation ships:

| Base | OpenJDK in main | What to do |
|---|---|---|
| Debian 13 (trixie) | 21 | `sudo apt install openjdk-21-jre` |
| Debian 12 (bookworm) | 17 | `bookworm-backports`, or an Adoptium Temurin 21 `linux/aarch64` build |
| Debian 10 (buster) and older | 11 | EOL, repos archived, 32-bit only — reimage |

Prefer the full `openjdk-21-jre` over `openjdk-21-jre-headless`. The two senses of
"headless" are unrelated: Debian's `-headless` package omits AWT, while this app's
`--headless` mode is a libGDX backend choice. `-headless` is sufficient for production, but
the full package keeps the headed bring-up test below available.

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
a concern. Measured on a Pi 4 (VideoCore VI / Mesa V3D) under Trixie: **OpenGL 3.1, direct
rendering: Yes**. Anything reporting llvmpipe or software rendering means the GPU is not
being used and performance will be unusable.

```bash
sudo apt install mesa-utils
DISPLAY=:0 XDG_RUNTIME_DIR=/run/user/1000 glxinfo -B | grep -iE "OpenGL version|renderer|direct"
```

Measured throughput on that hardware: CUBOTRON's 27,000 nodes render at 30 fps with room to
spare, and the real stages are far smaller — SCARED ~6,000, TESSERACT ~7,000.

Two things commonly get in the way:

- **`DISPLAY` is unset over SSH.** With neither flag given, the launcher picks the mode by
  whether `DISPLAY` is set — and it never is in a plain SSH session, so an unqualified run
  goes headless. `--headed` overrides that detection outright. You still need a display for
  it to render on, so pair it with `export DISPLAY=:0` to target the Pi's own screen, or
  run from the Pi's desktop session. Forcing `--headed` with no reachable display warns and
  then fails at window creation, which is the intended loud failure.
- **Wayland.** Current Pi OS runs a Wayland session (compositor `labwc`) while LWJGL ships
  an X11 GLFW, so rendering goes through XWayland. This works — confirmed on Trixie with
  hardware acceleration intact — but an SSH session needs both variables, not just
  `DISPLAY`:

  ```bash
  DISPLAY=:0 XDG_RUNTIME_DIR=/run/user/1000 ./start-backend.sh --headed
  ```

  Check that XWayland is actually up with `ls /tmp/.X11-unix/` — an `X0` socket means
  `DISPLAY=:0` is valid. `raspi-config` can force a true X11 session if needed.

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
