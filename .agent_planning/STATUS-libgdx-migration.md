# Status Report: LibGDX Migration Evaluation

**Generated**: 2025-12-10
**Evaluator**: project-evaluator
**Project**: tesseract_java - Processing to LibGDX Migration Assessment

---

## Executive Summary

**Overall Completion**: 0% (Migration not started)
**Current State**: Application running on Processing 3.5.4/4.3 with partial Groovy 4 / Java 17 upgrade
**Migration Complexity**: VERY HIGH
**Recommendation**: **DO NOT PROCEED** without significant planning and user commitment

### Critical Assessment

This is **NOT** a straightforward library swap. Processing is deeply embedded in the application architecture as:
1. The **application framework** (PApplet = main class)
2. The **rendering engine** (all 3D graphics, transformations)
3. The **event system** (mouse, keyboard, draw loop)
4. The **windowing system** (window creation, OpenGL context)
5. A **utility library** (color, math, noise functions used throughout)

**Migration scope**: ~5,400 lines of source code, 16 files directly using Processing APIs, complete architectural refactoring required.

---

## Current Processing Usage Breakdown

### 1. Core Architecture Dependency

**File**: `src/main/app/TesseractMain.java`
- **Extends `PApplet`** - This IS the application
- Implements Processing lifecycle: `settings()`, `setup()`, `draw()`
- Uses `PApplet.main()` as entry point
- Entire application runs inside Processing's event loop

**Impact**: CRITICAL - Application architecture is fundamentally Processing-based

### 2. 3D Rendering and Visualization

**File**: `src/main/app/OnScreen.java` (205 lines)
- Uses Processing 3D rendering exclusively:
  - `ortho()` - orthographic projection
  - `translate()`, `rotateX()`, `rotateY()` - 3D transformations
  - `pushMatrix()`, `popMatrix()` - transformation stack
  - `stroke()`, `strokeWeight()`, `point()` - 3D drawing primitives
  - `background()`, `fill()`, `noFill()` - rendering state
  - `applyMatrix()` - custom rotation matrices
  - `box()`, `line()` - 3D geometry
  - `screenX()`, `screenY()` - 3D to 2D projection
  - `text()`, `textSize()` - text rendering
  - Mouse event handling: `mousePressed`, `mouseX`, `mouseY`
  - `width`, `height`, `frameRate` - window properties

**Impact**: CRITICAL - All visualization code needs complete rewrite

### 3. Clip System (Animation/Effects)

**8 Clip implementations** using Processing APIs:

#### VideoClip.groovy (140 lines) - LEGACY, being replaced
- Uses `processing.video.Movie` class
- Direct pixel access via `movie.copyPixels`
- Video loop control via Processing video library
- **Status**: Partially replaced by JavaCVVideoClip (which doesn't use Processing)

#### JavaCVVideoClip.groovy (228 lines) - NEW implementation
- **Does NOT use Processing** (except `_myMain.map()` for coordinate mapping)
- Uses JavaCV for video playback
- **Migration advantage**: Already Processing-independent for video

#### PerlinNoiseClip.java (108 lines)
- Uses `_myMain.noise()` - Perlin noise generation
- Uses `_myMain.noiseDetail()` - noise configuration
- Uses `_myMain.color()` - color creation
- Uses static `PApplet.map()` - value mapping

#### ColorWashClip.java (75 lines)
- Uses `_myMain.colorMode()` - HSB color mode switching
- Uses `_myMain.color()` - HSB to RGB conversion
- Switches between HSB and RGB color modes

#### SolidColorClip.java (70 lines)
- Comments show HSB color conversion code (disabled)
- RGB-only in current implementation (minimal Processing dependency)

#### LinesClip.java (140 lines)
- Uses `PVector` - 3D vector class (positions, velocities)
- Uses static `PApplet.dist()` - distance calculation
- Uses static `PApplet.map()`, `constrain()` - math utilities
- Uses `_myMain.color()` - color creation

#### ParticleClip.java (130 lines)
- Uses `PVector` extensively
- Uses static `PApplet.dist()`, `map()`, `constrain()`
- Uses `_myMain.color()`

#### Particle.java (70 lines)
- Core physics using `PVector` for position, velocity, acceleration
- Vector math: `add()`, `mult()`, `copy()`
- Uses static `PApplet.map()` for fade calculation

**Impact**: HIGH - All clips need refactoring for LibGDX equivalents

### 4. Color and Math Utilities

**Used throughout application**:
- `PApplet.color()` - RGB/HSB color creation (32-bit ARGB int)
- `PApplet.map()` - linear interpolation
- `PApplet.constrain()` - value clamping
- `PApplet.dist()` - distance calculation
- `PApplet.floor()`, `PApplet.sin()`, `PApplet.cos()` - math functions
- `PApplet.noise()` - Perlin noise (procedural generation)
- `PVector` - 3D vector math class

**Impact**: MEDIUM - Can be replaced with utility class, but used in ~20 locations

### 5. Image and Pixel Processing

**File**: `src/main/hardware/Tile.java`
- Uses `PImage` for tile number images
- Uses `loadImage()` to load images from data folder
- Direct pixel array access: `numberPImage.pixels[loc]`

**Impact**: MEDIUM - Needs LibGDX texture/pixmap replacement

### 6. Additional Processing Dependencies

**Files with Processing imports**:
- `src/main/model/Palette.java` - color utilities
- `src/main/model/Channel.java` - rendering channel (unused method)
- `src/main/hardware/PixelPusher.java` - PApplet reference
- `src/main/output/UDPModel.java` - PApplet reference, `unhex()` utility
- `src/main/environment/PixelPlane.java` - PApplet reference

**Impact**: LOW-MEDIUM - Mostly passing PApplet reference, minimal actual usage

---

## Files Requiring Modification for LibGDX Migration

### Critical Path (Must Rewrite)
1. **TesseractMain.java** (243 lines) - Complete architectural refactor
2. **OnScreen.java** (205 lines) - Complete 3D rendering rewrite
3. **All Clip classes** (8 files, ~900 lines total) - Refactor for LibGDX APIs

### Moderate Refactoring
4. **Tile.java** - Replace PImage with LibGDX Texture/Pixmap
5. **Palette.java** - Replace Processing color utilities
6. **Particle.java** - Replace PVector with LibGDX Vector3
7. **Scene.java** - Update clip instantiation logic

### Minor Changes
8. **UDPModel.java** - Remove PApplet.unhex() calls
9. **PixelPusher.java** - Remove PApplet reference
10. **PixelPlane.java** - Remove PApplet reference
11. **Channel.java** - Clean up unused PApplet method

**Total files needing changes**: ~16 Java/Groovy files
**Total lines affected**: ~2,500+ lines (estimated 45% of codebase)

---

## Processing API Usage Statistics

### By Category
| Category | Occurrences | Complexity |
|----------|-------------|------------|
| 3D Rendering | ~50+ calls | VERY HIGH |
| Vector Math (PVector) | ~30+ uses | HIGH |
| Color Operations | ~20+ calls | MEDIUM |
| Math Utilities | ~20+ calls | LOW |
| Image Loading | ~5 calls | MEDIUM |
| Event Handling | ~10 methods | HIGH |

### Most Used Processing APIs
1. `_myMain.color()` - Used in 7 clips for color creation
2. `PVector` - Used in 3 clips for physics/positioning
3. `PApplet.map()` - Used in 7 clips for value mapping
4. `_myMain.noise()` - Used in PerlinNoiseClip for procedural generation
5. 3D transformation methods - Used extensively in OnScreen.java

---

## LibGDX Migration Challenges

### 1. Application Lifecycle ⚠️ CRITICAL
**Processing**:
```java
public class TesseractMain extends PApplet {
    public void settings() { size(1400, 800, P3D); }
    public void setup() { /* initialization */ }
    public void draw() { /* render loop */ }
    public static void main(String[] args) {
        PApplet.main("app.TesseractMain", args);
    }
}
```

**LibGDX** requires:
```java
public class TesseractMain implements ApplicationListener {
    public void create() { /* setup */ }
    public void render() { /* draw */ }
    public void resize(int width, int height) { }
    public void pause() { }
    public void resume() { }
    public void dispose() { }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        new LwjglApplication(new TesseractMain(), config);
    }
}
```

**Migration work**: Complete restructure of main class and lifecycle

### 2. 3D Rendering System ⚠️ CRITICAL
**Processing**: Immediate mode rendering with automatic matrix stack
```java
pushMatrix();
translate(x, y, z);
rotateX(angle);
point(x, y, z);  // Draws immediately
popMatrix();
```

**LibGDX**: Retained mode rendering with manual camera/batch management
```java
camera.update();
modelBatch.begin(camera);
modelBatch.render(modelInstance, environment);
modelBatch.end();
```

**Migration work**:
- Implement camera system
- Create model/mesh system for points/lines
- Manual shader management
- Complete rendering pipeline rewrite

### 3. PVector → LibGDX Vector3 ⚠️ HIGH
**API differences**:
| Processing PVector | LibGDX Vector3 | Notes |
|-------------------|----------------|-------|
| `new PVector(x,y,z)` | `new Vector3(x,y,z)` | Similar |
| `v.add(other)` | `v.add(other)` | Similar |
| `v.mult(scalar)` | `v.scl(scalar)` | **Different name** |
| `v.copy()` | `new Vector3(v)` | **Different pattern** |
| `PVector.dist(a,b)` | `a.dst(b)` | **Different API** |

**Migration work**:
- Replace all PVector with Vector3 (~30+ locations)
- Update method calls (mult→scl, etc.)
- Test physics calculations

### 4. Color System ⚠️ MEDIUM
**Processing**: 32-bit ARGB int with mode switching
```java
colorMode(HSB, 100);
int c = color(hue, sat, bright);
colorMode(RGB, 255);
int r = (c >> 16) & 0xFF;
```

**LibGDX**: float-based Color class
```java
Color c = new Color(r, g, b, a); // floats 0.0-1.0
// Or use Color utility methods
```

**Migration work**:
- Replace color() calls with Color class
- Handle HSB→RGB conversion (LibGDX doesn't have built-in)
- Update all color manipulation code

### 5. Perlin Noise ⚠️ MEDIUM
**Processing**: Built-in `noise()` function with `noiseDetail()`

**LibGDX**: No built-in Perlin noise
- Must implement or use external library (e.g., `com.badlogic.gdx.math.PerlinNoise` doesn't exist in core)
- Or use `SimplexNoise` from community extensions
- Or implement custom noise generator

**Migration work**:
- Find/implement Perlin noise library
- Ensure same behavior as Processing (critical for visual consistency)

### 6. Event Handling ⚠️ MEDIUM
**Processing**: Method overrides
```java
public void mousePressed() { }
public void keyPressed() { }
```

**LibGDX**: InputProcessor interface
```java
public class MyInputProcessor implements InputProcessor {
    public boolean touchDown(int x, int y, int pointer, int button) { }
    public boolean keyDown(int keycode) { }
}
Gdx.input.setInputProcessor(new MyInputProcessor());
```

**Migration work**: Create InputProcessor, hook up events

### 7. Image Loading ⚠️ LOW-MEDIUM
**Processing**: `loadImage("path")`

**LibGDX**:
```java
Texture texture = new Texture(Gdx.files.internal("path"));
Pixmap pixmap = new Pixmap(Gdx.files.internal("path"));
```

**Migration work**: Replace loadImage calls, update image handling

---

## Build System Impact

### Current Build Configuration
- **Gradle**: 7.6.4
- **Java**: 17
- **Groovy**: 4.0.17
- **Processing Core**: 4.3 (or 3.5.4 from lib/)
- **JOGL**: Bundled with Processing 4
- **Platform support**: macOS (ARM64/x86_64), Linux (x86_64/ARM64/ARMhf)

### LibGDX Build Requirements
```gradle
// LibGDX requires different dependency structure
ext {
    gdxVersion = '1.12.0'
}

dependencies {
    implementation "com.badlogicgames.gdx:gdx:$gdxVersion"
    implementation "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion"
    implementation "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"

    // Plus separate natives for each platform
}
```

**Impact**:
- Replace Processing dependencies with LibGDX
- Update fatJar task for LibGDX natives packaging
- May need to restructure project for LibGDX's conventions
- Test cross-platform native loading

---

## Testing Status

### Current Test Coverage
**Test files**:
1. `TesseractAppTest.groovy` - Integration test that launches full Processing application
2. `ReadDracoMappingTest.groovy` - CSV mapping parsing (no Processing dependency)
3. `ConfigStoreTest.groovy` - Configuration loading (no Processing dependency)

**Test status** (from previous run):
- ❌ `TesseractAppTest` - FAILS with JOGL native library errors
- ✅ `ReadDracoMappingTest` - PASSES
- ❌ `ConfigStoreTest` - FAILS (Java 17 reflection issue, not Processing-related)

**Processing-dependent tests**: Only `TesseractAppTest`

### Testing Challenges for LibGDX
- LibGDX headless testing requires HeadlessApplication
- OpenGL mocking is complex
- Full integration tests harder than with Processing
- May need to restructure for better testability

**Impact**: MEDIUM - Tests need significant updates, but test coverage is already minimal

---

## Dependencies Analysis

### Current Dependencies (Processing-related)
```gradle
// Processing core
implementation fileTree(dir: 'lib', include: ['**/*.jar'])  // includes processing-core/core.jar
// OR
implementation 'org.processing:core:4.3'

// JOGL (OpenGL bindings) - bundled with Processing
// lib/jogl-4.0/jogl-all.jar
// lib/jogl-4.0/gluegen-rt.jar

// Processing libraries
implementation 'org.processing:serial:3.3.7'
// lib/video/ - Processing video library (being replaced by JavaCV)
// lib/udp/ - Processing UDP library
```

### Required LibGDX Dependencies
```gradle
ext {
    gdxVersion = '1.12.0'
}

dependencies {
    // Core
    implementation "com.badlogicgames.gdx:gdx:$gdxVersion"

    // Desktop backend (LWJGL3)
    implementation "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion"
    implementation "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"

    // Platform-specific natives (for fat jar)
    implementation "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"
    // OR separate for each platform:
    // implementation "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a"
    // implementation "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a"
    // etc.

    // May need extensions
    // implementation "com.badlogicgames.gdx:gdx-freetype:$gdxVersion"  // For fonts
}
```

**Additional considerations**:
- LibGDX uses LWJGL3 (modern OpenGL bindings)
- Native libraries are much larger than JOGL
- May need gdx-freetype for text rendering
- Cross-platform packaging more complex

---

## Key Risks and Unknowns

### CRITICAL RISKS ⚠️

1. **Rendering Parity**
   - Can LibGDX replicate Processing's visual output exactly?
   - Point cloud rendering in 3D with proper depth buffering
   - Screen-space coordinate projection (`screenX/screenY`)
   - **Impact**: If visual output differs, entire LED mapping may be wrong

2. **Performance**
   - Processing optimized for creative coding, immediate mode
   - LibGDX optimized for games, retained mode
   - Will frame rate remain stable at 30fps with ~1000+ points?
   - Memory usage with model batching vs. immediate mode
   - **Impact**: Performance regression could make system unusable

3. **Perlin Noise Consistency**
   - Processing's noise() is specific implementation
   - Must match EXACTLY for visual consistency
   - Different noise = different animations = different look
   - **Impact**: If noise differs, procedural animations look wrong

4. **Development Time**
   - Estimated: **100-200 hours** of development
   - Not a "drop-in replacement" - complete architectural rewrite
   - High risk of subtle bugs in physics, rendering, color
   - **Impact**: Massive time investment with uncertain outcome

### HIGH RISKS ⚡

5. **3D Camera System**
   - Processing handles camera/projection automatically
   - LibGDX requires manual camera setup and management
   - Orthographic projection configuration
   - Mouse-based rotation system needs reimplementation
   - **Impact**: Incorrect camera = wrong visualization

6. **Testing Coverage**
   - Minimal tests currently
   - LibGDX harder to test (OpenGL context required)
   - No visual regression tests
   - **Impact**: Hard to verify migration correctness

7. **Cross-Platform Native Libraries**
   - LibGDX natives are larger than JOGL
   - More platforms to test
   - Fat jar packaging more complex
   - **Impact**: Deployment and distribution complications

### MEDIUM RISKS 💡

8. **Color Space Handling**
   - Processing's HSB mode switching is convenient
   - LibGDX doesn't have built-in HSB
   - Color conversion must be manual
   - **Impact**: Colors may not match exactly

9. **Learning Curve**
   - Team must learn LibGDX APIs
   - Different paradigm (game engine vs. creative coding)
   - Less Processing-like, more complex
   - **Impact**: Slower development, more bugs

10. **Regression Potential**
    - Every clip must be reimplemented
    - Physics calculations may have subtle bugs
    - Visual differences may not be immediately obvious
    - **Impact**: Long tail of bug fixes post-migration

---

## Alternative Approaches

### Option 1: Stay on Processing ✅ RECOMMENDED
**Pros**:
- Zero migration effort
- Code already works
- Processing 4 has ARM64 support
- Visual output proven correct
- Team familiar with APIs

**Cons**:
- Stuck with Processing ecosystem
- Less control over rendering pipeline
- Processing development has slowed

**Effort**: 0 hours
**Risk**: NONE

### Option 2: Hybrid Approach - Minimal Processing
**Keep**:
- Processing for window/OpenGL context creation
- Processing for math utilities (map, constrain, noise)
- PVector for vector math

**Replace**:
- Direct OpenGL calls for rendering instead of Processing's API
- Custom camera management
- Custom rendering pipeline

**Pros**:
- Smaller migration scope
- Keep convenient Processing utilities
- More rendering control

**Cons**:
- Still dependent on Processing
- Mixed paradigm (confusing)
- May not gain much

**Effort**: 40-80 hours
**Risk**: MEDIUM

### Option 3: Full LibGDX Migration ⚠️ NOT RECOMMENDED
**Approach**:
- Complete rewrite as described in this document
- Replace every Processing API with LibGDX equivalent
- Restructure application architecture

**Pros**:
- Modern game engine
- Better performance potential
- More control
- Larger ecosystem

**Cons**:
- MASSIVE effort (100-200 hours)
- HIGH risk of visual regressions
- Difficult to verify correctness
- No guarantee of success

**Effort**: 100-200 hours
**Risk**: VERY HIGH

### Option 4: Gradual Migration Strategy
**Phase 1**: Extract non-rendering code
- Separate business logic from rendering
- Create interfaces that both Processing and LibGDX could implement
- Improve testability

**Phase 2**: Parallel implementation
- Implement LibGDX renderer alongside Processing
- Runtime switch between renderers
- Extensive visual comparison testing

**Phase 3**: Deprecate Processing
- Once LibGDX version proven equivalent
- Remove Processing dependency

**Pros**:
- Lower risk (can rollback)
- Validates assumptions incrementally
- Improves code quality along the way

**Cons**:
- Longest timeline (200-300 hours)
- Temporary code duplication
- Complex to maintain dual implementation

**Effort**: 200-300 hours
**Risk**: MEDIUM (but staged)

---

## Recommendations

### PRIMARY RECOMMENDATION: **DO NOT MIGRATE** ❌

**Rationale**:
1. **Processing 4 already solves the ARM64 problem** - No technical need
2. **Risk/effort ratio is terrible** - 100-200 hours for uncertain benefit
3. **High regression risk** - Visual output must be EXACTLY correct for LED mapping
4. **No clear business value** - What problem does LibGDX solve that Processing 4 doesn't?
5. **Test coverage is minimal** - Hard to verify migration correctness

### SECONDARY RECOMMENDATION: **Improve architecture first**

If there ARE reasons to migrate (not yet articulated), do this first:
1. **Extract rendering interface** - Decouple business logic from Processing
2. **Improve test coverage** - Add visual regression tests
3. **Measure performance** - Establish baseline with Processing 4
4. **Document visual behavior** - Record expected output for all clips
5. **Create proof-of-concept** - Build ONE clip in LibGDX, compare results

**Only proceed with migration IF**:
- Proof-of-concept succeeds
- Performance testing shows clear benefit
- Business case is compelling
- Team committed to 200+ hour project

---

## What Could Not Be Verified

### Runtime Behavior
| Item | Why | User Can Check |
|------|-----|----------------|
| Processing 4 ARM64 performance | No access to ARM64 Mac | Run on Apple Silicon, monitor frame rate and CPU usage |
| Visual output correctness | Application doesn't launch (JOGL native error) | Fix native libraries, run application, verify LED mapping correct |
| Memory leaks over time | Can't run long-term tests | Run for 8+ hours, monitor memory growth |

### LibGDX Compatibility
| Item | Why | User Can Check |
|------|-----|----------------|
| Perlin noise equivalence | Would need to implement LibGDX version | Create side-by-side comparison with same seed values |
| Rendering performance | No LibGDX version exists | Build proof-of-concept, benchmark frame rate |
| Point cloud rendering | Complex to prototype | Implement minimal LibGDX point renderer, compare visuals |

### Cross-Platform Native Loading
| Item | Why | User Can Check |
|------|-----|----------------|
| LibGDX on Raspberry Pi | No Raspberry Pi hardware | Deploy to Pi, test native library loading |
| LibGDX fat jar size | No LibGDX build exists | Build LibGDX version, compare jar sizes |

---

## Implementation Cost Estimates

### Full LibGDX Migration (Option 3)

| Task | Complexity | Hours |
|------|-----------|-------|
| **Phase 1: Core Migration** | | |
| Application lifecycle (TesseractMain) | HIGH | 8-12 |
| 3D rendering system (OnScreen) | VERY HIGH | 20-30 |
| Camera and projection | HIGH | 8-12 |
| Event handling (mouse, keyboard) | MEDIUM | 4-6 |
| **Phase 2: Clip System** | | |
| Replace PVector with Vector3 | MEDIUM | 8-12 |
| Implement Perlin noise | HIGH | 8-12 |
| Migrate all clips (8 files) | HIGH | 20-30 |
| Color system refactor | MEDIUM | 6-8 |
| **Phase 3: Supporting Systems** | | |
| Image loading (Tile.java) | LOW | 2-4 |
| Utility classes | LOW | 4-6 |
| Build system updates | MEDIUM | 4-6 |
| **Phase 4: Testing & Validation** | | |
| Visual regression testing | HIGH | 12-16 |
| Physics/math verification | MEDIUM | 8-12 |
| Cross-platform testing | MEDIUM | 8-12 |
| Bug fixing and polish | HIGH | 16-24 |
| **TOTAL** | **VERY HIGH** | **136-202 hours** |

**Note**: This assumes:
- Developer familiar with LibGDX
- No major unexpected issues
- Acceptable visual differences
- Good documentation

**Reality check**: Add 25-50% for:
- Learning curve
- Debugging subtle differences
- Visual calibration
- Scope creep

**Realistic estimate**: **170-300 hours**

---

## Questions for User

Before ANY migration work begins, these must be answered:

### 1. WHY migrate to LibGDX?
- What problem does it solve that Processing 4 doesn't?
- What's the business case / value proposition?
- Is there a technical requirement driving this?

### 2. What's the success criteria?
- How will we verify visual output is correct?
- What's acceptable performance? (frame rate, CPU usage, memory)
- What's the rollback plan if migration fails?

### 3. What's the timeline and resource commitment?
- Who will do this work?
- What's the deadline? (realistic: 2-3 months for one developer)
- What's the opportunity cost? (what else could be built in 200 hours?)

### 4. Has Processing 4 ARM64 been tested?
- Does the current system work on Apple Silicon?
- What's the actual performance?
- Are there REAL limitations with Processing 4?

### 5. What's the risk tolerance?
- OK if visual output is slightly different?
- OK if some animations look different due to noise changes?
- OK if migration takes 2x longer than estimated?

---

## Next Steps

### IF USER DECIDES TO MIGRATE:

1. **Proof of Concept** (40 hours)
   - Implement ONE clip (PerlinNoiseClip) in LibGDX
   - Side-by-side visual comparison with Processing version
   - Performance benchmarking
   - Document differences

2. **Architecture Preparation** (40 hours)
   - Extract rendering interfaces
   - Improve test coverage
   - Create visual regression test framework
   - Document expected behavior for all clips

3. **Decision Point**
   - Review POC results
   - Re-evaluate effort estimates based on learnings
   - GO / NO-GO decision

4. **Only if GO**: Begin phased migration per Option 4 strategy

### IF USER DECIDES NOT TO MIGRATE (RECOMMENDED):

1. **Fix current issues**
   - Resolve JOGL native library loading for tests
   - Complete Groovy 4 / Java 17 upgrade (per existing plan)
   - Verify Processing 4 ARM64 support works

2. **Improve architecture**
   - Decouple business logic from rendering
   - Improve test coverage
   - Document system behavior

3. **Monitor Processing 4**
   - Track Processing development
   - Evaluate alternatives if Processing becomes unmaintained
   - But don't migrate preemptively

---

## Workflow Recommendation

- [X] **PAUSE** - LibGDX migration needs user decision before proceeding

### Clarification Needed Before Proceeding

#### Question 1: What is the motivation for LibGDX migration?
**Context**: Processing 4 appears to already provide ARM64 support via JOGL 4.0. The project has partial support for this upgrade already implemented.

**How it was guessed**: Migration assumed necessary for ARM64/modern OpenGL, but this may not be true with Processing 4.

**Options**:
- **Option A**: Processing 4 insufficient (explain why: performance, features, etc.)
- **Option B**: LibGDX desired for other reasons (game engine features, ecosystem)
- **Option C**: Exploration / modernization effort
- **Option D**: No strong reason - stay on Processing

**Impact of wrong choice**: If migrating without clear need, wasting 200+ hours of development effort.

#### Question 2: What's the acceptable effort/risk level?
**Context**: Full migration is 100-200+ hours with HIGH risk of visual regressions that may break LED mapping.

**Options**:
- **Option A**: Low risk, low effort - Stay on Processing (0 hours)
- **Option B**: Medium risk, medium effort - Proof of concept first (40 hours, then re-evaluate)
- **Option C**: High risk, high effort - Full migration with staged approach (200-300 hours)

**Impact of wrong choice**: Committing to migration without adequate resources = incomplete/broken project.

#### Question 3: How will visual correctness be verified?
**Context**: LED mapping depends on exact color values and coordinate calculations. Any difference = lights look wrong.

**Options**:
- **Option A**: Side-by-side comparison with current system (manual verification)
- **Option B**: Automated visual regression tests (requires test framework development)
- **Option C**: Tolerate minor differences (risky for production LED system)

**Impact of wrong choice**: Can't verify migration succeeded = broken production system.

---

## Summary for User

**Current state**: Application runs on Processing 3/4 with ~5,400 lines of code, 16 files using Processing APIs extensively.

**Migration scope**: Complete architectural rewrite, touching 45% of codebase.

**Estimated effort**: 100-200+ hours of complex development work.

**Risk level**: VERY HIGH - Visual regressions would break LED mapping.

**Recommendation**: **Stay on Processing 4** unless there's a compelling reason to migrate that outweighs the enormous cost and risk.

**If you proceed**: Start with proof-of-concept (40 hours), then re-evaluate.

**Questions**: Need answers to WHY, success criteria, and resource commitment before any work begins.

---

**Evaluation complete. Awaiting user decision on migration path.**
