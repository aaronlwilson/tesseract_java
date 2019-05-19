package show

import model.Channel
import show.Playlist
import stores.PlaylistStore
import websocket.WebsocketInterface

// This class will handle state update events from the UI to play the correct playlist or scene, playing and pausing the
// playlist, etc

public class PlaylistManager {
  public static PlaylistManager instance;
  private Channel channel;
  private WebsocketInterface ws = WebsocketInterface.get();

  private Playlist currentPlaylist;

  private PlaylistStore playlistStore

  public PlaylistManager() {
    this.playlistStore = PlaylistStore.get();
//    this.registerHandlers();
  }

  // Singleton
  public static PlaylistManager get() {
    if (instance == null) {
      instance = new PlaylistManager();
    }

    instance
  }

  public void setChannel(Channel channel) {
    this.channel = channel;
  }

  public Playlist getCurrentPlaylist() {
    return this.currentPlaylist;
  }

  // figure out how long we have until our timer expires
  public long getCurrentSceneDurationRemaining() {
    this.currentPlaylist.getCurrentSceneDurationRemaining()
  }

  // this will have to cancel the timer on the playlist
  public pauseCurrentPlaylist() {

  }

  public resumeCurrentPlaylist() {

  }

  // Play a specific playlist
  public playPlaylist(Integer playlistId = null, int itemIdx = 0) {
    // for now, just handle playing the first playlist and the first scene
    if (!playlistId) {
      Playlist p = this.playlistStore.getItems().first()
      if (!p) throw new RuntimeException("Error: PlaylistManager: PlaylistStore is empty!")

      if (this.currentPlaylist) {
        this.currentPlaylist.unsetChannel()
      }

      this.currentPlaylist = p;
      p.setChannel(this.channel);
      p.play(false);
    }
  }


}
