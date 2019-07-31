package stores

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import show.Playlist
import show.PlaylistItem
import show.Scene
import state.IJsonPersistable
import util.Util

class PlaylistStore extends BaseStore implements IJsonPersistable {
  public static PlaylistStore instance

  // List of PlaylistItems
  private List<Playlist> items = []

  public PlaylistStore() {
    List<Map> data = this.loadDataFromDisk()
    refreshFromJS(data)

    // save right when we load for testing
    // this.saveDataToDisk()
  }

  // Singleton
  public static PlaylistStore get() {
    if (instance == null) {
      instance = new PlaylistStore()
    }

    instance
  }

  public List<Playlist> getItems() {
    return this.items;
  }

  public void setItems(List<Playlist> items) {
    this.items = items;
  }

  // Add or update a playlist in the store.  If the ID of the scene already exists, it will update the values
  public addOrUpdate(Playlist playlist) {
    Playlist existingPlaylist = this.find("id", playlist.id)
    if (existingPlaylist) {
      existingPlaylist.setDisplayName(playlist.getDisplayName())
      existingPlaylist.setDefaultDuration(playlist.getDefaultDuration())
      existingPlaylist.setItems(playlist.getItems())
    } else {
      this.items.push(playlist)
    }
  }

  public Playlist find(String property, value) {
    items.find { item -> item."${property}" == value }
  }

  // Creates a Playlist object from the JSON representation
  // Steps:
  // - Match keys on json object to constructor parameters
  // - Hydrate references to objects (e.g., 'sceneId: 1' becomes the actual Scene object from the appropriate store
  public Playlist createPlaylistFromJson(jsonObj) {
    List<PlaylistItem> playlistItems = jsonObj.items.collect { Map playlistItem ->
      // Find scene in scene store
      Scene scene = SceneStore.get().find 'id', playlistItem.sceneId

      // create new id for playlist item if one doesn't exist
      String playlistItemId = playlistItem.id ?: UUID.randomUUID();

      new PlaylistItem(playlistItemId, scene, playlistItem.duration)
    }

    new Playlist(jsonObj.id, jsonObj.displayName, jsonObj.defaultDuration, playlistItems)
  }

  // Takes an array of parsed JSON
  public void refreshFromJS(List arr) {
    this.items = arr.collect { o -> createPlaylistFromJson(o) }
  }

  // Load the JSON data from the disk and parse JSON
  public List<Map> loadDataFromDisk() {
    File dataFile = new File(Util.getDataFilePath('playlist'))
    if (!dataFile.exists()) {
      return []
    }

    println "Loading playlist data from Disk".yellow()
    // this could use better protection around bad data
    new JsonSlurper().parseText(dataFile.text) as List<Map>
  }

  // Save current state to disk as JSON
  public void saveDataToDisk() {
    println "Writing Playlist Data to Disk".yellow()

    String filename = Util.getDataFilePath('playlist')

    List<Map> jsonObj = this.asJsonObj()

    new File(filename).write "${new JsonBuilder(jsonObj).toPrettyString()}\n"
    println "Wrote Playlist Data to Disk".green()
  }

  // Get the store data as JSON, either for persisting or sending to the front end
  // This will be a List/Map that serializes to the correct JSON, rather than the JSON string itself
  public List<Map> asJsonObj() {
    this.items.collect { Playlist item ->
      [
          id             : item.id,
          displayName    : item.displayName,
          defaultDuration: item.defaultDuration,
          items          : item.items.collect { i -> [id: i.id, sceneId: i.scene.id, duration: i.duration] },
      ]
    }
  }
}
