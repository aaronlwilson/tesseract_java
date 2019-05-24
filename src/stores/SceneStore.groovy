package stores

import app.TesseractMain
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import show.Scene
import state.IJsonPersistable
import util.Util

class SceneStore extends BaseStore implements IJsonPersistable {
  private static SceneStore instance
  private List<Scene> items = []

  public SceneStore() {
    List<Map> data = this.loadDataFromDisk()
    refreshFromJS(data)

    // save right when we load for testing
    // this.saveDataToDisk()
  }

  // Singleton
  public static SceneStore get() {
    if (instance == null) {
      instance = new SceneStore()
    }

    instance
  }

  // Add a scene to the store
  public add(Scene scene) {
    if (this.items.find { Scene s -> s.id == scene.id }) {
      throw new RuntimeException("Error: Scene with ID ${scene.id} already exists in store")
    }
    this.items.push(scene)
  }

  // Add or update a scene in the store.  If the ID of the scene already exists, it will update the values
  public addOrUpdate(Scene scene) {
    Scene existingScene = this.find("id", scene.id)
    if (existingScene) {
      existingScene.setDisplayName(scene.getDisplayName())
      existingScene.setSceneValues(scene.getSceneValues())
    } else {
      this.items.push(scene)
    }
  }

  Scene find(String property, value) {
    items.find { item -> item."${property}" == value }
  }

  // Find the next ID if we are creating a scene and not providing an ID
  int getNextId() {
    this.items.size() == 0 ? 1 : this.items.max { Scene s -> s.id }.id + 1
  }

  // This probably isn't the best spot for this but it will work for now
  int getClipEnumValue(String clipId) {
    // map of clipId to ENUM value
    Map clipIdMap = [
        color_wash : TesseractMain.COLORWASH,
        node_scan  : TesseractMain.NODESCAN,
        solid_color: TesseractMain.SOLID,
        stripe     : TesseractMain.STRIPE,
    ]

    int enumVal = clipIdMap[clipId]

    if (!enumVal) {
      throw new RuntimeException("Error: No matching class for clipId: ${clipId}")
    }

    enumVal
  }

  // Creates a Scene object from the JSON representation
  // Steps:
  // - Match keys on json object to constructor parameters
  // - Hydrate references to objects (e.g., 'clipId: 1' somehow matches the clip...maybe store a map that points clipId to class right now?)
  Scene createSceneFromJson(jsonObj) {
//    println "createSceneFromJSON!!!".cyan()
//    pp jsonObj

    // find the correct clipClass for the clipId
    int clipClass = this.getClipEnumValue(jsonObj.clipId)

    new Scene(jsonObj.id, jsonObj.displayName, clipClass, jsonObj.clipValues as float[])
  }

  // Takes an array of parsed JSON and sets the 'items' property
  void refreshFromJS(List arr) {
    // Collect maps over a list and runs a function on each item, returning the result as a new list
    this.items = arr.collect { o -> createSceneFromJson(o) }
  }

  // Load the JSON data from the disk and parse JSON
  List<Map> loadDataFromDisk() {
    File dataFile = new File(Util.getDataFilePath('scene'))
    if (!dataFile.exists()) {
      println "[SceneStore] No datafile found at '${dataFile.getAbsolutePath()}'.  Returning empty list"
      return []
    }

    println "Loading scene data from Disk".yellow()
    // this could use better protection around bad data
    new JsonSlurper().parseText(dataFile.text) as List<Map>
  }

  // Save current state to disk as JSON
  void saveDataToDisk() {
    String filename = Util.getDataFilePath('scene')

    List<Map> jsonObj = this.asJsonObj()

    new File(filename).write new JsonBuilder(jsonObj).toPrettyString()
    println "Wrote Scene Data to Disk".green()

  }

  // Get the store data as JSON, either for persisting or sending to the front end
  // This will be a List/Map that serializes to the correct JSON, rather than the JSON string itself
  List<Map> asJsonObj() {
    this.items.collect { Scene item ->
      [
          id         : item.id,
          displayName: item.getDisplayName(),
          clipId     : item.clip.clipId,
          clipValues : item.getSceneValues(),
      ]
    }
  }
}
