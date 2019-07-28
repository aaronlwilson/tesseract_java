package stores

import app.TesseractMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.contrib.java.lang.system.RestoreSystemProperties
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

  // Creates a temporary directory that is cleaned up after the test suite
  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder()

  // Allows us to make detailed assertions about thrown exceptions
  @Rule
  public ExpectedException thrown = ExpectedException.none()

  // This rule automatically restores any system properties we set in test cases
  @Rule
  public RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

  // Allows us to set env vars that are automatically cleaned up between tests
  @Rule
  public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Before
  void setUp() {
    Util.enableColorization()

    TestUtil.mockUtilClass(tmpDir)

    TestUtil.mockTesseractMain()
  }

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
  void testConfigStoreReadsFileAtConfigPath() {
    TestUtil.mockConfigFile(tmpDir, [initialPlaylist: 'Something'])

    TestUtil.createMockPlaylists()

    // Don't use singleton or it affects every test!
    ConfigStore store = ConfigStore.get()

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
    ConfigStore.get().getString('initialPlaylist')
  }

  @Test
  void testConfigStoreReadsConfigFileInRepoByDefault() {
    TestUtil.createMockPlaylists(displayName: 'Color Cube')
    assertThat ConfigStore.get().getString('initialPlaylist'), equalTo('Color Cube')
  }

  @Test
  void testConfigStoreTransformsInitialPlayStateToUppercase() {
    TestUtil.createMockPlaylists(displayName: 'Color Cube')
    TestUtil.mockConfigFile(tmpDir, [initialPlaylist: 'Color Cube', initialPlayState: 'loop_scene'])
    assertThat ConfigStore.get().getString('initialPlayState'), equalTo('LOOP_SCENE')
  }

  @Test
  void testConfigStoreThrowsExceptionForInvalidPlayStateValue() {
    TestUtil.createMockPlaylists(displayName: 'Color Cube')
    TestUtil.mockConfigFile(tmpDir, [initialPlaylist: 'Color Cube', initialPlayState: 'some_random_thing'])

    // Expect an exception to be thrown w/ a specific msg
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("ERROR: Failed validation of option 'initialPlayState': PlayState 'SOME_RANDOM_THING' is invalid.  Must be one of 'playing', 'loop_scene', or 'stopped'");

    ConfigStore.get().getString('initialPlayState')
  }

  @Test
  void testConfigStoreUsesDefaultValuesIfConfigFileNotFound() {
    TestUtil.createMockPlaylists(displayName: 'Color Cube')

    System.setProperty("configPath", '/some/totally/non-existent/path')

    assertThat ConfigStore.get().getString('initialPlaylist'), equalTo('Color Cube')
    assertThat ConfigStore.get().getString('initialPlayState'), equalTo('LOOP_SCENE')
  }

  @Test
  void testConfigStoreUsesDefaultValueIfConfigOptionNotDefined() {
    TestUtil.createMockPlaylists(displayName: 'Color Cube')

    TestUtil.mockConfigFile(tmpDir, [initialPlayState: 'some_random_thing'])

    assertThat ConfigStore.get().getString('initialPlaylist'), equalTo('Color Cube')
  }

  // Tests the conversion of the 'optionKey' which is camel case to the environment variable name which is prefixed with
  // 'TESSERACT_', converted to snake case, and converted to all uppercase
  @Test
  void testGetEnvVarNameForConfigOption() {
    assertThat ConfigStore.get().getEnvVarNameForConfigOption('initialPlaylist'), equalTo('TESSERACT_INITIAL_PLAYLIST')
    assertThat ConfigStore.get().getEnvVarNameForConfigOption('initialPlayState'), equalTo('TESSERACT_INITIAL_PLAY_STATE')
  }

  @Test
  void testConfigStoreCanReadConfigFromSystemProperties() {
    String mockPlaylistName = 'some random playlist name'
    TestUtil.createMockPlaylists(displayName: mockPlaylistName)
    System.setProperty('initialPlaylist', mockPlaylistName)
    System.setProperty('initialPlayState', 'stopped')

    assertThat ConfigStore.get().getString('initialPlaylist'), equalTo(mockPlaylistName)
    assertThat ConfigStore.get().getString('initialPlayState'), equalTo('STOPPED')
  }

  @Test
  void testConfigStoreCanReadConfigFromEnvVars() {
    String mockPlaylistName = 'some random playlist name'
    TestUtil.createMockPlaylists(displayName: mockPlaylistName)

    environmentVariables.set('TESSERACT_INITIAL_PLAYLIST', mockPlaylistName)
    environmentVariables.set('TESSERACT_INITIAL_PLAY_STATE', 'stopped')

    assertThat ConfigStore.get().getString('initialPlaylist'), equalTo(mockPlaylistName)
    assertThat ConfigStore.get().getString('initialPlayState'), equalTo('STOPPED')
  }

  @Test
  void testSystemPropertiesTakePrecedenceOverEnvironmentVariables() {
    String mockPlaylistName1 = 'some random playlist name'
    TestUtil.createMockPlaylists(displayName: mockPlaylistName1)

    System.setProperty('initialPlaylist', mockPlaylistName1)

    environmentVariables.set('TESSERACT_INITIAL_PLAYLIST', 'non-existent playlist')

    assertThat ConfigStore.get().getString('initialPlaylist'), equalTo(mockPlaylistName1)
  }

  @Test
  void testEnvVarsTakePrecedenceOverConfigFile() {
    String mockPlaylistName1 = 'some random playlist name'
    TestUtil.createMockPlaylists(displayName: mockPlaylistName1)

    environmentVariables.set('TESSERACT_INITIAL_PLAYLIST', mockPlaylistName1)

    TestUtil.mockConfigFile(tmpDir, [initialPlaylist: 'non-existent playlist'])

    assertThat ConfigStore.get().getString('initialPlaylist'), equalTo(mockPlaylistName1)
  }

  // Here we define some of the options in multiple ways and ensure everything works out with the correct precedence
  @Test
  void testCanReadConfigsFromBothSystemPropertiesAndEnvVars() {
    String mockPlaylistName1 = 'some random playlist name'
    TestUtil.createMockPlaylists(displayName: mockPlaylistName1)

    environmentVariables.set('TESSERACT_INITIAL_PLAYLIST', mockPlaylistName1)

    System.setProperty('initialPlayState', 'playing')

    // Both of these should be ignored
    TestUtil.mockConfigFile(tmpDir, [initialPlaylist: 'non-existent playlist', initialPlayState: 'stopped'])

    assertThat ConfigStore.get().getString('initialPlaylist'), equalTo(mockPlaylistName1)
    assertThat ConfigStore.get().getString('initialPlayState'), equalTo('PLAYING')
  }
}
