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
  public void pause() {
    this.currentPlaylist.pause()
  }

  public void stop() {
    this.currentPlaylist.stop()
  }

  // Play a specific playlist
  // This API will probably change
  public play(Integer playlistId = null, int itemIdx = 0, Playlist.PlayState initialPlayState = Playlist.PlayState.PLAYING) {
    Playlist p;

    // for now, just handle playing the first playlist and the first scene
    if (!playlistId) {
      p = this.playlistStore.getItems().first()
      if (!p) throw new RuntimeException("Error: PlaylistManager: PlaylistStore is empty!")
    } else {
      p = this.playlistStore.find('id', playlistId)
    }

    println "[PlaylistManager] Playing playlist '${p.getDisplayName()}'. PlayState: ${p.getCurrentPlayState()}"

    // will implement when getting playlist switching working
//    if (this.currentPlaylist) {
//      this.currentPlaylist.unsetChannel()
//    }

    if (this.currentPlaylist != p) {
      this.currentPlaylist = p;
      this.channel.unsetScene()
      p.setChannel(this.channel);
    }

    if (initialPlayState == Playlist.PlayState.PLAYING) {
      p.play();
    } else if (initialPlayState == Playlist.PlayState.PAUSED) {
      p.pause();
    } else {
      p.stop();
    }
  }
}
