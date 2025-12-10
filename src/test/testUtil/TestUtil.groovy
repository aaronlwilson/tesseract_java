package testUtil

import app.TesseractApp
import groovy.json.JsonBuilder
import org.apache.commons.io.FileUtils
import org.junit.rules.TemporaryFolder
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.yaml.snakeyaml.Yaml
import util.Util

import java.nio.charset.Charset
import java.util.regex.Pattern

class TestUtil {
  // Store the static mock so we can close it later if needed
  private static MockedStatic<TesseractApp> tesseractAppMock
  private static MockedStatic<Util> utilMock

  public static void mockTesseractApp() {
    // Use Mockito's mockStatic instead of PowerMockito
    if (tesseractAppMock != null) {
      tesseractAppMock.close()
    }
    tesseractAppMock = Mockito.mockStatic(TesseractApp.class)
    tesseractAppMock.when { TesseractApp.getMain() }.thenReturn(TestUtil.getMockMain())
  }

  public static TesseractApp getMockMain() { new MockMain() }

  // Mock some of the functions in the util class (JUnit 4 version)
  public static void mockUtilClass(TemporaryFolder tmpDir) {
    mockUtilClass(tmpDir.getRoot())
  }

  // Mock some of the functions in the util class (JUnit 5 version - accepts File)
  public static void mockUtilClass(File tmpDir) {
    // Use Mockito's mockStatic for stubbing static methods
    String dataDir = tmpDir.getCanonicalPath()
    if (utilMock != null) {
      utilMock.close()
    }
    utilMock = Mockito.mockStatic(Util.class, Mockito.CALLS_REAL_METHODS)
    utilMock.when { Util.getDataDir(Mockito.anyString()) }.thenReturn(dataDir)
  }

  public static void cleanupMocks() {
    if (tesseractAppMock != null) {
      tesseractAppMock.close()
      tesseractAppMock = null
    }
    if (utilMock != null) {
      utilMock.close()
      utilMock = null
    }
  }

  public static Map getMockPlaylist(Map data) {
    [
        id         : 1,
        displayName: 'Something',
        items      : []
    ] + data
  }

  public static void createMockPlaylists(Map playlistData) {
    TestUtil.createMockPlaylists([playlistData])
  }

  public static void createMockPlaylists(List<Map> playlistData = [[:]]) {
    List<Map> playlists = playlistData.collect { getMockPlaylist(it) }

    String playlistJsonPath = Util.getDataFilePath('playlist')

    new File(playlistJsonPath).write "${new JsonBuilder(playlists).toPrettyString()}\n"
  }

  // JUnit 4 version - accepts TemporaryFolder
  public static void mockConfigFile(TemporaryFolder tmpDir, Map configData) {
    File configFile = tmpDir.newFile()
    FileUtils.writeStringToFile(configFile, new Yaml().dump(configData), Charset.defaultCharset())

    // Set the config path for the application
    System.setProperty('configPath', configFile.getCanonicalPath())
  }

  // JUnit 5 version - accepts File
  public static void mockConfigFile(File tmpDir, Map configData) {
    File configFile = new File(tmpDir, "config-${System.currentTimeMillis()}.yml")
    configFile.createNewFile()
    FileUtils.writeStringToFile(configFile, new Yaml().dump(configData), Charset.defaultCharset())

    // Set the config path for the application
    System.setProperty('configPath', configFile.getCanonicalPath())
  }

  // Converts a string literal into a pattern suitable for partial matching (basically a string contains)
  public static Pattern preparePartialMatchPattern(String s) {
    ~/[\s\S]*${Pattern.quote(s)}[\s\S]*/
  }
}
