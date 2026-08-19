# tesseract_java

LibGDX-based control system for interactive Kinetic LED and Flame FX sculptures.

This project was created for IntelliJ IDEA IDE.

## Requirements

**MUST USE JAVA SDK 21+!** The project uses LibGDX, Groovy, and JavaCV which require Java 21.

## Architecture

- **Graphics Engine**: LibGDX 1.14.0 (cross-platform, headless-capable)
- **Video Playback**: JavaCV 1.5.12 with FFmpeg 7.1.1 (cross-platform video support)
- **LED Output**: UDP networking to Teensy/PixelPusher hardware via custom UDP library
- **UI Control**: Websocket server (port 8883) for remote browser-based control
- **Language**: Groovy on Java 21

### Rendering Modes

The application supports two rendering modes:

- **Graphical Mode**: Opens a LibGDX window and renders visualizations
- **Headless Mode** (`--headless`): Runs without a window, outputs only to UDP for LED hardware

With neither flag, the mode is auto-detected from whether `DISPLAY` is set (always graphical on macOS). Pass `--headed` to force the window and skip that detection — necessary over SSH, where `DISPLAY` is unset and auto-detection would otherwise pick headless.

## Local Development (IntelliJ)

You should be able to directly open the project via `idea .` in the repo directory, or by going to File -> Open and choosing the repo directory in IntelliJ.

### Creating the IntelliJ project from scratch

This project uses Gradle. Gradle provides dependency resolution and allows us to easily get the project set up in IntelliJ.

To configure the project from scratch in IntelliJ, follow these steps:

- First install the IntelliJ command line launcher if you haven't already
  - In IntelliJ, go to the 'Tools' menu at the top of the screen and click 'Create Command Line Launcher...'
- Then, delete any existing IntelliJ project files
  - Close IntelliJ
  - Run `rm -rf *.iml .idea` in the repo directory
- Next, run `idea build.gradle`
  - IntelliJ will open and show you a dialog box
  - You can accept the default settings
  - You can check 'Enable auto-import' if you want IntelliJ to automatically watch your project configuration for changes and update itself. This can be very helpful, or annoying depending on the situation.
  - Note: you must have a JVM installed on your machine. Make sure the Gradle JVM box is not empty and points to Java 21+

Once you click OK, the project will be configured. IntelliJ will start downloading and indexing dependencies.

You can run `idea .` in the repo directory to open the project again (you can also open it again in various other ways).

Gradle allows us to easily define and resolve dependencies w/o storing them in the repo, and it will allow us to easily write tests if we ever get around to that. :)
It also allows us to transparently integrate code written in other JVM languages, in addition to a bunch of other potentially useful stuff. Check out some of the features here: https://gradle.org/features/

**IMPORTANT:** The UDP library jar is vendored in `lib/udp/`, so no extra dependency-download step is required — Gradle picks it up automatically.

### Creating the Launcher configuration to launch the application

If you've created the project from scratch, you can create the 'run configuration' easily. This allows you to click the green 'play' (or 'debug') button near the top right of the screen to launch the application, with breakpoints working as normal.

- Open the project
- Open the file `TesseractLauncher.java` (in `src/main/app/`)
- Find the 'main' method
- Click the 'Play' symbol next to the method (the green left-facing triangle) — this launches once and creates a temporary run configuration
- **On macOS, this first launch will crash** with an LWJGL/GLFW error, because IntelliJ's native run configurations don't inherit the `-XstartOnFirstThread` JVM flag that `build.gradle` applies to Gradle-driven runs. Fix it once:
  - Open the 'Run/Debug Configurations' dropdown at the top of the screen and choose 'Edit Configurations...'
  - Select the 'TesseractLauncher' configuration
  - In 'VM options', enter `-XstartOnFirstThread`
  - Click 'OK'
- Click the 'Play' (or 'Debug') button again — it will launch cleanly this time
- Close the application
- In the Run Configurations dropdown (to the left of the play/debug buttons at the top of the screen), choose 'Save TesseractLauncher' if it's still listed as temporary

Now every time you open the project, you can run or debug it by pressing the 'play'/'debug' buttons at the top of the screen. This run configuration lives in `.idea/runConfigurations/`, which is gitignored, so each developer sets it up once locally — it isn't shared through the repo.

## Building a fat jar

A fat jar file is a jar (compiled java application) that contains all necessary dependencies to be run independently.

To build a fat jar, run the command:

```bash
./gradlew fatJar
```

All dependencies (including the vendored UDP library in `lib/udp/`) are resolved automatically — no separate download step is needed.

The jar will be created in the `./build/libs` directory.

To run the resulting jar:

```bash
java -jar ./build/libs/TesseractFatJar.jar
```

To run in headless mode (for production/server deployment):

```bash
java -jar ./build/libs/TesseractFatJar.jar --headless
```

### macOS

On macOS, LibGDX/LWJGL requires the `-XstartOnFirstThread` JVM flag:

```bash
java -XstartOnFirstThread -jar ./build/libs/TesseractFatJar.jar
```

To build a distributable `.dmg` installer (via `jpackage`, requires JDK 21+):

```bash
./gradlew dmg   # runs bin/package_dmg.sh; output in build/dmg/
```

### Linux

The same fat jar runs on Linux (x86_64 and arm64). No `-XstartOnFirstThread` flag is needed. For headless production/server deployment, use the `--headless` flag (see above).

## Configuration

There are 3 ways to define configuration options for the application: Java system properties, environment variables, or a configuration file. This is also the order of precedence: system properties take precedence over environment variables, which take precedence over values defined in the config file.

### System properties

Java system properties are passed to the Java command with the `-D` command line argument. For example:

```bash
java "-DinitialPlaylist=All Videos" -jar TesseractFatJar.jar
```

### Environment variables

You can also use environment variables to configure the application. The environment variable names are prefixed with `TESSERACT_` and converted to all-caps snake case. If you want to set the `initialPlayState` option via environment variable, you would do something like this:

```bash
TESSERACT_INITIAL_PLAY_STATE=playing java -jar TesseractFatJar.jar
```

### Configuration file

Tesseract Java is configured via a YAML configuration file named 'tesseract-config.yml'.

By default, it will look in the location `./config/tesseract-config.yml`. If it doesn't exist, the app will print a warning.

You can pass in a path to your configuration file via the system property `configPath` (e.g., `java -DconfigPath=/path/to/config/tesseract-config.yml -jar TesseractFatJar.jar`). You can also set this via the environment variable `TESSERACT_CONFIG_PATH`, e.g.:

```bash
TESSERACT_CONFIG_PATH=/path/to/config/tesseract-config.yml java -jar TesseractFatJar.jar
```

The repo contains a configuration file in `<repo>/config/tesseract-config.yml` that you can use as an example. This is the file that will be used when running the application via your IDE.

### Configuration options

#### initialPlaylist

This option controls which playlist will load by default when the application starts. Specify the 'displayName' property of the playlist. If the playlist doesn't exist, the application will throw an error and exit. These values are case-sensitive.

#### initialPlayState

This option controls the initial 'playState' of the application. Applicable values are `LOOP_SCENE`, `PLAYING`, or `STOPPED`. These values are case-insensitive and will be converted to all caps internally.

`loop_scene` will simply continue playing the first scene continuously forever.

`playing` will advance scenes in the playlist according to the specified duration.

`stopped` will not play anything (the LEDs will be dark)

#### stageType

Chooses which stage to initialize in the application. Current values are 'CUBOTRON', 'TESSERACT', 'TESSERACT_WALL', 'DRACO', and 'SCARED'. Default is 'CUBOTRON'.

## Deployment

For production/server/embedded (Raspberry Pi) deployment, run the fat jar directly in headless mode:

```bash
java -jar ./build/libs/TesseractFatJar.jar --headless
```

Headless mode is windowless (no LWJGL/OpenGL/X server required) and outputs only UDP to LED hardware. It is auto-selected when no `DISPLAY` is set on non-macOS hosts.

Nothing is cross-compiled for the Pi: Java bytecode is platform-neutral and `build.gradle` pulls prebuilt natives for every 64-bit target including `linux-arm64`, so the jar built on a Mac runs unmodified. The Pi needs only a **JRE 21+**, not a JDK or a dev environment.

**The target must run a 64-bit OS** (`uname -m` → `aarch64`). Graphics natives exist for 32-bit ARM, but JavaCV dropped `linux-armhf` in 1.5.12+, so video clips cannot decode there and the arm64-only WebUI container will not run at all. A 32-bit host starts and renders, then silently never plays video.

**See [`deploy/README.md`](deploy/README.md)** for the full walkthrough — `deploy/deploy.sh` to build and ship, `deploy/start-backend.sh` to run, and a systemd unit to start on boot. It also covers the WebUI container (`aaronlennwilson/tesseract-ui`), which runs alongside the backend rather than containing it: UDP output broadcasts to `255.255.255.255`, which does not traverse Docker's bridge network.

> **Note:** The `docker/` configuration in this repo is legacy (x86_64, runs the GUI under Xvfb rather than true `--headless`) and is not the recommended path.

## Testing

Run the test suite:

```bash
./gradlew test
```

The integration test `TesseractAppTest` verifies that the application can start in headless mode successfully.
