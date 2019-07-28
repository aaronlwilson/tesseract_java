package testUtil

import app.TesseractMain
import groovy.json.JsonBuilder
import org.apache.commons.io.FileUtils
import org.junit.rules.TemporaryFolder
import org.powermock.api.mockito.PowerMockito
import org.yaml.snakeyaml.Yaml
import util.Util

import java.nio.charset.Charset

import static org.mockito.Mockito.when

class TestUtil {
  public static void mockTesseractMain() {
    PowerMockito.mockStatic(TesseractMain.class)
    when(TesseractMain.getMain()).thenReturn(TestUtil.getMockMain())
  }

  public static TesseractMain getMockMain() { new MockMain() }

  // Mock some of the functions in the util class
  public static void mockUtilClass(TemporaryFolder tmpDir) {
    // By using 'stub', we can mock specific methods without messing with the rest of them
    String dataDir = tmpDir.getRoot().getCanonicalPath()
    PowerMockito.stub(PowerMockito.method(Util.class, "getDataDir")).toReturn(dataDir);
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

  public static void mockConfigFile(tmpDir, Map configData) {
    File configFile = tmpDir.newFile()
    FileUtils.writeStringToFile(configFile, new Yaml().dump(configData), Charset.defaultCharset())

    // Set the config path for the application
    System.setProperty('configPath', configFile.getCanonicalPath())
  }
}
