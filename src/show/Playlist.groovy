package show;

class Playlist {

  // Playlist item: One scene + one duration.  and any other fields we decide we want
  class PlaylistItem {
    Scene scene
    Integer duration

    PlaylistItem(Scene scene, Integer duration) {
      this.scene = scene
      this.duration = duration
    }
  }

  String displayName

  List<PlaylistItem> items

  Integer defaultDuration

  Playlist(String displayName, Integer defaultDuration = 60, List<PlaylistItem> items = []) {
    this.displayName = displayName
    this.defaultDuration = defaultDuration
    this.items = items
  }
}
