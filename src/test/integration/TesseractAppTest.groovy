package integration

import app.TesseractMain
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.SystemErrRule
import org.junit.contrib.java.lang.system.SystemOutRule
import stores.ConfigStore
import stores.MediaStore
import stores.PlaylistStore
import stores.SceneStore

import java.util.regex.Pattern

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.matchesPattern
import static org.hamcrest.junit.MatcherAssert.assertThat

class TesseractAppTest {

  @Rule
  public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

  @Rule
  public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();

  @After
  void teardown() {
    // Reset all singleton instances so we have fresh ones for each test
    // We will need to do this in every test suite.  Maybe do something like this: https://igorski.co/java/junit/run-stuff-before-and-after-each-test-in-junit4/
    ConfigStore.instance = null
    MediaStore.instance = null
    PlaylistStore.instance = null
    SceneStore.instance = null
  }

  @Test
  public void testCanStartApplication() {
    // Launch the application by calling the 'main' method
    TesseractMain.main([] as String[])

    // Give the application a few seconds to launch
    sleep 5 * 1000

    String stdout = systemOutRule.getLog().stripColors()
    String stderr = systemErrRule.getLog().stripColors()

    List<String> expectedStrings = [
        'Wrote Scene Data to Disk',
        'Writing Playlist Data to Disk',
        'Wrote Playlist Data to Disk',
        "Reading config file from ${ConfigStore.get().getConfigFilePath()}",
        'WebsocketInterface started on port: 8883',
        'Websocket server started',
        "[Playlist] Playing scene 'PerlinNoise' on playlist 'Color Cube' (Playstate: LOOP_SCENE)",
        '[StateManager] Sending stateUpdate event: activeState',
    ]

    // Assert that all of the expected strings appear in the output
    expectedStrings.each { String expected ->
      assertThat stdout, matchesPattern(~/[\s\S]*${Pattern.quote(expected)}[\s\S]*/)
    }

    // Assert that nothings been printed to stderr
    assertThat stderr.size(), equalTo(0)

    // The application will die immediately when this test finishes.  Use a 'sleep' if we want it to stay up longer
    // for any particular reason
    // sleep 1000 * 1
  }
}
