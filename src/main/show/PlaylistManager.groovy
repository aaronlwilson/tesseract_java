package show

import model.Channel
import show.Playlist.PlayState
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

  public PlayState getCurrentPlayState() {
    return this.currentPlaylist.getCurrentPlayState()
  }

  // figure out how long we have until our timer expires
  public long getCurrentSceneDurationRemaining() {
    this.currentPlaylist.getCurrentSceneDurationRemaining()
  }


  // Remove Scene from all Playlists, and change the current playState to the next Scene
  // When we delete a scene, we need to ensure the data stays consistent by removing it from all playlists or we'll get errors
  public void removeSceneFromPlaylists(Scene s) {
    println "[PlaylistManager] Removing Scene '${s.displayName}' from all playlists"
    PlaylistStore.get().items.each { playlist ->
      playlist.items.removeAll { PlaylistItem i -> i.scene.id == s.id }
    }

    PlaylistStore.get().saveDataToDisk()

    this.getCurrentPlaylist().playNext()
  }

  // Get the initial playlist (or non-initial, or whatever)
  public Playlist getInitialPlaylist(Integer playlistId) {
    Playlist p;
    if (!playlistId) {
      p = this.playlistStore.getItems().first()
      if (!p) throw new RuntimeException("Error: PlaylistManager: PlaylistStore is empty!")
    } else {
      p = this.playlistStore.find('id', playlistId)
    }

    p
  }

  public void stop(Integer playlistId = null, String playlistItemId = null) {
    // for now, just handle playing the first playlist and the first scene
    Playlist p = this.getInitialPlaylist(playlistId)

    if (this.currentPlaylist != p) {
      //this should be wrapped by a "debug" setting
      //println "[PlaylistManager: Switching playlist from ${this.currentPlaylist.displayName} to ${this.currentPlaylist.displayName}"
      this.currentPlaylist = p;
      this.channel.unsetScene()
    }

    p.setChannel(this.channel);
    this.getCurrentPlaylist().stop(false)

  }

  // Play a specific playlist, set the play state
  public play(Integer playlistId = null, String playlistItemId = null, PlayState playState) {
    Playlist p = this.getInitialPlaylist(playlistId)

    //this should be wrapped by a "debug" setting
    //println "[PlaylistManager] Playing playlist '${p.getDisplayName()}'. PlayState: ${playState}"

    if (this.currentPlaylist != p) {
      // Unset the channel on the old playlist, and set it on the new playlist
      if (this.currentPlaylist) {
        this.currentPlaylist.unload()
      }
      this.currentPlaylist = p;
    }

    p.setChannel(this.channel);

    p.play(playlistItemId, playState);
  }
}
