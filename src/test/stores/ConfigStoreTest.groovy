package stores

import app.TesseractMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import testUtil.TestUtil
import util.Util

import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.junit.MatcherAssert.assertThat

@RunWith(PowerMockRunner.class)
@PrepareForTest([TesseractMain.class, Util.class])
class ConfigStoreTest {

  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder()

  @Rule
  public ExpectedException thrown = ExpectedException.none()

  @Before
  void setUp() {
    Util.enableColorization()

    TestUtil.mockUtilClass(tmpDir)

    TestUtil.mockTesseractMain()
  }

  @Test
  void testConfigStoreReadsFileAtConfigPath() {
    TestUtil.mockConfigFile(tmpDir, [initialPlaylist: 'Something'])

    TestUtil.createMockPlaylists()

    // Don't use singleton or it affects every test!
    ConfigStore store = new ConfigStore()

    assertThat store.getString('initialPlaylist'), equalTo('Something')
  }

  @Test
  void testConfigStoreThrowsErrorIfPlaylistDoesNotExist() {
    TestUtil.mockConfigFile(tmpDir, [initialPlaylist: 'non-existent playlist'])

    TestUtil.createMockPlaylists()

    // Expect an exception to be thrown w/ a specific msg
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("ERROR: Failed validation of option 'initialPlaylist': Playlist 'non-existent playlist' does not exist");

    // This will cause the exception to be thrown
    new ConfigStore()
  }

  @Test
  void testConfigStoreReadsConfigFileInRepoByDefault() {
    TestUtil.createMockPlaylists(displayName: 'Color Cube')
    assertThat new ConfigStore().getString('initialPlaylist'), equalTo('Color Cube')
  }

  @Test
  void testConfigStoreTransformsInitialPlayStateToUppercase() {
    TestUtil.createMockPlaylists(displayName: 'Color Cube')
    TestUtil.mockConfigFile(tmpDir, [initialPlaylist: 'Color Cube', initialPlayState: 'loop_scene'])
    assertThat new ConfigStore().getString('initialPlayState'), equalTo('LOOP_SCENE')
  }

  @Test
  void testConfigStoreThrowsExceptionForInvalidPlayStateValue() {
    TestUtil.createMockPlaylists(displayName: 'Color Cube')
    TestUtil.mockConfigFile(tmpDir, [initialPlaylist: 'Color Cube', initialPlayState: 'some_random_thing'])

    // Expect an exception to be thrown w/ a specific msg
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("ERROR: Failed validation of option 'initialPlayState': PlayState 'SOME_RANDOM_THING' is invalid.  Must be one of 'playing', 'loop_scene', or 'stopped'");

    new ConfigStore()
  }

  @Test
  void testConfigStoreUsesDefaultValuesIfConfigFileNotFound() {
    TestUtil.createMockPlaylists(displayName: 'Color Cube')

    System.setProperty("configPath", '/some/totally/non-existent/path')

    assertThat new ConfigStore().getString('initialPlaylist'), equalTo('Color Cube')
    assertThat new ConfigStore().getString('initialPlayState'), equalTo('LOOP_SCENE')
  }
}
