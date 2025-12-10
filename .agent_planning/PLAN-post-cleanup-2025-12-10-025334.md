# Post-Processing Cleanup Plan

**Generated**: 2025-12-10-025334
**Planner**: status-planner
**Source STATUS**: STATUS-post-cleanup.md (2025-12-10)
**Project**: tesseract_java - Post-Processing Removal Cleanup

---

## Executive Summary

The Processing removal is fundamentally complete and successful. The codebase compiles cleanly with LibGDX, but cleanup work remains:

- **P0 Items**: 2 critical blockers (tests broken, Docker broken)
- **P1 Items**: 3 quality issues (outdated docs, stale build config, unused imports)
- **Total Phases**: 5 focused phases
- **Estimated Complexity**: MEDIUM (straightforward fixes, no architectural changes)

This plan focuses **exclusively on P0 and P1 items**. P2 polish work (TODOs, wildcards, logging, file cleanup) is deferred for later.

---

## Phase 1 - Fix Test Infrastructure [P0]

**Status**: Not Started
**Complexity**: SMALL (half-day)
**Dependencies**: None
**Spec Reference**: N/A (test infrastructure)
**STATUS Reference**: STATUS-post-cleanup.md § P0 - Must Fix #1

### Description

Replace the incompatible `system-rules:1.19.0` test library with a Java 17 compatible alternative. Current test failures (17/22 tests) are NOT code bugs—they're caused by the test library using reflection to access internal Java collections, which Java 17's module system blocks.

### Root Cause

```
java.lang.reflect.InaccessibleObjectException: Unable to make field
private final java.util.Map java.util.Collections$UnmodifiableMap.m accessible:
module java.base does not "opens java.util" to unnamed module
```

All failures are in `ConfigStoreTest` which uses the `EnvironmentVariables` JUnit rule to mock environment variables.

### Acceptance Criteria

- [ ] Replace `com.github.stefanbirkner:system-rules:1.19.0` with `org.junitpioneer:junit-pioneer:2.2.0` in build.gradle
- [ ] Update `ConfigStoreTest` to use JUnit Pioneer's `@SetEnvironmentVariable` annotation instead of `EnvironmentVariables` rule
- [ ] Update test imports from `org.junit.contrib.java.lang.system.*` to `org.junitpioneer.jupiter.*`
- [ ] Migrate affected tests from JUnit 4 style (`@Rule`) to JUnit Jupiter style (`@Test`, `@SetEnvironmentVariable`)
- [ ] All 22 tests pass: `./gradlew test` returns "BUILD SUCCESSFUL"
- [ ] Integration test still passes: `testCanStartApplicationHeadless`
- [ ] ConfigStore tests now pass: all 17 previously failing tests succeed

### Technical Notes

**Option Selected**: Use junit-pioneer (modern, Jupiter-based, actively maintained)

**Alternative Options Rejected**:
- System properties instead of env vars: Would require changing app code
- JVM args to open java.util module: Hacky workaround, not recommended

**Files to Modify**:
- `build.gradle:97` - Update test dependency
- `src/test/stores/ConfigStoreTest.groovy` - Update test annotations

**Migration Pattern**:
```groovy
// OLD (JUnit 4 + system-rules)
@Rule
public final EnvironmentVariables envVars = new EnvironmentVariables()

@Test
void testEnvVar() {
    envVars.set("KEY", "value")
    // test code
}

// NEW (JUnit Jupiter + junit-pioneer)
@Test
@SetEnvironmentVariable(key = "KEY", value = "value")
void testEnvVar() {
    // test code
}
```

---

## Phase 2 - Update Docker Configuration [P0]

**Status**: Not Started
**Complexity**: MEDIUM (1 day)
**Dependencies**: None (can run in parallel with Phase 1)
**Spec Reference**: N/A (deployment config)
**STATUS Reference**: STATUS-post-cleanup.md § P0 - Must Fix #2

### Description

Update Docker configuration to remove Processing library references and modernize the base image and Java version. Current Dockerfile will fail completely because it tries to copy deleted Processing libraries.

### Acceptance Criteria

- [ ] Update `docker/Dockerfile` FROM ubuntu:16.04 → ubuntu:22.04 (16.04 is EOL)
- [ ] Remove Processing library copy commands (lines 48, 52)
- [ ] Remove Processing unzip tasks from gradle command (line 56)
- [ ] Keep UDP library task: `unzipProcessingUdpLibrary` (UDP lib still needed)
- [ ] Update Java installation from Java 8 to Java 17
- [ ] Update gradle command to: `./gradlew unzipProcessingUdpLibrary fatJar` (only tasks that still exist)
- [ ] Docker image builds successfully: `docker/build.sh` completes without errors
- [ ] Built image can run headless: `docker run <image> --headless` starts successfully

### Technical Notes

**Files to Modify**:
- `docker/Dockerfile` - Remove Processing references, update base image and Java
- `docker/build.sh` - Verify script still works (may need no changes)

**Dockerfile Changes Needed**:

```dockerfile
# Remove these lines (Processing libraries no longer exist):
COPY files/jogamp-2.3.2-patched.jar ...  # Line 48
COPY files/processing-3.5.4-linux64.tgz ...  # Line 52

# Update gradle command (line 56):
# OLD: ./gradlew unzipProcessingVideoLibrary unzipProcessingUdpLibrary untarProcessingCoreLibrary downloadJoglJar fatJar
# NEW: ./gradlew unzipProcessingUdpLibrary fatJar

# Update base image:
FROM ubuntu:16.04  →  FROM ubuntu:22.04

# Update Java installation (find Java 17 packages for Ubuntu 22.04)
```

**UDP Library Note**: The UDP library (lib/udp/) is still needed despite having unused Processing imports. Those imports are harmless since Processing is not in the classpath.

**Verification Steps**:
1. Build image: `cd docker && ./build.sh`
2. Run container: `docker run <image-name> --headless`
3. Check logs for successful app start

---

## Phase 3 - Update README [P1]

**Status**: Not Started
**Complexity**: SMALL (half-day)
**Dependencies**: None (documentation can happen anytime)
**Spec Reference**: N/A (documentation)
**STATUS Reference**: STATUS-post-cleanup.md § P1 - Should Fix #3

### Description

Completely rewrite README.md to document the current LibGDX-based architecture. Current README extensively references Processing and will completely mislead new developers.

### Current Problems

- Line 2: "Processing-based control system" (false)
- Line 8: "Processing core.jar is included" (false)
- Line 10: "MUST USE JAVA SDK 1.8" (false—requires Java 17 now)
- Line 43: References 3 deleted gradle tasks
- Line 46: References deleted TesseractMain.java
- Line 51: "Save TesseractMain" (should be TesseractLauncher)

### Acceptance Criteria

- [ ] Update project description: "Processing-based" → "LibGDX-based"
- [ ] Update Java requirement: "Java SDK 1.8" → "Java SDK 17+"
- [ ] Update library information: Remove Processing core.jar mention, add LibGDX 1.12.1
- [ ] Update build commands: Remove `unzipProcessingVideoLibrary untarProcessingCoreLibrary downloadJoglJar`
- [ ] Update build commands: Keep only `unzipProcessingUdpLibrary fatJar`
- [ ] Update entry point: "TesseractMain.java" → "TesseractLauncher.java"
- [ ] Document LibGDX architecture: IRenderer interface, headless vs graphical modes
- [ ] Document JavaCV video: Explain cross-platform video support (replaces Processing video)
- [ ] Keep all hardware setup sections (LED controllers, Raspberry Pi, Docker)
- [ ] README accurately reflects current codebase state

### Technical Notes

**New README Structure** (suggested):

```markdown
# Tesseract VJ LED Control System

LibGDX-based LED control system for live visual performances...

## Requirements
- Java SDK 17+ (Java 17 required for LibGDX and Groovy 4)
- Gradle (wrapper included)

## Architecture
- **Graphics**: LibGDX 1.12.1 (cross-platform, headless-capable)
- **Video**: JavaCV 1.5.9 with FFmpeg (cross-platform video playback)
- **LED Output**: UDP networking to Teensy/PixelPusher hardware
- **UI**: Websocket server (port 8883) for remote control

## Building
./gradlew unzipProcessingUdpLibrary fatJar

## Running
java -jar build/libs/tesseract.jar
# or
java -jar build/libs/tesseract.jar --headless

## Entry Point
- TesseractLauncher.java - Initializes LibGDX and launches app

## Modes
- **Graphical**: Opens LibGDX window, renders visualizations (default)
- **Headless**: Runs without window, outputs only to UDP (--headless flag)

...
```

**Keep Unchanged**:
- Hardware setup sections (LED controllers, wiring)
- Raspberry Pi deployment instructions (update Docker references)
- Development workflow sections

**Files to Modify**:
- `README.md` - Complete rewrite of architecture sections

---

## Phase 4 - Clean build.gradle [P1]

**Status**: Not Started
**Complexity**: SMALL (1-2 hours)
**Dependencies**: None
**Spec Reference**: N/A (build config)
**STATUS Reference**: STATUS-post-cleanup.md § P1 - Should Fix #4

### Description

Clean up stale Processing-related tasks in build.gradle. The UDP library download tasks are still needed, but their names and comments are misleading because they reference "Processing" when Processing itself is no longer used.

### Current Problem

Lines 105-119 contain tasks named `downloadProcessingUdpLibrary` and `unzipProcessingUdpLibrary`. The UDP library (lib/udp/) is still needed, but the task names imply Processing is still a dependency, which is confusing.

### Acceptance Criteria

- [ ] Review UDP library tasks (lines 105-119)
- [ ] Update task comments to clarify: "UDP networking library (originally from Processing, now standalone)"
- [ ] Consider renaming tasks (optional): `downloadProcessingUdpLibrary` → `downloadUdpLibrary`
- [ ] If renaming, update `fatJar` task dependencies
- [ ] If renaming, update Docker and README references
- [ ] Remove or clarify any misleading "// Build fat JAR" comments near Processing tasks
- [ ] Verify build still works: `./gradlew clean unzipProcessingUdpLibrary fatJar` succeeds
- [ ] Document decision: If keeping names, add clear comment explaining why

### Technical Notes

**Decision Point**: Rename tasks or just clarify comments?

**Option A (Minimal)**: Keep task names, improve comments
- Pros: No risk, no downstream changes
- Cons: Names remain misleading

**Option B (Clean)**: Rename tasks to remove "Processing" reference
- Pros: Clearer intent, better long-term maintainability
- Cons: Must update Dockerfile, README, any scripts
- Recommended if Docker/README updates already planned (Phases 2 & 3)

**Files to Modify**:
- `build.gradle:105-119` - Task definitions and comments
- `docker/Dockerfile` (if renaming)
- `README.md` (if renaming)

**UDP Library Context**:
The lib/udp/ library was originally part of Processing's networking package but is included as source in this repo. It has unused Processing imports (`processing.core.*`) which are harmless since Processing is not in the classpath.

---

## Phase 5 - Fix UDP Library Import [P1]

**Status**: Not Started
**Complexity**: SMALL (1 hour)
**Dependencies**: None
**Spec Reference**: N/A (library cleanup)
**STATUS Reference**: STATUS-post-cleanup.md § P1 - Should Fix #5

### Description

Remove unused Processing import from UDP library. The lib/udp/src/UDP.java file imports `processing.core.*` but doesn't actually use it (compiles fine without Processing in classpath).

### Current State

- Line 26: `import processing.core.*;`
- Lines 153-156: Optional PApplet registration code (unused)
- Lines 201-202: Comment references PApplet disposal (unused)

### Acceptance Criteria

- [ ] Remove `import processing.core.*;` from lib/udp/src/UDP.java line 26
- [ ] Remove unused PApplet-related methods (lines 153-156, if present)
- [ ] Remove or update PApplet-related comments (lines 201-202)
- [ ] Verify UDP.java compiles: `./gradlew compileGroovy` succeeds
- [ ] Verify tests pass: `./gradlew test` succeeds (after Phase 1 fixes)
- [ ] Verify UDPModel still works in integration test
- [ ] Grep codebase for `processing.core` to ensure no other references remain: `grep -r "processing.core" src/` returns only comments

### Technical Notes

**Impact**: Very low risk. The UDP library currently compiles and works without Processing in the classpath, proving the import is unused.

**Why It's There**: The UDP library was originally part of Processing's contributed libraries and had optional integration hooks. Those hooks are not used in this project.

**Verification**:
```bash
# Confirm import is unused
grep -n "PApplet\|processing" lib/udp/src/UDP.java

# After removal, verify build
./gradlew clean compileGroovy
./gradlew test --tests TesseractAppTest
```

**Alternative Approach**: If removing code from lib/udp feels risky (it's an external library), document the unused imports instead:
```java
// Note: processing.core imports are unused but kept for compatibility
// with upstream UDP library source. This project does not use PApplet.
import processing.core.*;
```

**Files to Modify**:
- `lib/udp/src/UDP.java` - Remove Processing import and unused methods

---

## Dependency Graph

```
Phase 1 (Fix Tests) ─────┐
                          ├──> All phases can verify
Phase 2 (Fix Docker) ────┤     with working tests
                          │
Phase 3 (Update README) ─┤
                          │
Phase 4 (Clean Gradle) ──┤
                          │
Phase 5 (Fix UDP Import)─┘

No hard dependencies between phases.
Phases 2, 3, 4, 5 can run in parallel.
Phase 1 is recommended first for verification.
```

**Execution Order Recommendations**:

1. **Start with Phase 1** (Fix Tests) - Establishes verification baseline
2. **Phase 2 and 3 in parallel** (Docker + README) - Both are documentation/config
3. **Phase 4 and 5 in parallel** (Gradle + UDP) - Both are minor code cleanups
4. **Final Verification** - Run full test suite and Docker build

**Alternative (Faster)**:
- Run all phases in parallel if multiple developers available
- Coordinate on shared files (build.gradle used by Docker)

---

## Risk Assessment

### Low Risk Items
- **Phase 1 (Tests)**: Standard library replacement, well-documented migration path
- **Phase 3 (README)**: Documentation only, cannot break code
- **Phase 5 (UDP Import)**: Import already unused, library compiles without it

### Medium Risk Items
- **Phase 2 (Docker)**: Could break deployment if not tested thoroughly
- **Phase 4 (Gradle)**: Could break build if task dependencies wrong

### Mitigation Strategies

**Phase 2 (Docker)**:
- Test Docker build after each change
- Keep old Dockerfile as Dockerfile.old until verified
- Document all changes in commit message

**Phase 4 (Gradle)**:
- Test build after any task renames: `./gradlew clean unzipProcessingUdpLibrary fatJar`
- If renaming tasks, update all references atomically in single commit
- Consider keeping old task names as aliases temporarily

**General**:
- Create feature branch for all changes
- Test after each phase completion
- Integration test (`TesseractAppTest`) is the smoke test
- Keep git history clean with one commit per phase

---

## Success Criteria

### Definition of Done (All Phases)

- [ ] All 22 tests pass: `./gradlew test` → "BUILD SUCCESSFUL"
- [ ] Docker builds successfully: `docker/build.sh` completes
- [ ] Docker runs headless: `docker run <image> --headless` starts
- [ ] README accurately describes current architecture
- [ ] No Processing references in active code (lib/udp excluded, documented)
- [ ] No stale gradle tasks or misleading comments
- [ ] Integration test passes: `testCanStartApplicationHeadless` succeeds
- [ ] Build is clean: `./gradlew clean build` succeeds

### Verification Commands

```bash
# Test infrastructure works
./gradlew test
# Expected: BUILD SUCCESSFUL, 22 tests passed

# Build works
./gradlew clean unzipProcessingUdpLibrary fatJar
# Expected: BUILD SUCCESSFUL, tesseract.jar created

# Docker works
cd docker && ./build.sh
# Expected: Image built successfully

# Integration works
./gradlew test --tests TesseractAppTest
# Expected: testCanStartApplicationHeadless PASSED

# No Processing in code (only comments and lib/udp)
grep -r "import.*processing" src/main/
# Expected: No matches (or only ProcessingCompat which is intentional)
```

---

## Out of Scope (Deferred to Later)

The following P2 items from STATUS are explicitly **not** included in this plan:

- **TODO/FIXME triage** (21 comments) - Requires domain knowledge
- **Wildcard imports** (12 files) - Low priority code style
- **System.out vs logging** (37 uses) - Low priority code style
- **printStackTrace usage** (6 uses) - Low priority code style
- **Planning doc archival** - File management, non-urgent
- **Stale file cleanup** - Disk space, non-urgent
- **Justfile documentation** - Low priority

These can be addressed in a separate "code quality" pass after P0/P1 items are complete.

---

## Next Steps

1. **Review this plan** - Ensure all stakeholders agree on scope
2. **Set up feature branch** - `git checkout -b cleanup/post-processing`
3. **Execute Phase 1** - Fix test infrastructure first
4. **Verify baseline** - Ensure all tests pass before proceeding
5. **Execute Phases 2-5** - Can be done in parallel or sequentially
6. **Final verification** - Run all success criteria checks
7. **Code review** - Have changes reviewed before merging
8. **Merge to master** - Deploy updated system

**Estimated Total Time**: 2-3 days (assuming sequential execution, single developer)
**Estimated Total Time (Parallel)**: 1 day (if multiple developers work in parallel)

---

## Appendix: STATUS File Reference

This plan is derived from **STATUS-post-cleanup.md** generated on 2025-12-10.

**Key STATUS Findings**:
- Overall completion: 85%
- Build status: ✅ COMPILES
- Test status: ❌ FAILING (infrastructure issue, not code bugs)
- Technical debt: MODERATE
- Processing removal: ✅ COMPLETE (core work done)

**What STATUS Got Right**:
- Clear prioritization (P0/P1/P2)
- Root cause analysis for test failures
- Concrete evidence for each issue
- Realistic effort estimates
- Acknowledgment that Processing removal fundamentally succeeded

**This Plan's Additions**:
- Organized into sequential phases
- Clear acceptance criteria per phase
- Dependency graph
- Risk assessment with mitigations
- Success criteria and verification commands
- Explicit scope boundaries (P0/P1 only)
