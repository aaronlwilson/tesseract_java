package show;

class Playlist {
  int id
  String displayName
  Integer defaultDuration
  List<PlaylistItem> items

  Playlist(int id, String displayName, Integer defaultDuration = 60, List<PlaylistItem> items = []) {
    this.id = id
    this.displayName = displayName
    this.defaultDuration = defaultDuration
    this.items = items
  }
}
