package show;

public class Playlist {
  int id;
  String displayName;
  Integer defaultDuration;
  List<PlaylistItem> items;

  public Playlist(int id, String displayName, Integer defaultDuration = 60, List<PlaylistItem> items = []) {
    this.id = id;
    this.displayName = displayName;
    this.defaultDuration = defaultDuration;
    this.items = items;
  }




}
