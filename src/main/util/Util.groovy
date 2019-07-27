package util

import app.TesseractMain
import groovy.json.JsonBuilder
import show.Playlist
import show.Playlist.PlayState
import show.PlaylistItem
import show.Scene
import stores.MediaStore
import stores.PlaylistStore
import stores.SceneStore

public class Util {

  // This function will add several functions to the String class, allowing us to print messages w/ colors
  public static enableColorization() {
    String.metaClass.color_code = { code -> "${(char) 27}[${code}m" }
    String.metaClass.colorize = { code -> "${delegate.color_code(code)}${delegate.replace(delegate.color_code(0), delegate.color_code(code))}${delegate.color_code(0)}" }
    String.metaClass.red = { -> delegate.colorize(31) }
    String.metaClass.green = { -> delegate.colorize(32) }
    String.metaClass.yellow = { -> delegate.colorize(33) }
    String.metaClass.blue = { -> delegate.colorize(34) }
    String.metaClass.magenta = { -> delegate.colorize(35) }
    String.metaClass.cyan = { -> delegate.colorize(36) }
  }

  // Ensure data dir exists
  public static initDataDir() {
    String path = "${new File(".").getAbsoluteFile().getParent()}/data"
    File dirFile = new File(path)
    if (!dirFile.exists()) {
      println "Created data directory at ${path}"
      dirFile.mkdir()
    }

    initGroupDir('default')
  }

  // Ensure data dir exists
  public static initGroupDir(String group) {
    String path = Util.getDataDir(group)
    File dirFile = new File(path)
    if (!dirFile.exists()) {
      println "Created group directory at ${path}"
      dirFile.mkdir()
    }
  }

  // Returns the directory we are going to use to store json files
  // 'group' will be a way we can have different sets of files
  // creates any necessary directories to ensure the path exists
  public static String getDataDir(String group = 'default') {
    // this returns '<repo dir>/data/<group>'
    String dirPath = "${new File(".").getAbsoluteFile().getParent()}/data/${group}"
    File dir = new File(dirPath)
    if (!dir.exists()) {
      dir.mkdirs()
    }

    dir.getCanonicalPath()
  }

  // Get the data file path for a type
  // types: ClipControl, Playlist, Scene
  public static String getDataFilePath(String type, String group = 'default') {
    "${getDataDir(group)}/${type}.json"
  }

  // The root data directory (e.g. repo/data)
  public static String getRootDataDir() {
    new File("./data").getCanonicalPath()
  }

  // returns relative paths to all files in the directory (relative to the root directory)
  public static getMediaFileList(String type) {
    String rootPath = "${Util.getRootDataDir()}/${type}"

    // Return empty array if video directory doesn't exist
    if (!new File(rootPath).isDirectory()) {
      return []
    }

    List<String> res = []

    new File(rootPath).eachFileRecurse(groovy.io.FileType.FILES) { File file ->
      res.push(file.getCanonicalPath().replace("${rootPath}/", ''))
    }

    return res
  }

  // Pretty print a complex object.  doesn't work for objects w/ cyclical references, you can use obj.dump() and obj.inspect() on complex objects
  public static void pp(o) {
    println new JsonBuilder(o).toPrettyString()
  }

  public static int getClipEnumValue(String clipId) {
    // map of clipId to ENUM value
    Map clipIdMap = [
        color_wash   : TesseractMain.COLORWASH,
        node_scan    : TesseractMain.NODESCAN,
        solid_color  : TesseractMain.SOLID,
        video        : TesseractMain.VIDEO,
        particle_clip: TesseractMain.PARTICLE,
        perlin_noise : TesseractMain.PERLINNOISE,
    ]

    Integer enumVal = clipIdMap[clipId]

    if (enumVal == null) {
      throw new RuntimeException("Error: No matching class for clipId: ${clipId}")
    }

    enumVal
  }

  //COLOR utility methods
  //c in this case is a processing type "color" which is really just a 32 bit integer
  public static int getR(int c) {
    return c >> 16 & 0xFF;
  }

  public static int getG(int c) {
    return c >> 8 & 0xFF;
  }

  public static int getB(int c) {
    return c & 0xFF;
  }


  public static float getPercent(int loaded, int total) {
    return ((float) loaded / total) * 100;
  }

  //TODO move to another static class
  public static void createBuiltInPlaylists() {
    // Arrays.asList makes an immutable list, creating a new LinkedList with those items will make it mutable which we need
    List<PlaylistItem> playlist1Items = new LinkedList<>(Arrays.asList(
        new PlaylistItem(UUID.randomUUID().toString(), 'Color Wash', 4),
        new PlaylistItem(UUID.randomUUID().toString(), 'Yellow', 10),
        new PlaylistItem(UUID.randomUUID().toString(), 'Red', 3),
        new PlaylistItem(UUID.randomUUID().toString(), 'Color Wash', 4),
        new PlaylistItem(UUID.randomUUID().toString(), 'Yellow', 4),
        new PlaylistItem(UUID.randomUUID().toString(), 'Purple', 4),
        new PlaylistItem(UUID.randomUUID().toString(), 'Color Wash', 4),
        new PlaylistItem(UUID.randomUUID().toString(), 'Red', 3),
        new PlaylistItem(UUID.randomUUID().toString(), 'Yellow', 5),
        new PlaylistItem(UUID.randomUUID().toString(), 'Color Wash', 4),
        new PlaylistItem(UUID.randomUUID().toString(), 'Purple', 5),
        new PlaylistItem(UUID.randomUUID().toString(), 'Red', 7)
    ));

    Playlist playlist1 = new Playlist(1, "Cubotron", 60, playlist1Items);
    PlaylistStore.get().addOrUpdate(playlist1);

    List<PlaylistItem> playlist2Items = new LinkedList<>(Arrays.asList(
        new PlaylistItem(UUID.randomUUID().toString(), 'PerlinNoise', 10),
        new PlaylistItem(UUID.randomUUID().toString(), 'Particles', 10),
        new PlaylistItem(UUID.randomUUID().toString(), 'Purple', 4),
        new PlaylistItem(UUID.randomUUID().toString(), 'Red', 3),
        new PlaylistItem(UUID.randomUUID().toString(), 'Yellow', 4)
    ));

    Playlist playlist2 = new Playlist(2, "Color Cube", 60, playlist2Items);
    PlaylistStore.get().addOrUpdate(playlist2);

    // Determine if there are any videos loaded.  If so, create a playlist containing them all.  If not, delete the playlist if it exists
    List<Scene> allVideoScenes = SceneStore.get().getItems()
        .findAll { scene -> scene.clip.clipId == 'video' }

    if (allVideoScenes.size() > 0) {
      // Create playlist of all videos
      List<PlaylistItem> playlist3Items = allVideoScenes
          .collect { scene -> new PlaylistItem(UUID.randomUUID().toString(), scene.getDisplayName(), 60 * 1) }

      PlaylistStore.get().addOrUpdate(new Playlist(3, "All Videos", 60 * 1, playlist3Items))
    } else {
      // delete video playlist
      Playlist videoPlaylist = PlaylistStore.get().find('displayName', 'All Videos')
      PlaylistStore.get().getItems().remove(videoPlaylist)
    }


    // Save the created data to disk so we persist our manually created scenes/playlists
    // This also has the effect of resetting any changes we make to them in the UI once we start the backend
  }

  public static void createBuiltInScenes() {
    // These are hydrated from the json now.  creating them here will update the existing data in the store, but this can be commented out and it will load entirely from disk
    // If we specify the id in the constructor and it matches an existing Scene, it will update the data.  omitting the ID from the constructor will use the max id + 1 for the new scene
    List<Scene> scenes = [
        new Scene(1, "Yellow", TesseractMain.SOLID, [0, 0, 0, 1, 1, 0, 0, 0] as float[]),
        new Scene(2, "Purple", TesseractMain.SOLID, [0, 0, 0, 1, 0, 1, 0, 0] as float[]),
        new Scene(3, "Red", TesseractMain.SOLID, [0, 0, 0, 1, 0, 0, 0, 0] as float[]),
        new Scene(4, "Color Wash", TesseractMain.COLORWASH, [0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0] as float[]),
        new Scene(5, "Node Scanner", TesseractMain.NODESCAN, [0, 0, 0, 0, 0, 0, 0, 0] as float[]),
        new Scene(6, "Particles", TesseractMain.PARTICLE, [0.5f, 0.5f, 0.5f, 0.0f, 0.5f, 1, 1, 1] as float[]),
        new Scene(7, "PerlinNoise", TesseractMain.PERLINNOISE, [0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0, 0, 0] as float[]),
    ]

    scenes.each { SceneStore.get().addOrUpdate(it); }

    // Check to see if we have videos before blindly trying to create scenes.  TODO: improve this logic.  do better at removing scenes for video files that no longer exist
    List<String> allVideos = MediaStore.get().getMediaOfType('videos')
    if (allVideos.size() > 0) {
      int nextIdx = 8

      List<Scene> videoScenes = MediaStore.get().getMediaOfType('videos').collect { String videoPath ->
        Scene s = new Scene(nextIdx, videoPath, TesseractMain.VIDEO, [0, 0, 0, 0, 0, 0, 0, 0] as float[], videoPath)
        nextIdx++
        s
      }

      scenes.addAll(videoScenes)
    } else {
      // remove all video scenes.  this should be improved, we should remove any scenes for videos that don't exist
      SceneStore.get().getItems()
          .findAll { scene -> scene.clip.clipId == 'video' }
          .each { scene -> SceneStore.get().remove(scene) }
    }
  }

  public static void throwException(String msg) {
    throw new RuntimeException(msg)
  }

  // Transform a string playState to the enum.  Way easier to do it here than in Java
  public static PlayState getPlayState(String playStateStr) {
    playStateStr as PlayState
  }
}
