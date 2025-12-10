# Implementation Plan: Groovy 4.x / Java 17 / Processing 4 Upgrade

**Generated**: 2025-12-09-223500
**Source STATUS**: STATUS-groovy-java-upgrade.md
**Target**: Upgrade tesseract_java from Groovy 2.5.6/Java 8/Processing 3 to Groovy 4.x/Java 17/Processing 4

## Provenance
- **Source STATUS**: `.agent_planning/STATUS-groovy-java-upgrade.md` (generated 2025-12-09)
- **Specification**: Project requirements for ARM64 support and modern Java stack
- **Current Build Status**: ❌ BROKEN - Cannot compile on Java 17 due to Groovy 2.5.x/Gradle 5.4.1 incompatibility

## Executive Summary

**Current State**: ~15% complete. JavaCV video replacement code written but unverified. Build completely broken on Java 17.

**Critical Path**: Must unblock build (Phase 1) before any other work can proceed or be verified.

**Total Implementation Steps**: 29 discrete steps across 5 phases
**Estimated Complexity**: HIGH (multiple interacting dependencies, cross-platform concerns, API migrations)

**Key Decisions** (per user requirements):
1. **Groovy 4.x strategy**: Use `org.apache.groovy:groovy:4.0.17` (minimal, not groovy-all)
2. **Gradle version**: 7.6.4 (stable, well-tested)
3. **Test framework**: Keep JUnit 4, update Mockito to 4.x, remove PowerMock (Java 17 incompatible)

---

## Phase 1: Unblock Build (CRITICAL PATH)

**Priority**: P0 - **MUST COMPLETE FIRST**
**Status**: Not Started
**Effort**: MEDIUM (4-6 hours)
**Dependencies**: None
**Risk**: MEDIUM - Groovy 4.x may have breaking syntax changes requiring code fixes

### Description
The project cannot build on Java 17 due to fundamental incompatibilities. Gradle 5.4.1 ships with Groovy 2.5.4, which does not support Java 17. All dependencies use deprecated `compile`/`testCompile` syntax removed in Gradle 7.0. This phase unblocks the entire upgrade by modernizing the build system.

---

### STEP 1.1: Update Gradle Wrapper

**Status**: Not Started
**Effort**: SMALL
**Risk**: LOW

**What to do**:
Update Gradle from 5.4.1 to 7.6.4 to support Java 17 and Groovy 4.x.

**Files to modify**:
- `gradle/wrapper/gradle-wrapper.properties`

**Actions**:
1. Edit `gradle/wrapper/gradle-wrapper.properties`
2. Change `distributionUrl` from:
   ```
   https\://services.gradle.org/distributions/gradle-5.4.1-all.zip
   ```
   to:
   ```
   https\://services.gradle.org/distributions/gradle-7.6.4-all.zip
   ```
3. Run `./gradlew wrapper --gradle-version=7.6.4` to update wrapper files

**Acceptance Criteria**:
- [ ] `gradle/wrapper/gradle-wrapper.properties` contains `gradle-7.6.4-all.zip`
- [ ] `./gradlew --version` reports Gradle 7.6.4
- [ ] Wrapper downloads and initializes without errors

**Verification**:
```bash
./gradlew --version | grep "Gradle 7.6.4"
```

**Risk Mitigation**:
- Gradle 7.6.4 is well-tested and stable
- No code changes required for this step
- Can easily rollback by reverting properties file

---

### STEP 1.2: Replace Deprecated Dependency Syntax - Main Dependencies

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 1.1
**Risk**: LOW

**What to do**:
Replace all `compile` declarations with `implementation` (Gradle 7+ requirement). This step handles runtime dependencies.

**Files to modify**:
- `build.gradle`

**Actions**:
1. Find all lines with `compile group:` or `compile fileTree`
2. Replace `compile` with `implementation`
3. Count: 29 occurrences expected

**Pattern to find**:
```gradle
compile group: 'org.codehaus.groovy', name: 'groovy-all', version: '2.5.6'
compile group: 'org.slf4j', name: 'slf4j-api', version: '1.7.26'
compile fileTree(dir: 'lib', include: ['**/*.jar'])
```

**Pattern to replace with**:
```gradle
implementation group: 'org.codehaus.groovy', name: 'groovy-all', version: '2.5.6'
implementation group: 'org.slf4j', name: 'slf4j-api', version: '1.7.26'
implementation fileTree(dir: 'lib', include: ['**/*.jar'])
```

**Acceptance Criteria**:
- [ ] Zero occurrences of `compile group:` remain
- [ ] Zero occurrences of `compile fileTree` remain
- [ ] All replaced with `implementation`
- [ ] File syntax is valid Gradle

**Verification**:
```bash
grep -c "^[[:space:]]*compile " build.gradle  # Should return 0
grep -c "^[[:space:]]*implementation " build.gradle  # Should return ~29
```

**Risk Mitigation**:
- Mechanical find/replace operation
- `implementation` is direct replacement for `compile`
- No semantic changes to dependencies

---

### STEP 1.3: Replace Deprecated Dependency Syntax - Test Dependencies

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 1.2
**Risk**: LOW

**What to do**:
Replace all `testCompile` declarations with `testImplementation` (Gradle 7+ requirement).

**Files to modify**:
- `build.gradle`

**Actions**:
1. Find all lines with `testCompile group:`
2. Replace `testCompile` with `testImplementation`
3. Count: 8 occurrences expected

**Pattern to find**:
```gradle
testCompile group: 'junit', name: 'junit', version: '4.12'
testCompile group: 'org.mockito', name: 'mockito-core', version: '2.28.2'
testCompile group: 'org.powermock', name: 'powermock-core', version: '2.0.2'
```

**Pattern to replace with**:
```gradle
testImplementation group: 'junit', name: 'junit', version: '4.12'
testImplementation group: 'org.mockito', name: 'mockito-core', version: '2.28.2'
testImplementation group: 'org.powermock', name: 'powermock-core', version: '2.0.2'
```

**Acceptance Criteria**:
- [ ] Zero occurrences of `testCompile group:` remain
- [ ] All replaced with `testImplementation`
- [ ] Build file parses without syntax errors

**Verification**:
```bash
grep -c "testCompile " build.gradle  # Should return 0
grep -c "testImplementation " build.gradle  # Should return ~8
./gradlew dependencies --dry-run  # Should parse without errors
```

**Risk Mitigation**:
- Mechanical find/replace operation
- `testImplementation` is direct replacement for `testCompile`

---

### STEP 1.4: Update fatJar Task Configuration

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 1.3
**Risk**: LOW

**What to do**:
Update the `fatJar` task to use `runtimeClasspath` instead of deprecated `configurations.compile`.

**Files to modify**:
- `build.gradle`

**Actions**:
1. Locate the `fatJar` task (around line 202)
2. Replace `configurations.compile` with `configurations.runtimeClasspath`

**Current code** (lines 202-209):
```gradle
task fatJar(type: Jar) {
  manifest {
    attributes 'Main-Class': 'app.TesseractMain'
  }
  baseName = 'TesseractFatJar'
  from { configurations.compile.collect { it.isDirectory() ? it : zipTree(it) } }
  with jar
}
```

**New code**:
```gradle
task fatJar(type: Jar) {
  manifest {
    attributes 'Main-Class': 'app.TesseractMain'
  }
  archiveBaseName = 'TesseractFatJar'  // Also modernize baseName -> archiveBaseName
  from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
  with jar
}
```

**Acceptance Criteria**:
- [ ] `fatJar` task uses `runtimeClasspath` instead of `compile`
- [ ] `baseName` updated to `archiveBaseName` (Gradle 7+ convention)
- [ ] Task configuration is valid

**Verification**:
```bash
./gradlew fatJar --dry-run  # Should not error on task configuration
```

**Risk Mitigation**:
- `runtimeClasspath` includes all dependencies from `implementation`
- Behavior is equivalent for fat jar creation

---

### STEP 1.5: Upgrade Groovy to 4.0.17

**Status**: Not Started
**Effort**: MEDIUM
**Dependencies**: STEP 1.4
**Risk**: MEDIUM - May require source code changes

**What to do**:
Replace `groovy-all:2.5.6` with Groovy 4.0.17 minimal configuration. The `groovy-all` artifact no longer exists in Groovy 4.x; we must use the base `groovy` module.

**Files to modify**:
- `build.gradle`

**Actions**:
1. Remove the line:
   ```gradle
   implementation group: 'org.codehaus.groovy', name: 'groovy-all', version: '2.5.6'
   ```
2. Add:
   ```gradle
   implementation group: 'org.apache.groovy', name: 'groovy', version: '4.0.17'
   ```

**Note**: We're using the minimal `groovy` artifact, not `groovy-all`. If compile errors occur due to missing modules (e.g., `groovy-json`, `groovy-templates`), we'll add them incrementally.

**Acceptance Criteria**:
- [ ] `groovy-all:2.5.6` dependency removed
- [ ] `org.apache.groovy:groovy:4.0.17` added
- [ ] Dependency resolution succeeds (no version conflicts)

**Verification**:
```bash
./gradlew dependencies --configuration runtimeClasspath | grep "org.apache.groovy:groovy:4.0.17"
```

**Risk Mitigation**:
- Start with minimal `groovy` module
- Add additional modules (`groovy-json`, `groovy-templates`, etc.) only if needed based on compile errors
- Groovy 4.0.17 is stable and well-tested with Java 17

**Common Groovy 4.x Breaking Changes to Watch For**:
- MetaClass modifications (used in `Util.groovy` for string colorization)
- AST transformations
- Closure delegate handling
- Some internal package relocations

---

### STEP 1.6: First Build Attempt - Identify Groovy 4.x Issues

**Status**: Not Started
**Effort**: MEDIUM
**Dependencies**: STEP 1.5
**Risk**: HIGH - Unknown scope of code changes

**What to do**:
Attempt a clean build to identify any Groovy 4.x compatibility issues in source code. This is a diagnostic step.

**Actions**:
1. Run `./gradlew clean`
2. Run `./gradlew compileGroovy --info`
3. Document all compilation errors
4. Categorize errors:
   - Missing Groovy modules (requires adding dependencies)
   - Syntax changes (requires code fixes)
   - API changes (requires code refactoring)

**Expected Issues**:
- **MetaClass usage** in `src/main/util/Util.groovy` (lines 16-28) - String colorization via metaclass might have changed API
- **AST transformations** - if any `@CompileStatic`, `@TypeChecked` annotations exist
- **Closure delegate** - if closures with `delegate` property are used
- **Package imports** - some Groovy internal classes moved packages

**Acceptance Criteria**:
- [ ] Build attempted with detailed error output
- [ ] All compilation errors documented
- [ ] Errors categorized by type (missing module vs. syntax vs. API)
- [ ] Next steps identified for each error category

**Verification**:
```bash
./gradlew compileGroovy 2>&1 | tee groovy4-compile-errors.log
echo "Exit code: $?"  # Non-zero expected if errors exist
```

**Risk Mitigation**:
- This is a diagnostic step - failure is expected and informative
- Document errors thoroughly before attempting fixes
- May need to add groovy-json, groovy-templates, or other modules
- MetaClass usage in Util.groovy is the highest risk area

---

### STEP 1.7: Fix Groovy 4.x Compatibility Issues

**Status**: Not Started
**Effort**: MEDIUM-HIGH (depends on STEP 1.6 findings)
**Dependencies**: STEP 1.6
**Risk**: MEDIUM-HIGH

**What to do**:
Fix all Groovy 4.x compatibility issues identified in STEP 1.6. This step is intentionally flexible as the specific fixes depend on what errors surface.

**Known High-Risk Area**:
`src/main/util/Util.groovy` lines 16-28 use `String.metaClass` to add colorization methods. Groovy 4.x may have changed how metaclass modifications work.

**Potential Actions** (adapt based on actual errors):

1. **If missing groovy-json error**:
   - Add to `build.gradle`: `implementation 'org.apache.groovy:groovy-json:4.0.17'`

2. **If missing groovy-templates error**:
   - Add to `build.gradle`: `implementation 'org.apache.groovy:groovy-templates:4.0.17'`

3. **If MetaClass API changed**:
   - Review Groovy 4.x metaclass documentation
   - Update `Util.enableColorization()` method
   - May need to use `ExpandoMetaClass` explicitly

4. **If package import errors**:
   - Update import statements to new package locations
   - Consult Groovy 4.x migration guide

5. **If closure delegate errors**:
   - Review closure usage, update delegate handling
   - Check for deprecated closure properties

**Acceptance Criteria**:
- [ ] All Groovy compilation errors resolved
- [ ] `./gradlew compileGroovy` succeeds
- [ ] No new errors introduced
- [ ] Code changes documented in comments if non-obvious

**Verification**:
```bash
./gradlew compileGroovy  # Should exit 0
./gradlew classes  # Should build all classes successfully
```

**Risk Mitigation**:
- Fix errors incrementally, one category at a time
- Test each fix by re-running compile
- If MetaClass changes are too complex, consider alternative colorization approach
- Document any workarounds for future reference

---

### STEP 1.8: Verify Java Compilation Succeeds

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 1.7
**Risk**: LOW

**What to do**:
Verify that all Java source files compile successfully with Groovy 4.x and Java 17.

**Actions**:
1. Run `./gradlew compileJava`
2. Verify zero compilation errors
3. Check that Java/Groovy interop still works

**Acceptance Criteria**:
- [ ] `./gradlew compileJava` succeeds
- [ ] No Java 17 incompatibility errors
- [ ] Java classes can reference Groovy classes
- [ ] Groovy classes can reference Java classes

**Verification**:
```bash
./gradlew compileJava  # Should exit 0
./gradlew classes  # Should build all classes (Java + Groovy)
```

**Risk Mitigation**:
- Java source files should be unaffected by Groovy upgrade
- Main risk is Java/Groovy interop issues
- Very unlikely to have problems at this step

---

### STEP 1.9: Run Full Clean Build

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 1.8
**Risk**: LOW

**What to do**:
Execute a full clean build to verify the entire project compiles successfully.

**Actions**:
1. Run `./gradlew clean`
2. Run `./gradlew build --info`
3. Verify build artifacts are created
4. Check that test compilation succeeds (tests may fail, that's OK for this step)

**Acceptance Criteria**:
- [ ] `./gradlew clean build` completes without compilation errors
- [ ] Build artifacts created in `build/libs/`
- [ ] Test classes compile (even if tests fail at runtime)
- [ ] No dependency resolution errors

**Verification**:
```bash
./gradlew clean build 2>&1 | tee full-build.log
ls -lh build/libs/  # Should show compiled jar files
echo "Exit code: $?"  # Should be 0 if build succeeded
```

**Risk Mitigation**:
- If tests fail at this step, that's expected (PowerMock issues)
- Focus on compilation success, not test success
- Test fixes are handled in later phases

---

### STEP 1.10: Verify fatJar Task Works

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 1.9
**Risk**: LOW

**What to do**:
Verify the `fatJar` task builds successfully with all dependencies bundled.

**Actions**:
1. Run `./gradlew fatJar`
2. Verify fat jar is created
3. Check jar size is reasonable (should be 50-100MB with all dependencies)
4. Verify manifest has correct Main-Class

**Acceptance Criteria**:
- [ ] `./gradlew fatJar` succeeds
- [ ] `build/libs/TesseractFatJar.jar` created
- [ ] Jar size > 10MB (contains dependencies)
- [ ] Jar manifest contains `Main-Class: app.TesseractMain`

**Verification**:
```bash
./gradlew fatJar
ls -lh build/libs/TesseractFatJar.jar
unzip -p build/libs/TesseractFatJar.jar META-INF/MANIFEST.MF | grep Main-Class
```

**Risk Mitigation**:
- fatJar task configuration updated in STEP 1.4
- Should work if regular build succeeded
- If jar is too small, dependencies may not be included

---

## Phase 1 Summary

**When complete, you will have**:
- ✅ Gradle 7.6.4 (was 5.4.1)
- ✅ Groovy 4.0.17 (was 2.5.6)
- ✅ Modern Gradle syntax (implementation, not compile)
- ✅ Successful build on Java 17
- ✅ Working fatJar task

**This unblocks**: All subsequent phases - testing, Processing 4 upgrade, Docker updates

---

## Phase 2: Remove PowerMock & Update Test Dependencies

**Priority**: P0 - Required before tests can run
**Status**: Not Started
**Effort**: MEDIUM (3-4 hours)
**Dependencies**: Phase 1 complete
**Risk**: MEDIUM - Tests may need refactoring

### Description
PowerMock 2.0.2 is incompatible with Java 17 due to reflection restrictions. Remove all PowerMock usage and update to modern Mockito with inline mocking support. Keep JUnit 4 for minimal migration scope.

---

### STEP 2.1: Remove PowerMock Dependencies

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: Phase 1
**Risk**: LOW

**What to do**:
Remove all PowerMock dependencies from `build.gradle`.

**Files to modify**:
- `build.gradle`

**Actions**:
1. Remove these lines:
   ```gradle
   testImplementation group: 'org.powermock', name: 'powermock-core', version: '2.0.2'
   testImplementation group: 'org.powermock', name: 'powermock-module-junit4', version: '2.0.2'
   testImplementation group: 'org.powermock', name: 'powermock-api-mockito2', version: '2.0.2'
   ```

**Acceptance Criteria**:
- [ ] No PowerMock dependencies remain in `build.gradle`
- [ ] `./gradlew dependencies` shows no PowerMock artifacts
- [ ] Build still succeeds (tests will fail, that's expected)

**Verification**:
```bash
grep -i powermock build.gradle  # Should return nothing
./gradlew dependencies | grep -i powermock  # Should return nothing
```

**Risk Mitigation**:
- Simple removal, no code changes yet
- Tests will fail until we fix them in subsequent steps

---

### STEP 2.2: Update Mockito to 4.x

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 2.1
**Risk**: LOW

**What to do**:
Update Mockito from 2.28.2 to 4.x for Java 17 support and inline mocking capabilities.

**Files to modify**:
- `build.gradle`

**Actions**:
1. Change:
   ```gradle
   testImplementation group: 'org.mockito', name: 'mockito-core', version: '2.28.2'
   ```
   to:
   ```gradle
   testImplementation group: 'org.mockito', name: 'mockito-core', version: '4.11.0'
   testImplementation group: 'org.mockito', name: 'mockito-inline', version: '4.11.0'
   ```

**Note**: `mockito-inline` enables mocking of static methods and final classes without PowerMock.

**Acceptance Criteria**:
- [ ] Mockito 4.11.0 (or newer) in dependencies
- [ ] `mockito-inline` added for static mocking support
- [ ] Dependency resolution succeeds

**Verification**:
```bash
./gradlew dependencies --configuration testRuntimeClasspath | grep mockito
```

**Risk Mitigation**:
- Mockito 4.x is backward compatible with most 2.x usage
- inline module provides PowerMock replacement functionality

---

### STEP 2.3: Identify Tests Using PowerMock

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 2.2
**Risk**: LOW

**What to do**:
Find all test files that import or use PowerMock annotations/classes.

**Actions**:
1. Search for PowerMock imports:
   ```bash
   grep -r "import org.powermock" src/test/
   grep -r "@RunWith(PowerMock" src/test/
   grep -r "@PrepareForTest" src/test/
   ```
2. Document which test files need updates
3. Categorize by what PowerMock feature they use:
   - Static method mocking
   - Constructor mocking
   - Final class mocking
   - Private method testing

**Expected files**:
- May be none (PowerMock in dependencies but unused)
- If used, likely in integration tests

**Acceptance Criteria**:
- [ ] All PowerMock usages identified
- [ ] List of affected test files created
- [ ] Usage patterns categorized

**Verification**:
```bash
grep -r "powermock" src/test/ --include="*.groovy" --include="*.java" -i
```

**Risk Mitigation**:
- May find zero usages (best case)
- If usages exist, next step handles refactoring

---

### STEP 2.4: Refactor Tests to Remove PowerMock Usage

**Status**: Not Started
**Effort**: MEDIUM (depends on STEP 2.3 findings)
**Dependencies**: STEP 2.3
**Risk**: MEDIUM

**What to do**:
Replace PowerMock usage with Mockito 4.x equivalents. This step is conditional on findings from STEP 2.3.

**Potential Actions** (if PowerMock usages found):

**For static method mocking**:
```groovy
// OLD (PowerMock):
@RunWith(PowerMockRunner.class)
@PrepareForTest([SomeClass.class])
class MyTest {
    @Test
    void testStaticMethod() {
        PowerMockito.mockStatic(SomeClass.class)
        PowerMockito.when(SomeClass.staticMethod()).thenReturn("mocked")
        // test code
    }
}

// NEW (Mockito 4.x):
class MyTest {
    @Test
    void testStaticMethod() {
        try (MockedStatic<SomeClass> mocked = Mockito.mockStatic(SomeClass.class)) {
            mocked.when(() -> SomeClass.staticMethod()).thenReturn("mocked")
            // test code
        }
    }
}
```

**For constructor mocking**:
- Refactor code to use dependency injection instead
- Or use Mockito's `mockConstruction()`

**For private method testing**:
- Refactor to test through public API
- Or make methods package-private and test directly

**Acceptance Criteria**:
- [ ] All PowerMock imports removed from test files
- [ ] All PowerMock annotations removed
- [ ] Tests rewritten using Mockito 4.x or refactored to not need mocking
- [ ] Tests compile successfully

**Verification**:
```bash
./gradlew compileTestGroovy compileTestJava  # Should succeed
grep -r "powermock\|PowerMock" src/test/ -i  # Should return nothing
```

**Risk Mitigation**:
- If no PowerMock usage found in STEP 2.3, skip this step entirely
- For complex mocking, consider refactoring code for better testability
- Can temporarily disable problematic tests and fix later

---

### STEP 2.5: Run Test Suite - Identify Remaining Issues

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 2.4
**Risk**: LOW

**What to do**:
Run the full test suite to identify any remaining test failures after PowerMock removal.

**Actions**:
1. Run `./gradlew test --info`
2. Review test reports in `build/reports/tests/test/index.html`
3. Categorize failures:
   - Test logic errors (need fixing)
   - Environment issues (missing test data, etc.)
   - Dependency issues (missing test dependencies)
   - Groovy 4.x compatibility in test code

**Acceptance Criteria**:
- [ ] Test suite runs to completion (even if some fail)
- [ ] Test report generated
- [ ] All failures documented and categorized
- [ ] Plan created for fixing each category

**Verification**:
```bash
./gradlew test 2>&1 | tee test-run.log
open build/reports/tests/test/index.html  # View test results
```

**Risk Mitigation**:
- Some test failures expected at this stage
- Focus on getting tests to run, not pass (yet)
- Document failures for targeted fixes

---

### STEP 2.6: Fix Test Failures

**Status**: Not Started
**Effort**: MEDIUM (depends on STEP 2.5 findings)
**Dependencies**: STEP 2.5
**Risk**: MEDIUM

**What to do**:
Fix all test failures identified in STEP 2.5. Scope depends on findings.

**Common Expected Issues**:

1. **Groovy 4.x compatibility in test code**:
   - Similar issues as main code
   - Update imports, syntax as needed

2. **Changed test behavior after Mockito upgrade**:
   - Review mock verification syntax
   - Update argument matchers if needed

3. **Missing test resources or data**:
   - Ensure test data files exist
   - Check file paths are correct

4. **Integration test setup issues**:
   - Verify test fixtures work with Java 17
   - Update any reflection-based test utilities

**Acceptance Criteria**:
- [ ] All tests pass or are explicitly marked as skipped with reason
- [ ] `./gradlew test` exits 0
- [ ] Test coverage maintained or improved
- [ ] No flaky tests introduced

**Verification**:
```bash
./gradlew clean test
echo "Exit code: $?"  # Should be 0
```

**Risk Mitigation**:
- Fix highest-value tests first (integration tests, core logic)
- Can skip/disable low-value tests if fixes are too complex
- Document any skipped tests with JIRA/issue numbers
- Re-run tests multiple times to check for flakiness

---

## Phase 2 Summary

**When complete, you will have**:
- ✅ PowerMock removed (Java 17 incompatible)
- ✅ Mockito 4.11.0 with inline support
- ✅ All tests passing or explicitly skipped
- ✅ Modern test infrastructure

**This unblocks**: Confident refactoring and upgrades in later phases

---

## Phase 3: Upgrade Processing to 4.x

**Priority**: P1 - Core requirement for ARM64 JOGL support
**Status**: Not Started
**Effort**: MEDIUM-HIGH (4-6 hours)
**Dependencies**: Phase 1 complete
**Risk**: HIGH - Unknown API changes

### Description
Upgrade from Processing 3.5.4 to Processing 4.3 to get native ARM64 JOGL support and modern OpenGL bindings. Processing 4 has API changes that may require code updates.

---

### STEP 3.1: Research Processing 4 Breaking Changes

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: Phase 1
**Risk**: LOW

**What to do**:
Review Processing 4.x release notes and migration guide to understand breaking changes before attempting upgrade.

**Actions**:
1. Read Processing 4.0 release notes: https://github.com/processing/processing4/releases
2. Review PApplet API changes
3. Check P3D renderer changes
4. Look for deprecated methods used in tesseract_java
5. Document expected code changes

**Key Areas to Research**:
- `PApplet` method changes
- `settings()` method changes (JOGL initialization)
- `P3D` renderer compatibility
- `pixelDensity()` behavior
- Event handler changes (`draw()`, `keyPressed()`, etc.)
- Library loading mechanism changes

**Acceptance Criteria**:
- [ ] Processing 4.x breaking changes documented
- [ ] List of tesseract_java methods that need updating created
- [ ] Migration strategy defined
- [ ] Known risks identified

**Verification**:
Document findings in code comments or planning notes.

**Risk Mitigation**:
- Understanding changes before coding reduces trial-and-error
- May find dealbreaker issues early
- Can plan code changes in advance

---

### STEP 3.2: Update Processing Core Dependency

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 3.1
**Risk**: MEDIUM

**What to do**:
Replace Processing 3.5.4 with Processing 4.3 from Maven Central.

**Files to modify**:
- `build.gradle`

**Actions**:
1. Remove the local Processing core jar dependency:
   ```gradle
   implementation fileTree(dir: 'lib', include: ['**/*.jar'])
   ```
   becomes:
   ```gradle
   implementation fileTree(dir: 'lib', include: ['**/*.jar']) {
       exclude 'processing-core/**'
   }
   ```

2. Add Processing 4.x from Maven Central:
   ```gradle
   implementation group: 'org.processing', name: 'core', version: '4.3'
   ```

3. Remove obsolete Gradle tasks:
   - Delete `downloadProcessingCoreLibrary` task
   - Delete `untarProcessingCoreLibrary` task

4. Delete obsolete library files:
   ```bash
   rm -rf lib/processing-core/
   ```

**Acceptance Criteria**:
- [ ] Processing 4.3 dependency added
- [ ] Local core.jar excluded from file tree
- [ ] Old download tasks removed
- [ ] `./gradlew dependencies` shows `org.processing:core:4.3`
- [ ] Old lib/processing-core/ directory deleted

**Verification**:
```bash
./gradlew dependencies --configuration runtimeClasspath | grep "org.processing:core:4.3"
ls lib/processing-core 2>&1 | grep "No such file"  # Directory should not exist
```

**Risk Mitigation**:
- Keep local files until verification succeeds
- Can rollback by reverting build.gradle

---

### STEP 3.3: Update Processing Serial Library (if needed)

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 3.2
**Risk**: LOW

**What to do**:
Check if Processing serial library needs updating for Processing 4 compatibility.

**Files to modify**:
- `build.gradle`

**Current**:
```gradle
implementation group: 'org.processing', name: 'serial', version: '3.3.7'
```

**Actions**:
1. Check Maven Central for Processing 4-compatible serial library version
2. If available, update version number
3. If not available, check if serial library is actually used in code:
   ```bash
   grep -r "import processing.serial" src/
   ```
4. If unused, remove dependency

**Acceptance Criteria**:
- [ ] Serial library version compatible with Processing 4
- [ ] Or serial library removed if unused
- [ ] Dependencies resolve without conflicts

**Verification**:
```bash
./gradlew dependencies --configuration runtimeClasspath | grep processing
```

**Risk Mitigation**:
- Serial library may not have Processing 4 version
- Check if actually needed before trying to update
- Can remove if not used in code

---

### STEP 3.4: First Compile Attempt with Processing 4

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 3.3
**Risk**: MEDIUM

**What to do**:
Attempt compilation with Processing 4 to identify API incompatibilities.

**Actions**:
1. Run `./gradlew clean`
2. Run `./gradlew compileJava compileGroovy --info 2>&1 | tee processing4-errors.log`
3. Document all compilation errors
4. Categorize errors:
   - Missing methods (API removed)
   - Changed method signatures (parameters changed)
   - Renamed methods
   - Removed constants

**Expected Files with Issues**:
- `src/main/app/TesseractMain.java` - Main PApplet subclass
- Any Scene or Clip classes that call Processing methods

**Acceptance Criteria**:
- [ ] Compilation attempted
- [ ] All errors documented
- [ ] Errors categorized by type
- [ ] Affected files listed

**Verification**:
```bash
cat processing4-errors.log | grep "error:" | wc -l  # Count of errors
```

**Risk Mitigation**:
- This is diagnostic - failure expected
- Document errors before attempting fixes
- May discover Processing 4 is not yet feasible

---

### STEP 3.5: Fix Processing 4 API Compatibility Issues

**Status**: Not Started
**Effort**: MEDIUM-HIGH (depends on STEP 3.4 findings)
**Dependencies**: STEP 3.4
**Risk**: HIGH

**What to do**:
Fix all Processing 4 API compatibility issues identified in STEP 3.4.

**Common Expected Changes**:

1. **settings() method** (JOGL initialization):
   - May need to update size() call
   - Renderer specification might change
   - HiDPI/retina display handling

2. **PApplet method renames**:
   - Check if `map()`, `constrain()`, etc. still exist
   - Update to new method names if renamed

3. **Event handlers**:
   - Verify `draw()`, `keyPressed()`, `mousePressed()` signatures unchanged
   - Update if signatures changed

4. **P3D renderer**:
   - May need different initialization
   - Check if renderer string changed

5. **Library loading**:
   - May need different approach for native libraries
   - JOGL loading mechanism might change

**Files likely needing changes**:
- `src/main/app/TesseractMain.java`
- Any PApplet method calls in Scene classes

**Acceptance Criteria**:
- [ ] All compilation errors resolved
- [ ] No deprecated method warnings
- [ ] Code follows Processing 4 best practices
- [ ] `./gradlew build` succeeds

**Verification**:
```bash
./gradlew clean compileJava compileGroovy  # Should exit 0
```

**Risk Mitigation**:
- Consult Processing 4 Javadocs for each changed API
- Check Processing forum for migration examples
- May need to refactor significant code
- If too complex, consider staying on Processing 3 with manual JOGL ARM64 setup

---

### STEP 3.6: Remove Obsolete Processing 3 Libraries

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 3.5
**Risk**: LOW

**What to do**:
Clean up obsolete Processing 3 libraries no longer needed with Processing 4.

**Files to modify**:
- `build.gradle`
- Delete library directories

**Actions**:
1. Remove Processing video library (replaced by JavaCV):
   - Delete Gradle tasks: `downloadProcessingVideoLibrary`, `unzipProcessingVideoLibrary`
   - Delete `lib/video/` directory if exists

2. Remove Processing UDP library if unused:
   - Check usage: `grep -r "import processing.udp\|import hypermedia.net" src/`
   - If unused, delete tasks: `downloadProcessingUdpLibrary`, `unzipProcessingUdpLibrary`
   - Delete `lib/udp/` directory if exists

3. Remove JOGL download task:
   - Delete `downloadJoglJar` task (Processing 4 includes proper JOGL)

**Acceptance Criteria**:
- [ ] Obsolete download tasks removed
- [ ] Obsolete library directories deleted
- [ ] Build file cleaned up
- [ ] Build still succeeds

**Verification**:
```bash
./gradlew tasks --all | grep -i "download\|unzip"  # Should show minimal tasks
ls lib/  # Should not show video, old processing-core
```

**Risk Mitigation**:
- Keep directories until verification succeeds
- Can restore from git if needed

---

### STEP 3.7: Test JOGL Initialization on ARM64

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 3.6
**Risk**: MEDIUM

**What to do**:
Verify JOGL initializes correctly with Processing 4 on Apple Silicon (ARM64).

**Actions**:
1. Build fat jar: `./gradlew fatJar`
2. Run application: `java -jar build/libs/TesseractFatJar.jar`
3. Verify:
   - Application window opens
   - No native library errors
   - JOGL/OpenGL context creates successfully
   - No warnings about missing ARM64 natives

**Expected behavior**:
- Window opens showing Processing sketch
- Console shows JOGL version info
- No UnsatisfiedLinkError exceptions

**Acceptance Criteria**:
- [ ] Application launches without errors
- [ ] JOGL initializes successfully
- [ ] Processing window displays
- [ ] No native library warnings in console
- [ ] OpenGL context created

**Verification**:
```bash
java -jar build/libs/TesseractFatJar.jar 2>&1 | tee jogl-test.log
grep -i "error\|exception\|unsatisfied" jogl-test.log  # Should be empty
```

**Risk Mitigation**:
- Run on actual ARM64 macOS if possible
- Check for "Using experimental ARM64 libraries" warnings
- Verify correct JOGL version loaded
- If fails, check Processing 4 actually includes ARM64 JOGL

**Test on multiple platforms** (if available):
- macOS ARM64 (primary)
- macOS x86_64
- Linux x86_64

---

### STEP 3.8: Verify JavaCV Video Playback with Processing 4

**Status**: Not Started
**Effort**: MEDIUM
**Dependencies**: STEP 3.7
**Risk**: MEDIUM

**What to do**:
Test that JavaCVVideoClip works correctly with Processing 4's event loop and rendering.

**Actions**:
1. Ensure test video file exists in `data/videos/`
2. Run application
3. Load a scene with JavaCVVideoClip
4. Verify:
   - Video loads without errors
   - Video playback is smooth
   - Frame rate matches video FPS
   - Pixels map correctly to LED nodes
   - Video loops correctly
   - No memory leaks (check over 5+ minutes)

**Test procedure**:
```bash
# Run app
java -jar build/libs/TesseractFatJar.jar

# In UI/websocket, load a video scene
# Observe playback for several minutes
# Check memory usage: jconsole or Activity Monitor
```

**Acceptance Criteria**:
- [ ] Video loads successfully
- [ ] Playback is smooth (no stuttering)
- [ ] Frame rate stable
- [ ] Pixels extracted correctly
- [ ] Looping works
- [ ] No memory growth over 5 minutes
- [ ] No FFmpeg errors in console

**Verification**:
Visual inspection + memory monitoring.

**Risk Mitigation**:
- JavaCVVideoClip is already implemented
- Processing 4 shouldn't affect JavaCV
- Main risk is event loop timing changes
- If frame timing issues, adjust frame interval calculation

---

## Phase 3 Summary

**When complete, you will have**:
- ✅ Processing 4.3 (was 3.5.4)
- ✅ Native ARM64 JOGL support
- ✅ Modern OpenGL bindings
- ✅ JavaCV video playback verified
- ✅ Obsolete libraries removed

**This achieves**: Original project goal of ARM64 support

---

## Phase 4: Update Supporting Dependencies

**Priority**: P2 - Risk mitigation & modernization
**Status**: Not Started
**Effort**: SMALL-MEDIUM (2-3 hours)
**Dependencies**: Phase 1 complete
**Risk**: LOW

### Description
Update old dependencies to Java 17 compatible versions to reduce risk of subtle incompatibilities and security issues.

---

### STEP 4.1: Update SLF4J to 2.x

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: Phase 1
**Risk**: LOW

**What to do**:
Update SLF4J from 1.7.26 to 2.x for Java 17 compatibility.

**Files to modify**:
- `build.gradle`

**Actions**:
1. Update slf4j-api:
   ```gradle
   implementation group: 'org.slf4j', name: 'slf4j-api', version: '2.0.9'
   ```

2. Replace slf4j-log4j12 with slf4j-simple (log4j12 is deprecated):
   ```gradle
   // OLD: implementation group: 'org.slf4j', name: 'slf4j-log4j12', version: '1.7.26'
   // NEW:
   implementation group: 'org.slf4j', name: 'slf4j-simple', version: '2.0.9'
   ```

**Acceptance Criteria**:
- [ ] SLF4J 2.0.9 in dependencies
- [ ] slf4j-log4j12 removed
- [ ] slf4j-simple added
- [ ] Application logs still work
- [ ] No logging errors at runtime

**Verification**:
```bash
./gradlew dependencies | grep slf4j
./gradlew run  # Check that logging works
```

**Risk Mitigation**:
- SLF4J 2.x is backward compatible
- slf4j-simple is simpler than log4j12
- Logging is non-critical functionality

---

### STEP 4.2: Update Java-WebSocket Library

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: Phase 1
**Risk**: LOW

**What to do**:
Update Java-WebSocket from 1.4.0 to 1.5.x for Java 17 compatibility.

**Files to modify**:
- `build.gradle`

**Actions**:
1. Update dependency:
   ```gradle
   implementation group: 'org.java-websocket', name: 'Java-WebSocket', version: '1.5.4'
   ```

**Acceptance Criteria**:
- [ ] Java-WebSocket 1.5.4 in dependencies
- [ ] No API breaking changes affecting tesseract_java
- [ ] Websocket server starts correctly
- [ ] Can connect from client

**Verification**:
```bash
./gradlew dependencies | grep Java-WebSocket
./gradlew run  # Test websocket connectivity
```

**Risk Mitigation**:
- 1.5.x is backward compatible with 1.4.x
- WebsocketInterface.groovy may need minor updates
- Can rollback if issues occur

---

### STEP 4.3: Update SnakeYAML to 2.x

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: Phase 1
**Risk**: LOW

**What to do**:
Update SnakeYAML from 1.24 to 2.x for Java 17 compatibility and security fixes.

**Files to modify**:
- `build.gradle`

**Actions**:
1. Update dependency:
   ```gradle
   implementation group: 'org.yaml', name: 'snakeyaml', version: '2.2'
   ```

**Note**: SnakeYAML 2.x has breaking changes around trusted classes (security feature). May need to configure trusted types.

**Acceptance Criteria**:
- [ ] SnakeYAML 2.2 in dependencies
- [ ] YAML config files load successfully
- [ ] No deserialization errors
- [ ] Config parsing works as before

**Verification**:
```bash
./gradlew dependencies | grep snakeyaml
./gradlew run  # Check that config loading works
```

**Risk Mitigation**:
- SnakeYAML 2.x has security-focused breaking changes
- May need to configure trusted classes for deserialization
- If too complex, can stay on 1.x (still works on Java 17)

---

### STEP 4.4: Verify All Updated Dependencies

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 4.1, 4.2, 4.3
**Risk**: LOW

**What to do**:
Run full build and smoke test to verify all dependency updates work together.

**Actions**:
1. Run `./gradlew clean build`
2. Run `./gradlew fatJar`
3. Run application and test:
   - Logging works
   - Websocket connects
   - Config loads
   - Video plays

**Acceptance Criteria**:
- [ ] Full build succeeds
- [ ] Fat jar builds
- [ ] Application runs
- [ ] All features work (logging, websocket, config, video)
- [ ] No runtime errors related to dependencies

**Verification**:
```bash
./gradlew clean build fatJar
java -jar build/libs/TesseractFatJar.jar
# Test all features manually
```

**Risk Mitigation**:
- Incremental updates reduce risk
- Each dependency updated independently
- Can rollback individual dependencies if needed

---

## Phase 4 Summary

**When complete, you will have**:
- ✅ SLF4J 2.0.9 (modern logging)
- ✅ Java-WebSocket 1.5.4 (Java 17 compatible)
- ✅ SnakeYAML 2.2 (security fixes)
- ✅ All dependencies Java 17 compatible

**This achieves**: Reduced technical debt, improved security, Java 17 ecosystem alignment

---

## Phase 5: Docker & Cross-Platform Verification

**Priority**: P2 - Deployment & production readiness
**Status**: Not Started
**Effort**: MEDIUM (3-4 hours)
**Dependencies**: Phase 1, Phase 3
**Risk**: MEDIUM

### Description
Update Docker configuration for Java 17 and verify cross-platform compatibility on all target platforms.

---

### STEP 5.1: Update Dockerfile for Java 17

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: Phase 1
**Risk**: LOW

**What to do**:
Update Dockerfile from Ubuntu 16.04 + Java 8 to Ubuntu 22.04 + Java 17.

**Files to modify**:
- `docker/Dockerfile`

**Actions**:
1. Update base image:
   ```dockerfile
   FROM ubuntu:22.04
   ```

2. Remove Java 8 download logic, install Java 17 from apt:
   ```dockerfile
   RUN apt-get update && apt-get install -y \
       openjdk-17-jre-headless \
       xvfb \
       libgl1-mesa-glx \
       libglu1-mesa \
       && rm -rf /var/lib/apt/lists/*
   ```

3. Update JAVA_HOME if needed:
   ```dockerfile
   ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
   ```

4. Verify Gradle wrapper works (should already be 7.6.4 from Phase 1)

**Acceptance Criteria**:
- [ ] Dockerfile uses Ubuntu 22.04
- [ ] Dockerfile installs Java 17
- [ ] Dockerfile syntax is valid
- [ ] Build dependencies are correct

**Verification**:
```bash
docker build -f docker/Dockerfile . --no-cache -t tesseract-test
docker run tesseract-test java -version  # Should show Java 17
```

**Risk Mitigation**:
- Ubuntu 22.04 is LTS, stable choice
- OpenJDK 17 from apt is well-tested
- Can test locally before deploying

---

### STEP 5.2: Update Native Library Paths for Docker

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 5.1
**Risk**: LOW

**What to do**:
Verify native libraries (JOGL, FFmpeg) load correctly in Docker container.

**Actions**:
1. Ensure Dockerfile copies necessary native libraries
2. Set LD_LIBRARY_PATH if needed:
   ```dockerfile
   ENV LD_LIBRARY_PATH=/app/lib/natives:$LD_LIBRARY_PATH
   ```
3. Verify FFmpeg natives are included in fat jar
4. Verify JOGL natives load from Processing 4

**Acceptance Criteria**:
- [ ] Native libraries accessible in container
- [ ] LD_LIBRARY_PATH set correctly if needed
- [ ] JOGL loads without UnsatisfiedLinkError
- [ ] FFmpeg loads without errors

**Verification**:
```bash
docker run tesseract-test java -jar /app/TesseractFatJar.jar --version
# Check for native library errors
```

**Risk Mitigation**:
- Test with simple JOGL/FFmpeg initialization
- Check library paths are correct for Linux x86_64
- May need to adjust Dockerfile COPY commands

---

### STEP 5.3: Build and Test Docker Image

**Status**: Not Started
**Effort**: MEDIUM
**Dependencies**: STEP 5.2
**Risk**: MEDIUM

**What to do**:
Build complete Docker image and verify application runs in container.

**Actions**:
1. Build image:
   ```bash
   docker build -f docker/Dockerfile -t tesseract:java17 .
   ```
2. Run container:
   ```bash
   docker run -it -p 8080:8080 tesseract:java17
   ```
3. Test:
   - Application starts
   - JOGL initializes (headless with Xvfb)
   - Websocket server binds to port
   - Can connect from host machine
   - Video playback works

**Acceptance Criteria**:
- [ ] Docker image builds successfully
- [ ] Container starts without errors
- [ ] Application runs in container
- [ ] JOGL works with Xvfb
- [ ] Websocket accessible from host
- [ ] Video playback works

**Verification**:
```bash
docker build -f docker/Dockerfile -t tesseract:java17 .
docker run -it -p 8080:8080 tesseract:java17
# In another terminal:
curl http://localhost:8080/health  # Or whatever health check exists
```

**Risk Mitigation**:
- Test incrementally (build, start, connect, features)
- Check Docker logs for errors
- Verify Xvfb virtual display works
- May need to adjust display/OpenGL settings

---

### STEP 5.4: Cross-Platform Native Library Verification

**Status**: Not Started
**Effort**: MEDIUM
**Dependencies**: Phase 3 (Processing 4 upgrade)
**Risk**: MEDIUM

**What to do**:
Verify native libraries (JOGL, FFmpeg) load correctly on all target platforms.

**Target Platforms**:
1. macOS ARM64 (Apple Silicon) - PRIMARY
2. macOS x86_64 (Intel)
3. Linux x86_64
4. Linux ARM64 (Raspberry Pi 4)
5. Linux ARMhf (Raspberry Pi 32-bit)

**Actions for each platform**:
1. Copy fat jar to target platform
2. Run: `java -jar TesseractFatJar.jar`
3. Verify:
   - Application launches
   - JOGL initializes
   - Correct architecture natives load
   - FFmpeg loads
   - Video playback works
   - No UnsatisfiedLinkError

**Acceptance Criteria**:
- [ ] macOS ARM64: All features work ✅
- [ ] macOS x86_64: All features work ✅
- [ ] Linux x86_64: All features work ✅
- [ ] Linux ARM64: All features work ✅ (or documented limitation)
- [ ] Linux ARMhf: All features work ✅ (or documented limitation)

**Verification**:
```bash
# On each platform:
java -version  # Verify Java 17
java -jar TesseractFatJar.jar 2>&1 | tee platform-test.log
grep -i "unsatisfied\|error" platform-test.log
```

**Risk Mitigation**:
- May not have access to all platforms
- Document which platforms are verified
- Raspberry Pi may have specific JOGL/OpenGL limitations
- FFmpeg ARM natives may need special handling

**If platform not available**:
- Document as untested
- Rely on CI/CD for that platform
- Or mark as community-tested

---

### STEP 5.5: Performance Validation

**Status**: Not Started
**Effort**: SMALL
**Dependencies**: STEP 5.4
**Risk**: LOW

**What to do**:
Verify performance is acceptable on Java 17 with new dependencies.

**Actions**:
1. Run application with video playback for 30 minutes
2. Monitor:
   - CPU usage
   - Memory usage (check for leaks)
   - Frame rate stability
   - No degradation over time
3. Compare to baseline (if available from Java 8 version)

**Metrics to track**:
- Steady-state memory usage
- Memory growth over 30 minutes (should be minimal)
- CPU usage during video playback
- Frame rate (should match video FPS)
- Garbage collection frequency

**Acceptance Criteria**:
- [ ] No memory leaks (< 10MB growth over 30 min)
- [ ] CPU usage reasonable (< 50% on modern CPU)
- [ ] Frame rate stable
- [ ] No performance degradation over time
- [ ] No excessive GC pauses

**Verification**:
```bash
# Monitor with jconsole or VisualVM
jconsole &
java -jar TesseractFatJar.jar
# Let run for 30 minutes, observe memory/CPU graphs
```

**Risk Mitigation**:
- JavaCV frame grabbers must be properly released
- Check for buffered frame accumulation
- Verify GC is tuned appropriately
- May need JVM flags for performance tuning

---

## Phase 5 Summary

**When complete, you will have**:
- ✅ Docker image with Java 17
- ✅ Cross-platform native libraries verified
- ✅ Production-ready deployment
- ✅ Performance validated

**This achieves**: Production readiness and deployment capability

---

## Overall Workflow & Dependencies

### Critical Path (Must Complete in Order):
1. **Phase 1** → Unblock build (REQUIRED for everything else)
2. **Phase 2** → Fix tests (validates changes work correctly)
3. **Phase 3** → Processing 4 upgrade (core goal)
4. **Phase 4** & **Phase 5** → Can run in parallel after Phase 1

### Parallel Work Opportunities:
- After Phase 1 completes:
  - Phase 2 (tests) and Phase 4 (dependencies) can run in parallel
  - Phase 5 (Docker) can start once Phase 1 done
- Processing 4 upgrade (Phase 3) blocks on Phase 1 only

### Verification Gates:
After each phase completes:
- ✅ Full clean build succeeds
- ✅ Tests pass (or are explicitly skipped with reason)
- ✅ Application launches
- ✅ Core features work (JOGL, video, websocket)

---

## Risk Assessment

### HIGH RISK ⚠️
| Risk | Phase | Mitigation |
|------|-------|------------|
| Groovy 4.x breaking changes | 1.6, 1.7 | Incremental fixes, document workarounds |
| Processing 4 API changes | 3.4, 3.5 | Research first, may need significant refactoring |
| MetaClass incompatibility | 1.6, 1.7 | Alternative colorization approach if needed |

### MEDIUM RISK ⚡
| Risk | Phase | Mitigation |
|------|-------|------------|
| PowerMock removal complexity | 2.3, 2.4 | May have zero usage; use Mockito inline if needed |
| SnakeYAML 2.x breaking changes | 4.3 | Configure trusted types; can stay on 1.x if too complex |
| ARM64 native library loading | 5.4 | Processing 4 should handle; verify on actual hardware |
| Docker native library paths | 5.2, 5.3 | Test incrementally, adjust LD_LIBRARY_PATH |

### LOW RISK ✓
| Risk | Phase | Mitigation |
|------|-------|------------|
| Gradle syntax updates | 1.2, 1.3 | Mechanical find/replace |
| SLF4J 2.x upgrade | 4.1 | Backward compatible |
| Java-WebSocket upgrade | 4.2 | Backward compatible |

---

## Ambiguities & Assumptions

### Assumed (per user requirements):
- ✅ Groovy 4.x: Use `org.apache.groovy:groovy:4.0.17` (minimal, not groovy-all)
- ✅ Gradle: 7.6.4 (stable)
- ✅ Tests: Keep JUnit 4, update Mockito 4.x, remove PowerMock

### Unknown Until Attempted:
| Area | Unknown | Impact | Phase |
|------|---------|--------|-------|
| Groovy 4.x source compatibility | Actual code changes needed | May require refactoring | 1.6, 1.7 |
| Processing 4 API | Breaking changes in PApplet | May require significant code changes | 3.4, 3.5 |
| PowerMock usage | Whether it's actually used in tests | If unused, skip removal steps | 2.3, 2.4 |
| Raspberry Pi JOGL | Whether Processing 4 JOGL works on ARM | May need manual JOGL setup | 5.4 |

---

## Rollback Plan

**If Phase 1 fails catastrophically**:
1. Revert `build.gradle` to original
2. Revert `gradle-wrapper.properties` to 5.4.1
3. Stay on Java 8 until issues resolved

**If Groovy 4.x incompatibility is too severe**:
1. Try Groovy 4.0.23 (latest 4.0.x)
2. Or try Groovy 3.0.x (intermediate version, Java 17 compatible)
3. Or convert Groovy files to Java

**If Processing 4 API incompatibility is too severe**:
1. Stay on Processing 3.5.4
2. Manually install ARM64 JOGL natives
3. Use Processing 3 with custom JOGL configuration

**Git Strategy**:
- Create branch `groovy4-java17-upgrade` before starting
- Commit after each phase completes
- Can cherry-pick phases if some succeed and others fail

---

## Success Criteria (Entire Upgrade)

### Build & Compilation ✅
- [ ] Project builds on Java 17 with zero errors
- [ ] Gradle 7.6.4 wrapper works
- [ ] All Java and Groovy code compiles
- [ ] Fat jar task builds successfully
- [ ] No deprecated API warnings

### Tests ✅
- [ ] All tests pass or are explicitly skipped with documented reason
- [ ] Test coverage maintained or improved
- [ ] No PowerMock dependencies
- [ ] Modern Mockito 4.x in use

### Processing 4 ✅
- [ ] Processing 4.3 (or newer) in dependencies
- [ ] JOGL initializes on ARM64 macOS
- [ ] No native library errors
- [ ] Video playback works with JavaCV

### Dependencies ✅
- [ ] All dependencies Java 17 compatible
- [ ] No security vulnerabilities in dependencies
- [ ] Modern versions in use (SLF4J 2.x, Mockito 4.x, etc.)

### Cross-Platform ✅
- [ ] Works on macOS ARM64 (Apple Silicon) - PRIMARY
- [ ] Works on macOS x86_64
- [ ] Works on Linux x86_64
- [ ] Works on Linux ARM (best effort)

### Docker ✅
- [ ] Docker image builds with Java 17
- [ ] Application runs in container
- [ ] Headless rendering works (Xvfb)
- [ ] Websocket accessible

### Performance ✅
- [ ] No memory leaks
- [ ] Stable frame rate
- [ ] Acceptable CPU usage
- [ ] No degradation over time

---

## Estimated Total Effort

| Phase | Complexity | Time Estimate |
|-------|-----------|---------------|
| Phase 1: Unblock Build | MEDIUM | 4-6 hours |
| Phase 2: Test Updates | MEDIUM | 3-4 hours |
| Phase 3: Processing 4 | MEDIUM-HIGH | 4-6 hours |
| Phase 4: Dependencies | SMALL-MEDIUM | 2-3 hours |
| Phase 5: Docker & Cross-Platform | MEDIUM | 3-4 hours |
| **TOTAL** | **HIGH** | **16-23 hours** |

**Note**: Estimates assume no major unexpected issues. Groovy 4.x and Processing 4 compatibility unknowns could increase actual time.

---

## Next Steps

**IMMEDIATE ACTION**:
1. ✅ User confirms recommendations (Groovy 4.0.17, Gradle 7.6.4, keep JUnit 4)
2. ▶️ Start Phase 1, Step 1.1: Update Gradle wrapper
3. ▶️ Work through Phase 1 sequentially
4. ▶️ After Phase 1 complete, verify with full build + smoke test
5. ▶️ Proceed to Phase 2 (tests) or Phase 3 (Processing 4) based on priority

**After Completion**:
- Document lessons learned
- Update main README with new requirements
- Create deployment guide for Java 17
- Archive old Java 8 documentation

---

## Questions for User (if any arise during implementation)

1. **Groovy MetaClass issues**: If Util.groovy colorization breaks, OK to use simpler approach?
2. **Processing 4 dealbreakers**: If Processing 4 requires massive refactoring, stay on Processing 3?
3. **Test skipping**: OK to skip/disable low-value tests if fixes too complex?
4. **Platform coverage**: Which platforms are critical vs. nice-to-have?
5. **Performance baseline**: Do we have Java 8 performance metrics to compare against?

---

**End of Implementation Plan**
