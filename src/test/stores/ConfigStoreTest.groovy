package stores

import app.TesseractMain
import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import testUtil.TestUtil
import util.Util

import java.nio.charset.Charset

import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.junit.MatcherAssert.assertThat

@RunWith(PowerMockRunner.class)
@PrepareForTest([TesseractMain.class, Util.class])
class ConfigStoreTest {

  public TesseractMain mockMain

  public String testConfigFile = """
    initialPlaylist: Something
    initialPlayState: loop_scene
  """.stripIndent()

  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder();

  @Before
  void setUp() {
    Util.enableColorization()

    TestUtil.mockUtilClass(tmpDir)

    TestUtil.createMockPlaylist()

    TestUtil.mockTesseractMain()
  }

  @Test
  void testConfigStoreReadsFileAtConfigPath() {
    File configFile = tmpDir.newFile()
    FileUtils.writeStringToFile(configFile, this.testConfigFile, Charset.defaultCharset())

    // Set the config path for the application
    System.setProperty('configPath', configFile.getCanonicalPath())

    ConfigStore store = ConfigStore.get()

    assertThat store.getString('initialPlaylist'), equalTo('Something')
  }
}
