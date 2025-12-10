# Implementation Plan: Complete Processing Dependency Removal

**Generated**: 2025-12-10-023058
**Source STATUS**: STATUS-processing-removal.md (2025-12-10)
**Planner**: status-planner
**Project**: tesseract_java - Complete Processing Removal Implementation Plan

---

## Provenance

- **Source STATUS File**: `.agent_planning/STATUS-processing-removal.md`
- **Source STATUS Date**: 2025-12-10
- **Specification**: Project architecture (LibGDX migration complete, JavaCV video operational)
- **Plan Generation**: 2025-12-10-023058

---

## Executive Summary

**Current State**: LibGDX migration ~60% complete - core architecture and video playback fully migrated, but old Processing code still present as dead code.

**Total Work**: 6 phases, 33 work items
**Estimated Complexity**: MEDIUM (10-15 hours based on STATUS assessment)
**Risk Level**: LOW-MEDIUM (incremental approach with testing after each phase)

**Key Evidence from STATUS**:
- TesseractApp.java (LibGDX) replaces TesseractMain.java (Processing) ✅
- JavaCVVideoClip.groovy replaces VideoClip.groovy ✅
- Scene.java already uses JavaCVVideoClip (line 109) ✅
- TesseractLauncher uses TesseractApp, not TesseractMain ✅
- Old Processing files are DEAD CODE - not in execution path

**Success Criteria**:
1. No Processing imports in src/main
2. No Processing jars in lib/
3. Application runs in GUI mode (LibGDX)
4. Application runs in headless mode
5. Video clips work (JavaCV)
6. All tests pass
7. Fat jar builds and runs

**Recommended Approach**: Execute phases sequentially with build/test verification after each phase. This is cleanup work with low regression risk since we're removing code not in the execution path.

---

## Phase 1: Clean Commented Processing Code

**Priority**: P0 (Critical - foundation)
**Status**: Not Started
**Complexity**: SMALL (1-2 hours)
**Dependencies**: None
**Spec Reference**: STATUS § Current State Analysis → What Still References Processing
**Risk**: VERY LOW (deleting commented code)

### Description

Remove all commented-out Processing code from active source files. This code is dead weight - it references old Processing API (PApplet, VideoClip) that has been replaced by LibGDX/JavaCV architecture.

Evidence from STATUS:
- SolidColorClip.java lines 47-56: Commented PApplet.HSB code
- UDPModel.java lines 266-268: Commented PApplet.unhex() calls (own unhex() already implemented)
- Channel.java lines 28-61: Entire commented constructNewClip() method referencing VideoClip
- Playlist.groovy line 3: Unused import for VideoClip

### Acceptance Criteria

- [ ] **Unit**: SolidColorClip.java has no commented Processing code (lines 47-56 deleted)
- [ ] **Unit**: UDPModel.java has no PApplet references (lines 266-268 deleted)
- [ ] **Unit**: Channel.java has no commented methods (lines 28-61 deleted)
- [ ] **Unit**: Playlist.groovy does not import VideoClip (line 3 deleted)
- [ ] **Build**: `./gradlew clean build` succeeds
- [ ] **Verification**: Grep confirms no commented PApplet references remain: `grep -r "PApplet" src/main`

### Technical Notes

- SolidColorClip already works with RGB only, HSB mode was never restored to production
- UDPModel already has custom unhex() method (lines 72-74), PApplet.unhex was never used
- Channel.constructNewClip() logic is duplicated in Scene.java (per comment in code)
- Playlist never actually used VideoClip import (dead import)

---

## Phase 2: Delete Dead Source Files

**Priority**: P0 (Critical - foundation)
**Status**: Not Started
**Complexity**: SMALL (2-3 hours)
**Dependencies**: Phase 1
**Spec Reference**: STATUS § What's Dead Code (CAN DELETE)
**Risk**: LOW (files not in execution path, but need verification first)

### Description

Delete Processing-based source files that have been completely replaced by LibGDX architecture. STATUS evaluation confirms these files are DEAD CODE:
- TesseractMain.java replaced by TesseractApp.java (TesseractLauncher uses TesseractApp)
- OnScreen.java replaced by LibGDXRenderer/HeadlessRenderer
- VideoClip.groovy replaced by JavaCVVideoClip.groovy (Scene.java line 109 uses JavaCVVideoClip)

**CRITICAL**: Must verify no references exist before deletion (search step included).

### Acceptance Criteria

- [ ] **Pre-verification**: Grep confirms no src/main references to TesseractMain: `grep -r "TesseractMain" src/main`
- [ ] **Pre-verification**: Grep confirms no src/main references to OnScreen: `grep -r "OnScreen" src/main`
- [ ] **Pre-verification**: Grep confirms no src/main references to VideoClip (except JavaCVVideoClip): `grep -r "VideoClip" src/main`
- [ ] **Delete**: `src/main/app/TesseractMain.java` removed (243 lines)
- [ ] **Delete**: `src/main/app/OnScreen.java` removed (205 lines)
- [ ] **Delete**: `src/main/clip/VideoClip.groovy` removed (140 lines)
- [ ] **Build**: `./gradlew clean build` succeeds (confirms no active code references deleted files)
- [ ] **Note**: Test failures expected (tests still mock TesseractMain - Phase 3 will fix)

### Technical Notes

- Total dead code removal: 588 lines
- TesseractLauncher.java is the entry point - verify it uses TesseractApp
- Test files (MockMain.groovy, TestUtil.groovy) still reference TesseractMain - expected to break, fixed in Phase 3
- If grep finds unexpected references in src/main, STOP and investigate before deleting

---

## Phase 3: Update Test Infrastructure

**Priority**: P1 (High - required for MVP)
**Status**: Not Started
**Complexity**: MEDIUM (3-4 hours)
**Dependencies**: Phase 2 (TesseractMain deletion)
**Spec Reference**: STATUS § Test File Updates
**Risk**: MEDIUM (tests may need significant refactoring)

### Description

Update test infrastructure to use TesseractApp instead of TesseractMain. Tests currently extend/mock the old Processing-based TesseractMain class which will be deleted in Phase 2. Need to migrate to LibGDX-based TesseractApp.

Files requiring updates:
- MockMain.groovy (extends TesseractMain → extend TesseractApp)
- TestUtil.groovy (mocks TesseractMain.getMain() → mock TesseractApp.getMain())
- ConfigStoreTest.groovy (may use MockMain)
- ReadDracoMappingTest.groovy (may use MockMain)
- TesseractAppTest.groovy (currently @Ignored, needs implementation)

### Acceptance Criteria

- [ ] **Unit**: MockMain.groovy extends TesseractApp with headless constructor
- [ ] **Unit**: MockMain implements color(int r, int g, int b) matching TesseractApp API
- [ ] **Unit**: TestUtil.groovy mocks TesseractApp.class instead of TesseractMain.class
- [ ] **Unit**: TestUtil.mockTesseractApp() method created (replaces mockTesseractMain)
- [ ] **Review**: ConfigStoreTest.groovy updated if it uses MockMain/TesseractMain
- [ ] **Review**: ReadDracoMappingTest.groovy updated if it uses MockMain/TesseractMain
- [ ] **Implementation**: TesseractAppTest @Ignore annotation removed
- [ ] **Implementation**: TesseractAppTest tests TesseractLauncher with --headless flag
- [ ] **Test**: `./gradlew test` passes all tests
- [ ] **E2E**: Tests actually exercise TesseractApp behavior (not just mocked stubs)

### Technical Notes

MockMain.groovy migration pattern (from STATUS):
```groovy
// OLD (Processing-based)
import app.TesseractMain
class MockMain extends TesseractMain {
  public int color() { return 0 }
}

// NEW (LibGDX-based)
import app.TesseractApp
class MockMain extends TesseractApp {
  MockMain() {
    super(true, 100, 100) // headless, 100x100 window
  }
  @Override
  public int color(int r, int g, int b) { return 0 }
}
```

TestUtil.groovy changes:
- Line 17: `MockedStatic<TesseractMain>` → `MockedStatic<TesseractApp>`
- Line 20: Method name `mockTesseractMain()` → `mockTesseractApp()`
- Line 25: `TesseractMain.class` → `TesseractApp.class`
- Line 26: `TesseractMain.getMain()` → `TesseractApp.getMain()`

**WARNING**: Tests might pass but not provide meaningful coverage. Review test assertions to ensure they verify actual behavior.

---

## Phase 4: Delete Processing Libraries

**Priority**: P1 (High - required for MVP)
**Status**: Not Started
**Complexity**: SMALL (1 hour)
**Dependencies**: Phase 2 (source files deleted), Phase 3 (tests pass)
**Spec Reference**: STATUS § Processing Libraries to Remove
**Risk**: LOW (replacements already operational)

### Description

Delete Processing library directories from lib/ folder. These libraries are no longer needed:
- `processing-core/` - Replaced by LibGDX core
- `video/` - Replaced by JavaCV
- `jogl-4.0/` - LibGDX uses LWJGL3, not JOGL
- `jogl-2.3.2-patched/` - Already deleted (per git status)

**KEEP** lib/udp/ - This is NOT a Processing library, it's a standalone Java UDP library (hypermedia.net.UDP) that happens to be distributed with Processing but has no Processing dependencies.

### Acceptance Criteria

- [ ] **Delete**: `lib/processing-core/` directory removed
- [ ] **Delete**: `lib/video/` directory removed (Processing video + GStreamer jars)
- [ ] **Delete**: `lib/jogl-4.0/` directory removed (jogl-all.jar, gluegen-rt.jar)
- [ ] **Verify**: `lib/udp/` directory KEPT (not Processing-specific)
- [ ] **Verify**: `lib/PixelPusher/` directory KEPT (hardware library)
- [ ] **Build**: `./gradlew clean build` succeeds
- [ ] **Verification**: No Processing jars in lib/ tree: `find lib -name "*processing*" -o -name "*jogl*"`

### Technical Notes

Library size being removed:
- processing-core/core.jar: ~1 MB
- video/ directory: Multiple jars (video.jar, gst1-java-core, jna.jar) plus examples
- jogl-4.0/ directory: jogl-all.jar, gluegen-rt.jar

LibGDX native libraries:
- LibGDX uses LWJGL3 (bundled in gdx-backend-lwjgl3 dependency)
- LWJGL3 provides OpenGL bindings for all platforms
- JOGL is Processing's OpenGL library, incompatible with LibGDX

UDP library clarification (from STATUS § Processing UDP Library Status):
- Original source: http://ubaa.net/shared/processing/udp/
- Package: hypermedia.net.UDP
- Can be used in any Java application
- Used by UDPModel.java (actively used for LED output)

---

## Phase 5: Clean Build Configuration

**Priority**: P1 (High - required for MVP)
**Status**: Not Started
**Complexity**: SMALL (1 hour)
**Dependencies**: Phase 4 (libraries deleted)
**Spec Reference**: STATUS § Build Configuration Cleanup
**Risk**: LOW (build system changes only)

### Description

Remove obsolete Gradle tasks that download Processing libraries and update dependency configuration. The build.gradle currently has multiple tasks downloading Processing core, video library, and JOGL - all of which are replaced by LibGDX/JavaCV.

Gradle tasks to delete:
- Lines 108-122: `downloadProcessingVideoLibrary`, `unzipProcessingVideoLibrary`
- Lines 140-171: `downloadProcessing4CoreLibrary`, `untarProcessing4CoreLibrary`
- Lines 174-195: `downloadProcessingCoreLibrary`, `untarProcessingCoreLibrary` (duplicate)
- Lines 197-201: `downloadJoglJar`

Dependencies to remove/update:
- Line 85: `implementation 'org.processing:serial:3.3.7'` (not used in src/main per STATUS check)
- Line 82: `implementation fileTree(dir: 'lib', include: ['**/*.jar'])` needs specificity

### Acceptance Criteria

- [ ] **Delete**: Processing video library download tasks removed (lines 108-122)
- [ ] **Delete**: Processing 4 core download tasks removed (lines 140-171)
- [ ] **Delete**: Processing core download tasks removed (lines 174-195)
- [ ] **Delete**: JOGL download task removed (lines 197-201)
- [ ] **Delete**: Processing serial dependency removed (line 85)
- [ ] **Update**: Line 82 changed to `implementation fileTree(dir: 'lib', include: ['PixelPusher/**/*.jar', 'udp/**/*.jar'])`
- [ ] **Build**: `./gradlew clean build` succeeds
- [ ] **Build**: `./gradlew tasks` shows no Processing-related tasks
- [ ] **Verification**: No processing.serial imports in codebase: `grep -r "import processing.serial" src/`

### Technical Notes

File tree dependency options (from STATUS):
- **Option A**: Be specific: `include: ['PixelPusher/**/*.jar', 'udp/**/*.jar']`
- **Option B**: Delete Processing jars and keep wildcard
- **Recommended**: Option A (explicit is better than implicit)

Processing serial library check (from STATUS):
- STATUS already verified: No files in src/main use `processing.serial`
- Old TesseractMain might have used it, but TesseractApp does not
- Safe to remove

UDP library Gradle tasks:
- **KEEP** lines 124-138: `downloadUDPLibrary`, `unzipUDPLibrary`
- UDP library is actively used by UDPModel.java
- Not Processing-specific (standalone library)

---

## Phase 6: Runtime Verification & Testing

**Priority**: P0 (Critical - validation)
**Status**: Not Started
**Complexity**: MEDIUM (2-3 hours)
**Dependencies**: All previous phases
**Spec Reference**: STATUS § Verification, § Success Criteria
**Risk**: CRITICAL (final validation of complete migration)

### Description

Comprehensive runtime testing to verify Processing removal is complete and application functions correctly with LibGDX/JavaCV architecture on all target platforms. This phase validates the entire migration.

Testing dimensions:
- GUI mode (LibGDX windowing)
- Headless mode (no OpenGL dependencies)
- Video playback (JavaCV)
- Build artifacts (fat jar)
- Cross-platform compatibility

### Acceptance Criteria

- [ ] **Unit**: All unit tests pass: `./gradlew test`
- [ ] **Build**: Fat jar builds successfully: `./gradlew fatJar`
- [ ] **Build**: Fat jar executes: `java -jar build/libs/TesseractFatJar.jar`
- [ ] **Runtime**: Application launches in GUI mode (LibGDX window appears)
- [ ] **Runtime**: Application runs in headless mode without OpenGL errors
- [ ] **Runtime**: JavaCV video clip playback works (no GStreamer/Processing video errors)
- [ ] **Verification**: No Processing imports in src/main: `grep -r "import processing" src/main`
- [ ] **Verification**: No Processing jars in lib/: `find lib -name "*processing*"`
- [ ] **E2E**: Application runs for >5 minutes without crashes or native library errors
- [ ] **Cross-platform**: Tested on macOS ARM64 (Apple Silicon) - PRIMARY
- [ ] **Cross-platform**: Tested on macOS x86_64 (Intel) - OPTIONAL if available
- [ ] **Cross-platform**: Tested on Linux x86_64 - OPTIONAL if available
- [ ] **Cross-platform**: Tested on Linux ARM64 (RPi 4) - OPTIONAL if available

### Technical Notes

Platform verification priorities (from STATUS § Known Ambiguities):
- **macOS ARM64**: PRIMARY (developer platform)
- **macOS x86_64**: SECONDARY (if available)
- **Linux x86_64**: SECONDARY (if available)
- **Raspberry Pi ARM64**: OPTIONAL (requires hardware)

JOGL vs LWJGL3 concern (from STATUS § Known Ambiguities and Risks):
- **Question**: Will LibGDX natives work on all platforms, especially Raspberry Pi?
- **Impact**: Application might fail to launch on some platforms
- **Mitigation**: Test on each target platform OR provide clear error messages
- **Status**: NEEDS VERIFICATION - Especially on ARM platforms

Testing scenarios:
1. **Headless mode**: `java -jar TesseractFatJar.jar --headless` (should start with no GUI, no OpenGL errors)
2. **GUI mode**: `java -jar TesseractFatJar.jar` (LibGDX window should appear)
3. **Video playback**: Load a show with video clips, verify JavaCV playback works
4. **Long-running stability**: Leave running for 5+ minutes to check for native library leaks

What cannot be verified without hardware (from STATUS):
- RPi LWJGL3 support (no Raspberry Pi hardware)
- Long-running stability (need runtime test environment - but 5min smoke test feasible)
- Video playback on ARM (need ARM test environment)

---

## Dependency Graph

```
Phase 1: Clean Commented Code (INDEPENDENT - can start immediately)
    └─> Phase 2: Delete Dead Source Files
            └─> Phase 3: Update Test Infrastructure
                    └─> Phase 4: Delete Processing Libraries
                            └─> Phase 5: Clean Build Configuration
                                    └─> Phase 6: Runtime Verification
```

**Critical Path**: Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5 → Phase 6
**Parallelizable**: None (each phase builds on previous)
**Recommended Approach**: Execute sequentially, verify build after each phase

---

## Risk Assessment

### Low Risk Items ✅

1. **Phase 1: Commented code removal** - Obviously dead code, no runtime impact
2. **Phase 4: Library cleanup** - JavaCV/LibGDX already operational
3. **Phase 5: Gradle task cleanup** - Build system only, not runtime

### Medium Risk Items ⚠️

4. **Phase 2: Dead source file deletion** - Need verification of no hidden references (grep included)
5. **Phase 3: Test updates** - Tests may need significant refactoring, coverage quality uncertain
6. **Phase 6: Cross-platform verification** - May uncover LWJGL3 compatibility issues on ARM

### Items Needing User Verification 🔍

7. **Processing serial library** - STATUS verified not used, but user should confirm no hardware serial communication needed
8. **JOGL vs LWJGL3 on RPi** - Cannot verify without Raspberry Pi hardware (LibGDX should work, but untested)
9. **Test coverage quality** - Tests might pass but not provide meaningful coverage (need review)
10. **UDP library dependency** - Currently jar in lib/, could migrate to Maven (low priority)

### Mitigation Strategies

- **Grep verification before deletion**: Each delete phase includes grep step to confirm no references
- **Incremental build testing**: Run `./gradlew clean build` after each phase
- **Test quality review**: Phase 3 includes manual review of test assertions
- **Platform testing**: Phase 6 includes cross-platform verification (to extent hardware available)
- **Rollback capability**: Git commit after each phase for easy rollback if issues discovered

---

## Success Metrics

### Must Have (Blocking) ✅

1. ✅ No Processing imports in any src/main files
2. ✅ No Processing jars in lib/ directory
3. ✅ Application runs in GUI mode (LibGDX window)
4. ✅ Application runs in headless mode (no OpenGL errors)
5. ✅ Video clips play correctly (JavaCV)
6. ✅ All tests pass (`./gradlew test`)
7. ✅ Fat jar builds and runs

### Should Have (Important) 📋

8. ⚠️ Tests use TesseractApp, not deprecated TesseractMain
9. ⚠️ Build is clean (no obsolete Gradle tasks)
10. ⚠️ Cross-platform verification on at least 2 platforms (macOS ARM64 + one other)

### Nice to Have (Optional) 💡

11. 🔍 Documentation updated (if any Processing references exist)
12. 🔍 README updated to reflect LibGDX usage
13. 🔍 Remove Processing installation instructions

---

## Known Issues and Ambiguities

### 1. JOGL vs LWJGL3 Native Library Loading 🔍

**From STATUS § Known Ambiguities and Risks**

**Question**: Will LibGDX natives work on all platforms, especially Raspberry Pi?
**Impact**: Application might fail to launch on some platforms
**Mitigation**: Test on each target platform OR provide clear error messages
**Status**: NEEDS VERIFICATION - Especially on ARM platforms

**Background**:
- Processing uses JOGL for OpenGL
- LibGDX uses LWJGL3 for OpenGL
- Both should work, but native library loading differs
- Raspberry Pi ARM64 is highest risk platform

**User Action**: If targeting Raspberry Pi, test Phase 6 on actual hardware before considering migration complete.

### 2. Test Coverage Quality After Mock Changes ⚠️

**From STATUS § Known Ambiguities and Risks**

**Question**: Will tests still provide meaningful coverage after changing from TesseractMain to TesseractApp?
**Impact**: Tests might pass but not actually test anything useful (tautological tests)
**Mitigation**: Review test assertions in Phase 3, ensure they test actual behavior
**Status**: NEEDS ATTENTION during Phase 3

**Background**:
- MockMain currently extends TesseractMain (simple mocking)
- TesseractApp has different API surface
- Tests might need significant refactoring beyond simple class swap

**User Action**: During Phase 3, review test assertions - don't just make tests pass, ensure they're still meaningful.

### 3. UDP Library Dependency Management 📦

**From STATUS § Known Ambiguities and Risks**

**Question**: Should UDP library be migrated to Maven dependency instead of jar in lib/?
**Impact**: Cleaner build, but need to find correct Maven coordinates
**Research needed**: Is hypermedia.net.UDP available in Maven Central?
**Status**: LOW PRIORITY - Keep as-is for now

**Background**:
- Current: `lib/udp/library/udp.jar` included via fileTree
- Ideal: Maven dependency like other libraries
- Unknown: Maven coordinates (library is old, may not be in Maven Central)

**User Action**: Post-migration optimization - research if UDP library available in Maven, migrate if found.

### 4. Processing Serial Library Confirmation ✅

**From STATUS § Known Ambiguities and Risks**

**Question**: Is `processing.serial` actually used for hardware communication?
**Impact**: If used, need alternative (RXTX, jSerialComm, etc.)
**Status**: RESOLVED - STATUS verified not used in src/main, safe to remove

**Background**:
- build.gradle line 85: `implementation 'org.processing:serial:3.3.7'`
- STATUS grep confirmed: No `import processing.serial` in src/main
- Old TesseractMain might have used it, but TesseractApp does not

**User Action**: If hardware serial communication is needed in future, use jSerialComm (modern alternative).

---

## Recommended Execution Order

### Sprint 1: Code Cleanup (Low Risk)
**Effort**: ~3-5 hours
**Goal**: Remove dead code and commented references

1. Execute Phase 1 (Clean commented code)
2. Build verification: `./gradlew clean build`
3. Execute Phase 2 (Delete dead source files)
4. Build verification: `./gradlew clean build` (expect test failures)
5. Git commit: "Remove Processing dead code and commented references"

### Sprint 2: Test Migration (Medium Risk)
**Effort**: ~3-4 hours
**Goal**: Update test infrastructure to LibGDX

6. Execute Phase 3 (Update test infrastructure)
7. Test verification: `./gradlew test`
8. Review test coverage quality
9. Git commit: "Migrate tests from TesseractMain to TesseractApp"

### Sprint 3: Library & Build Cleanup (Low Risk)
**Effort**: ~2 hours
**Goal**: Remove Processing libraries and build tasks

10. Execute Phase 4 (Delete Processing libraries)
11. Build verification: `./gradlew clean build`
12. Execute Phase 5 (Clean build configuration)
13. Build verification: `./gradlew clean build`
14. Git commit: "Remove Processing libraries and build tasks"

### Sprint 4: Verification (Critical)
**Effort**: ~2-3 hours
**Goal**: Comprehensive runtime testing

15. Execute Phase 6 (Runtime verification)
16. Test all scenarios (GUI, headless, video)
17. Cross-platform testing (to extent hardware available)
18. Git commit: "Verify complete Processing removal"

**Total Effort**: 10-15 hours (matches STATUS estimate)

---

## Files Modified/Deleted Summary

### Files to DELETE (7 items)

**Source Files** (588 lines):
1. `src/main/app/TesseractMain.java` (243 lines) - Phase 2
2. `src/main/app/OnScreen.java` (205 lines) - Phase 2
3. `src/main/clip/VideoClip.groovy` (140 lines) - Phase 2

**Library Directories**:
4. `lib/processing-core/` - Phase 4
5. `lib/video/` - Phase 4
6. `lib/jogl-4.0/` - Phase 4

**Build Configuration**:
7. `build.gradle` lines 85, 108-122, 140-171, 174-195, 197-201 - Phase 5

### Files to MODIFY (9 items)

**Source Code Cleanup** (Phase 1):
1. `src/main/clip/SolidColorClip.java` - Delete lines 47-56 (commented HSB code)
2. `src/main/output/UDPModel.java` - Delete lines 266-268 (commented PApplet.unhex)
3. `src/main/model/Channel.java` - Delete lines 28-61 (commented constructNewClip)
4. `src/main/show/Playlist.groovy` - Delete line 3 (unused VideoClip import)

**Test Infrastructure** (Phase 3):
5. `src/test/testUtil/MockMain.groovy` - Extend TesseractApp instead of TesseractMain
6. `src/test/testUtil/TestUtil.groovy` - Mock TesseractApp instead of TesseractMain
7. `src/test/stores/ConfigStoreTest.groovy` - Review/update if uses MockMain
8. `src/test/mapping/ReadDracoMappingTest.groovy` - Review/update if uses MockMain
9. `src/test/integration/TesseractAppTest.groovy` - Remove @Ignore, implement tests

**Build Configuration** (Phase 5):
10. `build.gradle` - Delete obsolete tasks, update line 82 fileTree

### Files to KEEP (3 items)

1. `lib/udp/` - Standalone UDP library (not Processing-specific)
2. `lib/PixelPusher/` - Hardware library (independent)
3. JavaCV dependencies - Already in build.gradle (lines 52-76)

---

## Next Steps

### Immediate Actions (Can Start Now)

1. **Begin Phase 1**: Delete commented code from 4 files
   - SolidColorClip.java
   - UDPModel.java
   - Channel.java
   - Playlist.groovy

2. **Verification**: Run grep to confirm no unexpected Processing references:
   ```bash
   grep -r "import processing" src/main
   grep -r "PApplet" src/main
   grep -r "TesseractMain" src/main
   grep -r "OnScreen" src/main
   grep -r "VideoClip" src/main | grep -v JavaCVVideoClip
   ```

3. **Build Test**: Verify current build works: `./gradlew clean build`

### After Phase 1 Complete

4. **Execute Phase 2**: Delete dead source files (with grep verification first)
5. **Build Test**: `./gradlew clean build` (expect test failures - normal)
6. **Git Commit**: Checkpoint after Phases 1-2

### After Phase 2 Complete

7. **Execute Phase 3**: Update test infrastructure
8. **Test Verification**: `./gradlew test` should pass
9. **Coverage Review**: Ensure tests are meaningful
10. **Git Commit**: Checkpoint after test migration

### After Phase 3 Complete

11. **Execute Phases 4-5**: Library and build cleanup
12. **Build Verification**: `./gradlew clean build` and `./gradlew fatJar`
13. **Git Commit**: Checkpoint after cleanup

### Final Steps

14. **Execute Phase 6**: Comprehensive runtime verification
15. **Cross-Platform Testing**: Test on available platforms
16. **Documentation**: Update README if needed
17. **Final Git Commit**: "Complete Processing dependency removal"

---

## Blockers and Questions

### Pre-Implementation Checks

1. ✅ **Confirm migration readiness**: Has LibGDX migration been tested and validated?
   - **STATUS Evidence**: TesseractApp exists, TesseractLauncher uses it, JavaCVVideoClip operational
   - **Answer**: YES - migration ~60% complete, remainder is dead code cleanup

2. ⚠️ **Confirm no hardware dependencies**: Does application use Processing serial library for hardware communication?
   - **STATUS Evidence**: No `import processing.serial` found in src/main
   - **Answer**: NO - safe to remove processing:serial dependency

3. 🔍 **Confirm target platforms**: Which platforms need verification in Phase 6?
   - **User Input Needed**: Primary target is macOS ARM64, but are Linux x86_64 or RPi deployments critical?
   - **Default**: Test macOS ARM64 only, document LWJGL3 compatibility as "should work" for other platforms

### During Implementation

4. **If grep finds unexpected references in Phase 2**: STOP and investigate before deleting files
5. **If tests fail after Phase 3 updates**: Review test logic, don't just make them pass
6. **If LWJGL3 fails on platform**: Document as platform limitation, may need platform-specific natives

---

## Appendix: Evidence from STATUS Report

### Execution Path Verification

**TesseractLauncher → TesseractApp** (not TesseractMain):
- Entry point: TesseractLauncher.java uses TesseractApp
- Old entry: TesseractMain is NOT in execution path
- Conclusion: TesseractMain is dead code

**Scene.java → JavaCVVideoClip** (not VideoClip):
- Line 109: `new JavaCVVideoClip()` instantiation
- Old import: VideoClip.groovy is NOT in execution path
- Conclusion: VideoClip.groovy is dead code

**TesseractApp → LibGDXRenderer/HeadlessRenderer** (not OnScreen):
- TesseractApp implements ApplicationListener (LibGDX)
- OnScreen is only instantiated by TesseractMain
- TesseractMain is dead code → OnScreen is dead code
- Conclusion: OnScreen.java is dead code

### Library Replacement Evidence

**Processing Core → LibGDX**:
- TesseractApp extends ApplicationListener (LibGDX interface)
- No PApplet references in TesseractApp
- LibGDX dependencies in build.gradle (lines 45-50)

**Processing Video → JavaCV**:
- JavaCVVideoClip.groovy fully implemented (228 lines)
- Uses FFmpeg via JavaCV for cross-platform playback
- No Processing video library dependencies
- Scene.java uses JavaCVVideoClip (line 109)

**JOGL → LWJGL3**:
- LibGDX uses LWJGL3 (bundled in gdx-backend-lwjgl3)
- JOGL only referenced in TesseractMain (dead code)
- LWJGL3 provides OpenGL bindings for all platforms

### Test File Evidence

**Test files still use TesseractMain** (expected failures after Phase 2):
- MockMain.groovy extends TesseractMain
- TestUtil.groovy mocks TesseractMain.getMain()
- TesseractAppTest.groovy has @Ignore with note "need to use TesseractApp"

**Conclusion**: Tests will break after Phase 2, fixed in Phase 3

---

**Plan generation complete. Ready for implementation.**
