# Status Report - Groovy 4.x / Java 17 / Processing 4 Upgrade
**Generated**: 2025-12-09
**Project**: tesseract_java (LED visualization app)

## Executive Summary
**Overall**: ~15% complete | **Critical blockers**: 3 | **Build status**: ❌ FAILING

**Current environment**: Java 17 ✅ | Gradle 5.4.1 ❌ | Groovy 2.5.6 ❌ | Processing 3.5.4 ❌

**Critical Issue**: Project **cannot build** on Java 17 with current Gradle/Groovy versions. Gradle 5.4.1 ships with Groovy 2.5.4, which is incompatible with Java 17.

## Runtime Assessment

**Attempted**: Build project with `./gradlew build`

**Result**: ❌ **BUILD FAILED** - Fatal compatibility error

**Evidence**:
```
java.lang.NoClassDefFoundError: Could not initialize class org.codehaus.groovy.vmplugin.v7.Java7
	at org.codehaus.groovy.vmplugin.VMPluginFactory.<clinit>(VMPluginFactory.java:43)
```

**Root Cause**: Groovy 2.5.x does not support Java 17. The embedded Groovy in Gradle 5.4.1 fails immediately when JVM initializes.

**Impact**: **Zero functionality can be verified.** Application cannot compile, cannot run, cannot be tested.

## What's Been Completed ✅

### 1. Video Library Replacement (PARTIAL)
- ✅ JavaCV dependencies added to `build.gradle` (all platforms: macOS ARM64/x86_64, Linux x86_64/ARM64/ARMhf)
- ✅ `JavaCVVideoClip.groovy` created as replacement for `VideoClip.groovy`
- ✅ `Scene.java` updated to instantiate `JavaCVVideoClip` instead of `VideoClip`
- ✅ `TesseractMain.java` cleaned up - removed Processing video import and `movieEvent()` callback
- ✅ JOGL ARM64 natives extracted to `lib/jogl-2.3.2-patched/macos-aarch64/` and `lib/natives-macos-aarch64/`

**Code Quality**: JavaCVVideoClip implementation looks solid - proper resource cleanup, looping support, pixel format handling matches VideoClip API.

### 2. Processing Core Update (PARTIAL)
- ✅ `build.gradle` updated to download Processing 3.5.4 (was 3.5.3)
- ✅ Processing 3.5.4 core.jar present in `lib/processing-core/`
- ❌ Still Processing **3.x**, not Processing **4.x** (which has proper ARM64 JOGL support)

## Critical Blockers 🚨

### BLOCKER #1: Groovy 2.5.6 + Java 17 Incompatibility
**Status**: ❌ **BLOCKING ALL WORK**

**Problem**:
- `build.gradle` declares `groovy-all:2.5.6`
- Gradle 5.4.1 ships with embedded Groovy 2.5.4
- **Neither Groovy 2.5.x version supports Java 17**
- Java 17 requires **Groovy 4.0+**

**Evidence**: Build fails immediately with `NoClassDefFoundError: org.codehaus.groovy.vmplugin.v7.Java7`

**Required Fix**:
1. Upgrade to Gradle 7.3+ (first version supporting Groovy 4.x)
2. Update dependency from `groovy-all:2.5.6` to `groovy-all:4.0.x` (or newer)
3. Update all Groovy source files for Groovy 4.x breaking changes

**Groovy 4.x Breaking Changes to Address**:
- `groovy-all` artifact no longer exists - must use individual modules or BOM
- Package reorganization (some internal classes moved)
- AST transformation changes
- Closure delegate handling differences

### BLOCKER #2: Gradle 5.4.1 Too Old for Java 17
**Status**: ❌ **BLOCKING ALL WORK**

**Problem**:
- `gradle-wrapper.properties` specifies Gradle 5.4.1 (released 2019)
- Gradle 5.x **does not officially support Java 17** (released 2021)
- Gradle 7.3+ required for proper Java 17 + Groovy 4.x support

**Required Fix**: Update `gradle-wrapper.properties` to Gradle 7.3+ (recommend 7.6.4 or 8.x)

**Compatibility Matrix**:
- Java 17 → requires Gradle 7.3+
- Groovy 4.x → requires Gradle 7.0+
- Processing 4.x → works with Java 17

### BLOCKER #3: Deprecated Gradle Syntax
**Status**: ❌ **WILL BREAK on Gradle 8.x**

**Problem**: All dependencies use `compile` (removed in Gradle 7.0) and `testCompile` (removed in Gradle 7.0)

**Evidence**: 32 instances of `compile` in `build.gradle`
```gradle
compile group: 'org.codehaus.groovy', name: 'groovy-all', version: '2.5.6'  // DEPRECATED
compile group: 'org.bytedeco', name: 'javacv', version: '1.5.9'              // DEPRECATED
testCompile group: 'junit', name: 'junit', version: '4.12'                   // DEPRECATED
```

**Required Fix**: Replace all occurrences:
- `compile` → `implementation`
- `testCompile` → `testImplementation`
- `compile fileTree(...)` → `implementation fileTree(...)`
- `configurations.compile` → `configurations.runtimeClasspath` (in fatJar task)

## Remaining Work for Full Upgrade

### Phase 1: Unblock Build ⚠️ CRITICAL PATH
**Priority**: P0 - **MUST DO FIRST**

1. **Update Gradle wrapper** (5.4.1 → 7.6.4)
   - Modify `gradle/wrapper/gradle-wrapper.properties`
   - Run `./gradlew wrapper --gradle-version=7.6.4`

2. **Replace deprecated dependency syntax** in `build.gradle`
   - `compile` → `implementation` (all 29 instances)
   - `testCompile` → `testImplementation` (all 8 instances)
   - Update `fatJar` task: `configurations.compile` → `configurations.runtimeClasspath`

3. **Upgrade Groovy** (2.5.6 → 4.0.x)
   - Replace `groovy-all:2.5.6` with Groovy 4.x BOM or individual modules:
     ```gradle
     implementation platform('org.apache.groovy:groovy-bom:4.0.23')
     implementation 'org.apache.groovy:groovy'
     ```

4. **Verify build succeeds**
   - Run `./gradlew clean build`
   - Fix any Groovy 4.x compatibility issues in source files

**Estimated Complexity**: MEDIUM (syntax changes are mechanical, but Groovy 4.x may have breaking changes)

### Phase 2: Processing 4.x Upgrade
**Priority**: P1 - Core requirement for ARM64 JOGL

**Current**: Processing 3.5.4 (still has JOGL ARM64 issues)
**Target**: Processing 4.3 (proper ARM64 JOGL support)

**Tasks**:
1. Update Processing core dependency
   - Remove `lib/processing-core/core.jar` approach
   - Use Maven Central: `implementation 'org.processing:core:4.3'`
   - Remove Gradle tasks: `downloadProcessingCoreLibrary`, `untarProcessingCoreLibrary`

2. Update Processing serial library
   - Current: `compile group: 'org.processing', name: 'serial', version: '3.3.7'`
   - Update to Processing 4.x version (verify availability on Maven Central)

3. **API Changes to Handle** (Processing 3 → 4):
   - Review `PApplet` API changes (some methods renamed/removed)
   - Check `P3D` renderer compatibility
   - Verify `pixelDensity()` still works same way
   - Test JOGL initialization (`settings()` method in `TesseractMain`)

4. **Remove obsolete Processing video library**
   - Delete `lib/video/` directory (no longer needed - using JavaCV)
   - Remove Gradle tasks: `downloadProcessingVideoLibrary`, `unzipProcessingVideoLibrary`
   - Delete old `VideoClip.groovy` (replaced by `JavaCVVideoClip`)

**Estimated Complexity**: MEDIUM-HIGH (API changes unknown until attempted)

### Phase 3: Update Dependencies to Java 17 Compatible Versions
**Priority**: P2 - Risk mitigation

**Old dependencies that may need updates**:
- `slf4j-api:1.7.26` (2019) → `2.0.x` (Java 17 compatible)
- `slf4j-log4j12:1.7.26` → Consider switching to `slf4j-simple` or `logback-classic`
- `Java-WebSocket:1.4.0` (2019) → `1.5.x` (verify Java 17 compatibility)
- `snakeyaml:1.24` (2019) → `2.x` (has Java 17 specific fixes)
- JUnit 4.12 → Consider JUnit 5 (Jupiter) for better Java 17 support

**Risk**: Some old libraries may have subtle Java 17 incompatibilities (sealed classes, modules, etc.)

### Phase 4: Docker Build Updates
**Priority**: P2 - Required for deployment

**Current**: Dockerfile uses Ubuntu 16.04 + Java 8
**Target**: Ubuntu 22.04 + Java 17

**Files to update**:
- `docker/Dockerfile`:
  - Base image: `ubuntu:16.04` → `ubuntu:22.04`
  - Java version: Java 8 → Java 17 (use Eclipse Temurin or Amazon Corretto)
  - Remove Java 8 download logic, install from apt
  - Update native library paths if needed

- `docker/build.sh`:
  - Verify still works with new Gradle
  - May need to update Gradle wrapper before Docker build

**Estimated Complexity**: LOW-MEDIUM (mostly mechanical changes)

### Phase 5: Testing & Verification
**Priority**: P1 - Validation

**Must verify**:
1. ✅ **Build succeeds** on all platforms
   - macOS ARM64 (Apple Silicon) ← **PRIMARY TARGET**
   - macOS x86_64 (Intel)
   - Linux x86_64
   - Docker (Linux x86_64)

2. ✅ **Application launches** without errors
   - JOGL initializes correctly
   - Processing window opens
   - No native library errors

3. ✅ **JavaCV video playback works**
   - Load video file via JavaCVVideoClip
   - Video plays and loops correctly
   - Pixel data maps to LEDs correctly
   - No memory leaks (run for extended period)

4. ✅ **Cross-platform native libraries load**
   - Test on each platform if possible
   - Verify FFmpeg natives load correctly
   - Verify JOGL natives load correctly

5. ✅ **Existing tests pass**
   - Run `./gradlew test`
   - Update test dependencies if needed (PowerMock may have Java 17 issues)
   - Fix any test failures

6. ✅ **Docker image builds and runs**
   - Build docker image
   - Run headless with Xvfb
   - Verify websocket connectivity
   - Test video playback in container

**Estimated Complexity**: MEDIUM (depends on how many issues surface)

## Data Flow Verification

**Cannot verify** - project does not build. Once build succeeds:

| Flow | Input | Process | Store | Retrieve | Display |
|------|-------|---------|-------|----------|---------|
| Video → LED | ⏸️ | ⏸️ | ⏸️ | ⏸️ | ⏸️ |
| JOGL Init | ⏸️ | ⏸️ | - | - | ⏸️ |
| Native Libs | ⏸️ | - | - | - | - |

**⏸️** = Blocked by build failure

## Ambiguities Found

| Area | Question Not Answered | How LLM Guessed | Impact |
|------|----------------------|-----------------|--------|
| Groovy 4.x modules | Which Groovy 4.x modules are actually needed? | Used `groovy-all` (no longer exists in 4.x) | Build will fail - need to specify individual modules or use BOM |
| Processing 4 API | What are the breaking changes in Processing 4.x API? | Assumed compatible | May require code changes when attempted |
| Test framework | Should tests be updated to JUnit 5? | Left as JUnit 4 | JUnit 4 works on Java 17, but JUnit 5 is more idiomatic |
| Dependency versions | Which exact versions of updated dependencies? | None specified yet | Need to research Java 17 compatible versions |

## Test Suite Assessment

**Cannot run tests** - project does not build.

**Existing test files found**:
- `src/test/mapping/ReadDracoMappingTest.groovy`
- `src/test/integration/TesseractAppTest.groovy`
- `src/test/stores/ConfigStoreTest.groovy`

**Test dependencies**:
- JUnit 4.12
- Mockito 2.28.2
- PowerMock 2.0.2 ⚠️ (may have Java 17 issues - not actively maintained)

**Risk**: PowerMock is known to have issues with Java 17+ due to reflection restrictions. May need to:
- Remove PowerMock usage
- Switch to Mockito inline mocking (supports Java 17)
- Update test code to avoid static mocking

## LLM Blind Spot Findings

**Cannot verify** - blocked by build failure. Known risks:

- ❓ **Groovy 4.x compatibility**: Groovy source files may use deprecated syntax/APIs
- ❓ **Processing 4.x API changes**: Unknown until attempted
- ❓ **JOGL ARM64 natives**: Correctly extracted, but not verified to load
- ❓ **JavaCV memory leaks**: Frame grabbers must be properly released (code looks OK)
- ❓ **PowerMock + Java 17**: Likely incompatible, tests may fail

## Implementation Assessment

| Component | Status | Evidence | Issues |
|-----------|--------|----------|--------|
| build.gradle | ❌ BROKEN | Groovy 2.5.6, deprecated syntax | Multiple blockers |
| Gradle wrapper | ❌ TOO OLD | 5.4.1 | No Java 17 support |
| JavaCVVideoClip | ✅ COMPLETE | src/main/clip/JavaCVVideoClip.groovy | Looks good - proper cleanup, pixel handling |
| Scene.java | ✅ COMPLETE | Line 109 uses JavaCVVideoClip | Clean integration |
| TesseractMain.java | ✅ COMPLETE | Removed video imports, movieEvent | Clean |
| VideoClip.groovy | ⚠️ OBSOLETE | Still exists | Should delete after verification |
| Processing core | ⚠️ PARTIAL | 3.5.4, not 4.x | Need Processing 4.x for ARM64 |
| JOGL natives | ⚠️ UNVERIFIED | Extracted to lib/ | Need runtime test |
| Docker | ❌ OUTDATED | Java 8, Ubuntu 16.04 | Need Java 17 update |

## Code Quality Inspection

**Grep for red flags**:
```bash
grep -r "TODO\|FIXME\|stub\|placeholder\|hardcoded" src/
```

**Findings in existing code** (not upgrade-related):
- `src/main/clip/AbstractClip.java:44` - Comment: `// This is used & overridden by VideoClip`
- `src/main/model/Channel.java:29` - Comment: `//this logic is duplicated in Scene, it should be abstracted`
- Various TODOs in application logic (unrelated to upgrade)

**No hardcoded paths or obvious stubs in upgrade-related code.**

## Recommendations

### IMMEDIATE (Before Any Other Work)

**1. Unblock the build** - Upgrade Gradle and Groovy (Phase 1)
   - Without this, **nothing else can be tested**
   - Estimated effort: 2-4 hours
   - Risk: MEDIUM (Groovy 4.x may have breaking changes)

**2. Create a working branch**
   - Current changes are on `master` with modified files
   - Create `groovy4-java17-upgrade` branch
   - Commit current progress as "WIP: video library replacement"

### NEXT STEPS (After Build Works)

**3. Smoke test on Java 17**
   - Verify app launches
   - Check JOGL initialization
   - Test basic clip (non-video)
   - **THEN** tackle Processing 4.x

**4. Upgrade Processing 3.5.4 → 4.3** (Phase 2)
   - This is the **original goal** (ARM64 JOGL support)
   - Can only be tested once build succeeds

**5. Update Docker** (Phase 4)
   - Required for production deployment
   - Can be done in parallel with testing

### AVOID

- ❌ Don't upgrade dependencies piecemeal - do Gradle/Groovy together
- ❌ Don't delete `VideoClip.groovy` until JavaCVVideoClip is verified working
- ❌ Don't skip testing JOGL on ARM64 - that's the whole point
- ❌ Don't assume Processing 4.x API is compatible - verify first

## What Could Not Be Verified

| Item | Why | User Can Check |
|------|-----|----------------|
| **JavaCVVideoClip works** | Project doesn't build | Fix build, run app, load video, verify playback |
| **JOGL ARM64 natives load** | Can't run application | Fix build, run on Apple Silicon Mac, check for native library errors |
| **Processing 4.x compatibility** | Not upgraded yet | After Processing 4.x upgrade, test all PApplet API calls |
| **Cross-platform native libraries** | No test environment | Test on Linux x86_64, Linux ARM, macOS Intel if available |
| **Docker build** | Docker still uses Java 8 | After Docker update, build image and run container |
| **Tests pass** | Can't run tests | After build fix, run `./gradlew test`, fix failures |
| **PowerMock Java 17 compatibility** | Can't run tests | May need to remove PowerMock, use Mockito inline instead |
| **Memory leaks in JavaCV** | Can't run app | Run app for 30+ minutes with video playing, monitor memory |

## Risk Assessment

### HIGH RISK ⚠️
- **Groovy 4.x breaking changes**: Unknown scope until attempted
- **Processing 4.x API changes**: Could require significant refactoring
- **PowerMock incompatibility**: May need test refactoring

### MEDIUM RISK ⚡
- **Dependency version conflicts**: Some old libraries may conflict with Java 17
- **JOGL ARM64 runtime issues**: Natives extracted but not tested
- **Docker build changes**: Native libraries may need different paths

### LOW RISK ✓
- **Gradle syntax update**: Mechanical find/replace
- **JavaCV integration**: Code looks solid
- **Build configuration**: Well understood

## Workflow Recommendation

- [x] **PAUSE** - Critical ambiguities need clarification before proceeding

## Clarification Needed Before Proceeding

### Question 1: Groovy 4.x Module Selection
**Context**: Groovy 4.x removed `groovy-all` artifact. Must specify individual modules.

**How it was guessed**: Previous work assumed `groovy-all` still exists.

**Options**:
- **Option A**: Use Groovy BOM (Bill of Materials) with minimal modules
  ```gradle
  implementation platform('org.apache.groovy:groovy-bom:4.0.23')
  implementation 'org.apache.groovy:groovy'
  ```
  **Tradeoffs**: Minimal footprint, may need to add modules later if we use advanced features

- **Option B**: Use all common modules
  ```gradle
  implementation platform('org.apache.groovy:groovy-bom:4.0.23')
  implementation 'org.apache.groovy:groovy'
  implementation 'org.apache.groovy:groovy-json'
  implementation 'org.apache.groovy:groovy-templates'
  ```
  **Tradeoffs**: Larger footprint, less likely to hit missing module errors

- **Option C**: Analyze codebase, include only used modules
  **Tradeoffs**: Most precise, requires code analysis

**Impact of wrong choice**: Build may fail with "missing class" errors if we don't include a needed module.

**RECOMMENDATION**: Start with Option A (minimal), add modules as needed based on compile errors.

### Question 2: Gradle Version Target
**Context**: Need Gradle 7.3+ for Java 17, but newer versions available.

**Options**:
- **Option A**: Gradle 7.6.4 (last 7.x version)
  **Tradeoffs**: Stable, well-tested, conservative choice

- **Option B**: Gradle 8.5 (current stable)
  **Tradeoffs**: Latest features, better Java 17 support, may have new deprecations

**Impact of wrong choice**: Minimal - both work with Java 17. 8.x may require additional syntax updates.

**RECOMMENDATION**: Gradle 7.6.4 (conservative, minimizes migration scope)

### Question 3: Test Framework Modernization
**Context**: PowerMock likely incompatible with Java 17. Tests will probably fail.

**Options**:
- **Option A**: Keep JUnit 4 + Mockito, remove PowerMock usage
  **Tradeoffs**: Minimal change, may require test refactoring for static mocks

- **Option B**: Upgrade to JUnit 5 + Mockito, remove PowerMock
  **Tradeoffs**: Modern stack, better Java 17 support, more refactoring

- **Option C**: Deal with it later if tests actually fail
  **Tradeoffs**: Defer work, may waste time debugging PowerMock issues

**Impact of wrong choice**: Option C risks wasting time on PowerMock compatibility issues.

**RECOMMENDATION**: Option A (keep JUnit 4, remove PowerMock) - smallest scope, can upgrade JUnit later.

---

## Summary

**Critical Blocker**: Project **cannot build** on Java 17 due to Groovy 2.5.6 + Gradle 5.4.1 incompatibility.

**Path Forward**:
1. ✅ Clarify Groovy 4.x module selection (recommend: minimal BOM)
2. ✅ Upgrade Gradle 5.4.1 → 7.6.4
3. ✅ Upgrade Groovy 2.5.6 → 4.0.23
4. ✅ Replace deprecated Gradle syntax (`compile` → `implementation`)
5. ✅ Verify build succeeds and fix Groovy 4.x compatibility issues
6. ✅ THEN tackle Processing 4.x upgrade
7. ✅ Test JavaCV video playback
8. ✅ Update Docker for Java 17
9. ✅ Validate on all target platforms

**Current state**: ~15% complete. Video library replacement code written but unverified. Build is completely broken.

**Next action**: Get user confirmation on Groovy/Gradle versions, then execute Phase 1 (unblock build).
