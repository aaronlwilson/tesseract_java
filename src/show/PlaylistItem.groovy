package show

// Playlist item: One scene + one duration.  and any other fields we decide we want
class PlaylistItem {
  Scene scene
  Integer duration

  PlaylistItem(Scene scene, Integer duration) {
    this.scene = scene
    this.duration = duration
  }
}
