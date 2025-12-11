# Bug Audit Fix Plan

**Created**: 2025-12-10
**Prerequisite**: Complete Java 21 + deps upgrade first
**Execute with**: `/do:it implement the bug fixes from audit plan`

## Summary

22 bugs identified in codebase audit. Fixes organized by priority and grouped by related changes.

---

## Phase 1: Critical Fixes (4 bugs)

### 1.1 Thread-Safe Singletons

**Files**: `TesseractApp.java`, `StateManager.groovy`

| Bug | Location | Fix |
|-----|----------|-----|
| Race condition in TesseractApp singleton | `TesseractApp.java:31-32,80-84` | Use `volatile` + synchronized block or `AtomicReference` |
| Double-checked locking antipattern | `StateManager.groovy:27-33` | Add proper synchronization or use eager initialization |

**Implementation**:
```java
// TesseractApp.java
private static volatile TesseractApp instance;
public static synchronized TesseractApp get() {
    return instance;
}
```

### 1.2 Null Safety in Core Components

**Files**: `PlaylistManager.groovy`, `StateManager.groovy`

| Bug | Location | Fix |
|-----|----------|-----|
| Null pointer risk in PlaylistManager | `PlaylistManager.groovy:38-44` | Add null check before `currentPlaylist` access |
| Null pointer risk in getActiveClip() | `StateManager.groovy:44-46` | Add null checks for channel and scene |

### 1.3 Thread Safety in WebSocket

**File**: `WebsocketInterface.groovy`

| Bug | Location | Fix |
|-----|----------|-----|
| Unsynchronized actionHandlers map | `WebsocketInterface.groovy:22-23,161-168` | Replace `HashMap` with `ConcurrentHashMap` |

---

## Phase 2: High Priority Fixes (7 bugs)

### 2.1 Null Safety in Hardware/Output

**Files**: `UDPModel.java`, `UDP.java`

| Bug | Location | Fix |
|-----|----------|-----|
| Null arrays in UDPModel.send() | `UDPModel.java:143-178` | Initialize arrays to empty, add null guards |
| Incorrect null check in UDP | `lib/udp/src/UDP.java:402` | Check `pa != null` before `pa.getLength()` |

### 2.2 Resource Management

**Files**: `LibGDXRenderer.java`, `Playlist.groovy`

| Bug | Location | Fix |
|-----|----------|-----|
| Resource leak on partial init | `LibGDXRenderer.java:40-59` | Wrap in try-finally, dispose on failure |
| Timer leak in Playlist | `Playlist.groovy:26-27,117-123` | Cancel existing timer before creating new one |

### 2.3 Exception Handling

**File**: `WebsocketInterface.groovy`

| Bug | Location | Fix |
|-----|----------|-----|
| Swallowed exception in onError | `WebsocketInterface.groovy:144-150` | Log error properly, don't throw in callback |

### 2.4 Clip Initialization Safety

**Files**: `PerlinNoiseClip.java`, `ConfigStore.groovy`

| Bug | Location | Fix |
|-----|----------|-----|
| Unguarded palette access | `PerlinNoiseClip.java:87-97` | Check `_palette != null` before use |
| Null cast in getInt() | `ConfigStore.groovy:145-165` | Add null check before Integer cast |

---

## Phase 3: Medium Priority Fixes (6 bugs)

### 3.1 Null Safety Throughout

| Bug | Location | Fix |
|-----|----------|-----|
| Missing null check in Channel.drawNode() | `Channel.java:64-100` | Guard currentScene/nextScene access |
| NPE in PlaylistStore | `PlaylistStore.groovy:62-74` | Check `find()` result before use |
| NPE in Scene construction | `Scene.java:29-30` | Validate SceneStore.get() result |
| Missing null safety in onMessage | `WebsocketInterface.groovy:114-135` | Check handler list before iteration |

### 3.2 Memory Management

| Bug | Location | Fix |
|-----|----------|-----|
| Unbounded WebSocket connections | `StateManager.groovy:20` | Add connection limit or cleanup on disconnect |

---

## Phase 4: Low Priority Fixes (5 bugs)

| Bug | Location | Fix |
|-----|----------|-----|
| Dead code | `PerlinNoiseClip.java:73-83` | Remove commented-out threshold code |
| Integer overflow in colors | `ParticleClip.java:139-141` | Clamp values to 0-255 with `Math.min()` |
| Missing CLI validation | `TesseractLauncher.java:24-40` | Wrap `parseInt()` in try-catch |
| Empty nodes array | `Stage.java:54-62` | Check `nodes.length > 0` before bounds calc |
| NPE in NodeScanClip | `NodeScanClip.java:29` | Add null check for `_myMain.stage` |

---

## Testing Strategy

After each phase:
1. Run `./gradlew test` - all tests must pass
2. Run app in headed mode - verify no crashes
3. Run app in headless mode - verify no crashes

---

## Execution Order

1. `/do:it upgrade to java 21 and latest version of all deps`
2. `/do:it implement the bug fixes from audit plan` (this plan)
   - Phase 1: Critical (thread safety, null guards)
   - Phase 2: High (resources, exceptions)
   - Phase 3: Medium (additional null safety)
   - Phase 4: Low (cleanup, validation)
