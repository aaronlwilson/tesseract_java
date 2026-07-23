package util

import app.TesseractApp
import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
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

        // Method to strip colors from output
        String.metaClass.stripColors = { -> delegate.replaceAll("\u001B\\[[;\\d]*m", "") }
    }

    // Ensure data dir exists
    public static initDataDir() {
        File root = new File(getRootDataDir())
        if (!root.exists()) {
            println "Created data directory at ${root}"
            root.mkdirs()
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
        String dirPath = "${getRootDataDir()}/${group}"
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

    // Cached, resolved absolute path to the root data directory.
    private static String resolvedRootDataDir = null

    // The root data directory that holds all persisted JSON + media.
    //
    // Resolution (first match wins), so a double-clicked .app works while the dev workflow is
    // unchanged. Previously this was always CWD-relative "./data"; when the packaged app launches,
    // its CWD is "/", so it tried to write /data/... which isn't writable — the setup thread threw
    // and the stage was never built (a black screen with no LED nodes).
    //   1. Explicit override: -Dtesseract.dataDir=... or TESSERACT_DATA_DIR=...
    //   2. Project-local ./data when the working directory is writable (running from the repo).
    //   3. A per-user app-support dir otherwise (packaged app): on macOS
    //      ~/Library/Application Support/Tesseract/data, else ~/.tesseract/data.
    public static String getRootDataDir() {
        if (resolvedRootDataDir != null) {
            return resolvedRootDataDir
        }

        String override = System.getProperty('tesseract.dataDir') ?: System.getenv('TESSERACT_DATA_DIR')
        if (override != null && !override.trim().isEmpty()) {
            resolvedRootDataDir = new File(override.trim()).getAbsoluteFile().getPath()
        } else {
            File projectData = new File('data').getAbsoluteFile()   // <cwd>/data
            File cwd = projectData.getParentFile()
            if (cwd != null && cwd.canWrite()) {
                resolvedRootDataDir = projectData.getPath()
            } else {
                String home = System.getProperty('user.home')
                String os = (System.getProperty('os.name') ?: '').toLowerCase()
                File appData = os.contains('mac') ?
                        new File(home, 'Library/Application Support/Tesseract/data') :
                        new File(home, '.tesseract/data')
                resolvedRootDataDir = appData.getPath()
                println "Working directory not writable; using per-user data directory: ${resolvedRootDataDir}"
            }
        }

        File root = new File(resolvedRootDataDir)
        if (!root.exists()) {
            root.mkdirs()
        }

        return resolvedRootDataDir
    }

    // returns relative paths to all files in the directory (relative to the root directory)
    public static getMediaFileList(String type) {
        File rootDir = new File("${Util.getRootDataDir()}/${type}")

        // Return empty array if the media directory doesn't exist
        if (!rootDir.isDirectory()) {
            return []
        }

        List<String> res = []

        // Relativize each file against the media dir rather than using getCanonicalPath():
        // if the media dir is a symlink (e.g. data/videos -> an external folder), getCanonicalPath()
        // resolves the link to a different absolute path than rootDir, so the prefix strip fails and
        // filenames come back as full absolute paths — which then don't resolve as data/videos/<name>.
        java.nio.file.Path rootPath = rootDir.toPath()
        rootDir.eachFileRecurse(groovy.io.FileType.FILES) { File file ->
            res.push(rootPath.relativize(file.toPath()).toString())
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
                color_wash   : TesseractApp.COLORWASH,
                node_scan    : TesseractApp.NODESCAN,
                solid_color  : TesseractApp.SOLID,
                video        : TesseractApp.VIDEO,
                particle_clip: TesseractApp.PARTICLE,
                perlin_noise : TesseractApp.PERLINNOISE,
                lines_clip   : TesseractApp.LINESCLIP,
                tiles_test_clip   : TesseractApp.TILESTEST,
        ]

        Integer enumVal = clipIdMap[clipId]

        if (enumVal == null) {
            throw new RuntimeException("Error: No matching class for clipId: ${clipId}")
        }

        enumVal
    }

    //COLOR utility methods
    //c in this case is a processing type "color" which is really just a 32 bit integer
    // @CompileStatic: these are called per-node, per-frame by every clip (e.g. LinesClip extracts
    // ~12 channels/node = ~77k calls/frame). Dynamically compiled they cost ~9.8ns/call vs ~0.85ns
    // static (~11.5x, measured), i.e. ~0.7ms/frame of pure dispatch overhead on the render thread —
    // small on a laptop, but meaningful headroom on the Raspberry Pi target. Method-level annotation
    // keeps the rest of Util (metaClass tricks, JsonBuilder, etc.) dynamic. See the same fix on
    // JavaCVVideoClip.frameToArgb (commit 0256774).
    @CompileStatic
    public static int getR(int c) {
        return c >> 16 & 0xFF;
    }

    @CompileStatic
    public static int getG(int c) {
        return c >> 8 & 0xFF;
    }

    @CompileStatic
    public static int getB(int c) {
        return c & 0xFF;
    }

    public static float randFloatRange(float min, float max) {
        Random rand = new Random();
        float result = rand.nextFloat() * (max - min) + min;
        return result;
    }

    public static float getPercent(int loaded, int total) {
        return ((float) loaded / total) * 100;
    }


    //TODO move to another static class
    // force=false (default, startup): seed-if-absent — add a built-in playlist only if its id isn't
    //   already present, so persisted UI edits to built-ins survive a restart.
    // force=true (Restore to Defaults): overwrite the built-in playlists back to these code defaults.
    // The "All Videos" playlist (id 3) mirrors the media folder and is ALWAYS regenerated in both modes.
    public static void createBuiltInPlaylists(boolean force = false) {
        // Arrays.asList makes an immutable list, creating a new LinkedList with those items will make it mutable which we need
        List<PlaylistItem> playlist1Items = new LinkedList<>(Arrays.asList(
                new PlaylistItem(UUID.randomUUID().toString(), 'Tiles Test', 30),
                new PlaylistItem(UUID.randomUUID().toString(), 'Node Scanner', 30),
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
                new PlaylistItem(UUID.randomUUID().toString(), 'Red', 7),
        ));

        Playlist playlist1 = new Playlist(1, "Cubotron", 60, playlist1Items);
        if (force || PlaylistStore.get().find('id', 1) == null) {
            PlaylistStore.get().addOrUpdate(playlist1);
        }



        List<PlaylistItem> playlist2Items = new LinkedList<>(Arrays.asList(
                new PlaylistItem(UUID.randomUUID().toString(), 'LinesClip', 30),
                new PlaylistItem(UUID.randomUUID().toString(), 'PerlinNoise', 30),
                new PlaylistItem(UUID.randomUUID().toString(), 'Particles', 30),
                new PlaylistItem(UUID.randomUUID().toString(), 'Color Wash', 30),

        ));

        Playlist playlist2 = new Playlist(2, "Color Cube", 60, playlist2Items);
        if (force || PlaylistStore.get().find('id', 2) == null) {
            PlaylistStore.get().addOrUpdate(playlist2);
        }

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

    // force=false (default, startup): seed-if-absent — add a built-in scene only if its id isn't
    //   already present, so persisted UI edits to built-ins survive a restart.
    // force=true (Restore to Defaults): overwrite the built-in scenes back to these code defaults.
    // Video scenes (ids 10+) mirror the media folder and are ALWAYS regenerated in both modes.
    public static void createBuiltInScenes(boolean force = false) {
        List<Scene> builtInScenes = [
                new Scene(1, "Yellow", TesseractApp.SOLID, [0, 0, 0, 1, 1, 0, 0, 0] as float[]),
                new Scene(2, "Purple", TesseractApp.SOLID, [0, 0, 0, 1, 0, 1, 0, 0] as float[]),
                new Scene(3, "Red", TesseractApp.SOLID, [0, 0, 0, 1, 0, 0, 0, 0] as float[]),
                new Scene(4, "Color Wash", TesseractApp.COLORWASH, [0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0] as float[]),
                new Scene(5, "Node Scanner", TesseractApp.NODESCAN, [0, 0, 0, 0, 0, 0, 0, 0] as float[]),
                new Scene(6, "Particles", TesseractApp.PARTICLE, [0.5f, 0.5f, 0.5f, 0.0f, 0.5f, 1, 1, 1] as float[]),
                new Scene(7, "PerlinNoise", TesseractApp.PERLINNOISE, [0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0, 0, 0] as float[]),
                new Scene(8, "LinesClip", TesseractApp.LINESCLIP, [0.5f, 0.5f, 0.5f, 0.0f, 0.5f, 1, 1, 1] as float[]),
                new Scene(9, "Tiles Test", TesseractApp.TILESTEST, [0, 0, 0, 0, 0, 0, 0, 0] as float[]),
        ]

        builtInScenes.each { Scene s ->
            if (force || SceneStore.get().find('id', s.id) == null) {
                SceneStore.get().addOrUpdate(s)
            }
        }

        // Video scenes mirror the media folder — always (re)generated regardless of mode.
        // TODO: improve stale-scene removal when specific video files disappear.
        List<String> allVideos = MediaStore.get().getMediaOfType('videos')
        if (allVideos.size() > 0) {
            int nextIdx = 10
            allVideos.each { String videoPath ->
                Scene s = new Scene(nextIdx, videoPath, TesseractApp.VIDEO, [0, 0, 0, 0, 0, 0, 0, 0] as float[], videoPath)
                SceneStore.get().addOrUpdate(s)
                nextIdx++
            }
        } else {
            // remove all video scenes
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
