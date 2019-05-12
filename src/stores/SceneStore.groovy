package stores

import clip.AbstractClip
import clip.SolidColorClip
import clip.Stripe
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import mock.AnimatedGifClip
import mock.ColorWashClip
import mock.ImageScrollClip
import mock.MockData
import mock.TextScrollClip
import show.Playlist
import show.Scene
import state.IJsonPersistable
import util.Util

import static util.Util.pp

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

  Scene find(String property, value) {
    Scene item = items.find { item -> item."${property}" == value }
    if (!item) {
      throw new RuntimeException("Error: could not find scene matching '${property}: ${value}'")
    }
    item
  }

  // This probably isn't the best spot for this but it will work for now
  AbstractClip createClipById(String clipId) {
    // map of clipId to class
    Map clipIdMap = [
        animated_gif: AnimatedGifClip,
        color_wash  : ColorWashClip,
        image_scroll: ImageScrollClip,
        text_scroll : TextScrollClip,
        solid_color : SolidColorClip,
        stripe      : Stripe,
    ]

    def clazz = clipIdMap[clipId]

    if (!clazz) {
      throw new RuntimeException("Error: No matching class for clipId: ${clipId}")
    }

    clazz.newInstance('my-clip-name', 1) as AbstractClip
  }

  // Creates a Scene object from the JSON representation
  // Steps:
  // - Match keys on json object to constructor parameters
  // - Hydrate references to objects (e.g., 'clipId: 1' somehow matches the clip...maybe store a map that points clipId to class right now?)
  Scene createSceneFromJson(jsonObj) {
//    println "createSceneFromJSON!!!".cyan()
//    pp jsonObj

    // Match Scenes with Clips and instantiate (need to set the control values as well eventually)
    AbstractClip channel1Clip = this.createClipById(jsonObj.channel1Clip.clipId)
    AbstractClip channel2Clip = this.createClipById(jsonObj.channel2Clip.clipId)

    new Scene(jsonObj.id, jsonObj.displayName, channel1Clip, channel2Clip)
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
      println "No datafile found, using mock scene data"
      return MockData.getSceneStoreData()
    }

    println "Loading scene data from Disk!".yellow()
    // this could use better protection around bad data
    new JsonSlurper().parseText(dataFile.text) as List<Map>
  }

  // Save current state to disk as JSON
  void saveDataToDisk() {
    println "Writing Scene Data to Disk".yellow()

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
          id          : item.id,
          displayName : item.displayName,
          channel1Clip: [clipId: item.channel1Clip.clipId, controlSetId: 999],
          channel2Clip: [clipId: item.channel2Clip.clipId, controlSetId: 999],
      ]
    }
  }
}
