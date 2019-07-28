package testUtil

import app.TesseractMain
import groovy.json.JsonBuilder
import org.junit.rules.TemporaryFolder
import org.powermock.api.mockito.PowerMockito
import util.Util

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

  public static void createMockPlaylist() {
    def jsonObj = [
        [
            id         : 1,
            displayName: 'Something',
            items      : []
        ]
    ]

    String playlistJsonPath = Util.getDataFilePath('playlist')

    new File(playlistJsonPath).write "${new JsonBuilder(jsonObj).toPrettyString()}\n"
  }
}
