package show;

public class Playlist {

  // Playlist item: One scene + one duration.  and any other fields we decide we want
  public class PlaylistItem {
    Scene scene;
    Integer duration;

    PlaylistItem(Scene scene, Integer duration) {
      this.scene = scene;
      this.duration = duration;
    }
  }

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
