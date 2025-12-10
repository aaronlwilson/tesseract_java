package integration

import app.TesseractLauncher
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.SystemErrRule
import org.junit.contrib.java.lang.system.SystemOutRule
import stores.ConfigStore
import stores.MediaStore
import stores.PlaylistStore
import stores.SceneStore

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.MatcherAssert.assertThat

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
  public void testCanStartApplicationHeadless() {
    // Test that TesseractLauncher can start in headless mode
    // This is a basic smoke test - the app should initialize without errors
    Thread appThread = new Thread({
      try {
        TesseractLauncher.main(["--headless"] as String[])
      } catch (Exception e) {
        e.printStackTrace()
      }
    })

    appThread.start()

    // Give it a moment to initialize
    Thread.sleep(2000)

    // Check that it started without fatal errors
    String output = systemOutRule.getLog()
    assertThat(output, containsString("Tesseract"))

    // Cleanup: interrupt the thread
    appThread.interrupt()
  }
}
