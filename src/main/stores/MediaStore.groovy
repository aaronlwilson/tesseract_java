package stores

import util.Util

// Handles loading the media filenames from the data directories
class MediaStore {
  public static MediaStore instance

  // Items is a map of String
  public Map<String, List<String>> items = [:]

  public MediaStore() {
    this.items['videos'] = Util.getMediaFileList('videos')
  }

  // Singleton
  public static MediaStore get() {
    if (instance == null) {
      instance = new MediaStore()
    }

    instance
  }

  public boolean containsMedia(String type, String path) {
    !!this.items[type]?.contains(path)
  }

  public List<String> getMediaOfType(String type) {
    this.items[type]
  }

  // Get the store data as JSON, either for persisting or sending to the front end
  // This will be a List/Map that serializes to the correct JSON, rather than the JSON string itself
  public Map<String, List<String>> asJsonObj() {
    return this.items;
  }
}
