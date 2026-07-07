# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Tesseract is a [Processing](https://processing.org)-based control system that drives interactive kinetic LED and flame-effect sculptures. It renders animations frame-by-frame, maps them onto a 3D arrangement of physical LED nodes, and streams the resulting colors over UDP to hardware controllers. A browser-based UI talks to the app over a WebSocket.

## Critical constraint: Java 8 only

**Must build/run with JDK 1.8.** Processing 3 does not support anything newer. This is the single most common source of build/run failures. If your default `java` is newer (e.g. 21), point the build at a JDK 8 explicitly, e.g. on macOS:

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 1.8)"
```

The bundled Gradle wrapper is **Gradle 5.4.1**, which itself requires Java 8 to run these tasks.

## Build & run

Dependencies are downloaded/unpacked by custom Gradle tasks (Processing core, video, UDP, and JOGL libs) rather than resolved purely from Maven. They are slow, so they are **not** wired into the normal build and must be run manually the first time (and after cleaning `lib/`):

```bash
# One-time (or after cleaning lib/): fetch native/Processing libs
./gradlew unzipProcessingVideoLibrary unzipProcessingUdpLibrary untarProcessingCoreLibrary downloadJoglJar

# Build the standalone fat jar (Main-Class: app.TesseractMain) -> build/libs/TesseractFatJar.jar
./gradlew fatJar
```

**Heads-up (verified 2026-07):** `downloadProcessingCoreLibrary`/`untarProcessingCoreLibrary` currently **fail** — their source URL (`download.processing.org/processing-3.5.3-linux64.tgz`) returns 404. This is not fatal: the jars those tasks produce (`lib/processing-core/core.jar`, `lib/jogl-2.3.2-patched/…`) are already committed in `lib/`, and `build.gradle` puts every jar in `lib/` on the classpath. So `./gradlew fatJar` builds fine on its own as long as `lib/` is populated. Only run the (still-working) `unzipProcessingVideoLibrary`/`unzipProcessingUdpLibrary`/`downloadJoglJar` tasks if those pieces are missing from `lib/`.

macOS convenience scripts (start builds the jar if missing, and passes the gstreamer native paths needed for video):

```bash
./bin/build_macos.sh
./bin/start_macos.sh
```

In IntelliJ, run the `main` method in `src/main/app/TesseractMain.java` (save it as a run configuration). Open the project by pointing IntelliJ at `build.gradle`.

## Tests

```bash
./gradlew test                                   # all tests (cleanTest runs first; full stdout shown)
./gradlew test --tests "stores.ConfigStoreTest"  # single test class
```

Tests are JUnit 4 with Mockito + PowerMock (used to mock the pervasive static singletons). `src/test/testUtil/MockMain.groovy` stands in for the `TesseractMain` PApplet so store/clip logic can run without a real graphics context.

**Known environment caveat:** `integration.TesseractAppTest.testCanStartApplication` boots the real Processing/OpenGL stack. On Apple Silicon it fails with `UnsatisfiedLinkError` because the bundled JOGL natives (`libgluegen-rt.jnilib`) are x86-only (`i386,x86_64`, no `arm64`). The rest of the suite (unit/store tests) passes. Run the app / this test on x86_64 or under a Rosetta x86 JVM. `./gradlew test --tests "stores.ConfigStoreTest"` is a good headless-safe smoke check.

## Language layout

Source lives under `src/main` and `src/test`. **Both Java and Groovy compile together from the same directories** (configured in `build.gradle` sourceSets) — a `.java` file and a `.groovy` file can reference each other directly. Groovy is used for the more dynamic pieces (stores, config, websocket, playlists); Java for the hot per-frame rendering path (clips, nodes, channel).

## Architecture

### The frame loop (`app/TesseractMain`)
`TesseractMain extends PApplet` and is the single global instance, reachable anywhere via `TesseractMain.getMain()`. Almost everything reaches back to it this way. `setup()` wires the whole system together (stores → config → websocket → UDP → stage → channel → playlist). `draw()` runs every frame at 30fps:
1. `channel1.run()` advances the active clip's animation.
2. For each `Node` in `stage.nodes`, `renderNode()` asks the channel to compute an RGB color and stores it on the node.
3. `onScreen.draw()` renders the on-screen 3D visualization (`app/OnScreen`).
4. `udpModel.send()` pushes node colors out to the physical lights.

### Stage & Nodes (`environment/`)
A `Stage` holds the array of `Node`s — each Node is one physical LED with a 3D position (`x,y,z`), a screen position, and current `r,g,b`. `Stage.buildStage(stageType)` constructs the node layout for one of three sculptures: `CUBOTRON`, `DRACO`, or `TESSERACT` (chosen by the `stageType` config option). DRACO's layout is read from CSV mapping files in `data/mapping/draco_csv/` via `mapping/ReadDracoMapping` and `environment/StrandPanel`.

### Clips, Scenes, Playlists (`clip/`, `show/`)
- A **Clip** (`clip/AbstractClip` + subclasses like `SolidColorClip`, `PerlinNoiseClip`, `VideoClip`) is an animation. It exposes 8 normalized params `p1..p8` (all in range 0.00–1.00) and overrides `init()` / `run()` / `die()`. Clip types are identified by int constants on `TesseractMain` (`NODESCAN`, `SOLID`, `COLORWASH`, `VIDEO`, `PARTICLE`, `PERLINNOISE`, `LINESCLIP`).
- A **Scene** (`show/Scene`) = a clip type + saved param values + optional filename; it instantiates and owns the live clip.
- A **Playlist** (`show/Playlist`) = an ordered list of scenes with play states (`LOOP_SCENE`, `PLAYING`, `STOPPED`). `PlaylistManager` (singleton) plays a playlist into a `Channel`.
- A **Channel** (`model/Channel`) holds the current/next scene and produces per-node colors (`drawNode`). Only `channel1` is active; multi-channel mixing is stubbed/TODO.

### Persistence (`stores/`, `state/`)
Stores are singletons accessed via `SomeStore.get()`: `ConfigStore`, `SceneStore`, `PlaylistStore`, `MediaStore`. Scene/Playlist data is persisted as JSON under `data/default/` (see `state/IJsonPersistable`, `StateManager`). On startup the app creates built-in scenes/playlists (`Util.createBuiltInScenes/Playlists`) and saves them before loading config.

### Configuration (`stores/ConfigStore`)
Three sources, in precedence order: **Java system properties (`-D`) > environment variables > YAML file**. Env var names are the option key upper-snake-cased with a `TESSERACT_` prefix (e.g. `initialPlayState` → `TESSERACT_INITIAL_PLAY_STATE`). The YAML file defaults to `./config/tesseract-config.yml`; override with `-DconfigPath=...` or `TESSERACT_CONFIG_PATH`. Key options: `stageType`, `initialPlaylist` (matched against a playlist's `displayName`), `initialPlayState`. See README for full details.

### WebSocket UI bridge (`websocket/WebsocketInterface`)
Singleton WebSocket server on `0.0.0.0:8883`. The front-end sends JSON messages with an `action` field; handlers are registered as Groovy closures via `registerActionHandler(actionType, handler)` and stored in the `actionHandlers` map (multiple handlers per action allowed). The shutdown hook in `TesseractMain` must cleanly stop this server to free the port.

### Hardware output (`output/`, `hardware/`)
`output/UDPModel` broadcasts node colors and flame-effect commands over UDP (default local port 7777; broadcasts to `255.255.255.255`). `hardware/` models the controllers: `Rabbit` (flame FX), `Teensy` (LED), plus `Tile`/`Fixture`/`Node` groupings. Counts of controllers come from config (`numRabbits`, `numTeensies`).

## Deployment

`deploy/draco/` holds scripts for deploying to the DRACO installation. `docker/` contains a Dockerfile and helper scripts for running the app (and websocket testing) in a container.
