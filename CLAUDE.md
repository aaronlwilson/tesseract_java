# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Tesseract Java is a LibGDX-based control system for interactive Kinetic LED and Flame FX sculptures. It displays procedurally-generated visual content on LED arrays while simultaneously sending color data to physical hardware controllers via UDP.

**Tech Stack**: Java 21, Groovy 4.0.29, LibGDX 1.14.0, JavaCV 1.5.12 (FFmpeg 7.1.1), WebSocket

## Build Commands

```bash
# Download UDP library (required once before building)
./gradlew unzipProcessingUdpLibrary

# Build fat JAR with all dependencies
./gradlew fatJar

# Run tests
./gradlew test

# Combined: download deps and build
./gradlew unzipProcessingUdpLibrary fatJar
```

## Running the Application

```bash
# Desktop with GUI window
java -XstartOnFirstThread -jar build/libs/TesseractFatJar.jar

# Headless mode (server/Raspberry Pi - no window, UDP output only)
java -jar build/libs/TesseractFatJar.jar --headless

# macOS helpers
./bin/build_macos.sh
./bin/start_macos.sh
```

**Note**: The `-XstartOnFirstThread` flag is required on macOS for LibGDX/LWJGL to work properly.

## Configuration

Three-level precedence (highest to lowest):
1. **System properties**: `java -DinitialPlaylist="All Videos" -jar ...`
2. **Environment variables**: `TESSERACT_INITIAL_PLAY_STATE=playing java -jar ...`
3. **YAML file**: `./config/tesseract-config.yml`

Key config options:
- `initialPlaylist` - playlist displayName to load on startup
- `initialPlayState` - `LOOP_SCENE`, `PLAYING`, or `STOPPED`
- `stageType` - `CUBOTRON`, `DRACO`, or `TESSERACT`

## Architecture

### Entry Points
- `app/TesseractLauncher.java` - CLI entry point, handles `--headless` flag
- `app/TesseractApp.java` - LibGDX ApplicationListener, main render loop

### Core Systems

**Renderer Abstraction** (`render/`):
- `IRenderer` interface with `LibGDXRenderer` (desktop) and `HeadlessRenderer` (server) implementations
- Same clip code runs identically in both modes

**Clip System** (`clip/`):
- All visual effects extend `AbstractClip`
- Implement `run()` (update animation) and `drawNode(Node)` (return RGB for each LED)
- `ClipMetadata.groovy` - registry of all clips and their UI controls

**Playlist/Scene System** (`show/`):
- `PlaylistManager` → `Playlist` → `PlaylistItem[]` → `Scene` → `AbstractClip`
- Scenes hold clip instances with parameters; playlists sequence scenes with durations

**State Management** (`state/`):
- `StateManager` maintains app state and syncs with WebSocket UI clients (port 8883)
- Stores persist to JSON files (`stores/`)

**Hardware Output** (`hardware/`, `output/`):
- `Stage` holds 3D coordinates for all LEDs as `Node` objects
- `UDPModel` sends RGB data to Teensy/PixelPusher/Rabbit controllers via UDP

### Rendering Pipeline (30 FPS)

```
TesseractApp.render()
  → IRenderer.beginFrame()
  → Channel.run() → Clip.run() → Clip.drawNode() for each Node
  → IRenderer.drawNode() (renders on screen)
  → IRenderer.endFrame()
  → UDPModel.send() (sends to hardware)
```

### Source Organization

All source files are in `src/main/` and `src/test/` with mixed Java/Groovy (no separate directories). Groovy files are compiled alongside Java files.

Key packages:
- `app/` - entry points
- `render/` - graphics abstraction
- `clip/` - animation effects (11 implementations)
- `show/` - playlist/scene management
- `state/` - state management
- `stores/` - data persistence (JSON/YAML)
- `environment/` - stage/LED layout definitions
- `hardware/` - LED controller interfaces
- `output/` - UDP networking

## Adding a New Clip

1. Create `NewClip.java` extending `AbstractClip` in `src/main/clip/`
2. Implement `run()` and `drawNode(Node node)` methods
3. Register in `ClipMetadata.getClipMetadata()` with UI control definitions

## Testing

```bash
./gradlew test                    # Run all tests
./gradlew test --tests "*AppTest" # Run specific test
```

Tests use JUnit 6 with JUnit Pioneer for environment variable testing (Java 21 compatible). The `TesseractAppTest` integration test verifies headless startup works correctly.
