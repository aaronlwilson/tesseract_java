package state

import app.TesseractMain
import clip.AbstractClip
import clip.ClipMetadata
import org.java_websocket.WebSocketImpl
import show.Playlist
import show.Scene
import stores.MediaStore
import stores.PlaylistStore
import stores.SceneStore
import show.PlaylistManager
import websocket.WebsocketInterface

// State manager is responsible for managing application state and synchronizing state
// between client(s) and server
class StateManager {
  public static StateManager instance;

  WebsocketInterface ws = WebsocketInterface.get()

  public StateManager() {
    this.registerHandlers()
  }

  // Singleton
  public static StateManager get() {
    if (instance == null) {
      instance = new StateManager()
    }

    instance
  }

  public registerHandlers() {
    ws.registerActionHandler('requestInitialState', this.&sendInitialState)
    ws.registerActionHandler('stateUpdate', this.&handleStateUpdate)
  }

  // Returns a reference to the live clip instance.  this might be currentScene or nextScene on the channel, depending if we are transitioning
  private AbstractClip getActiveClip() {
    TesseractMain.getMain().channel1.getActiveClip()
  }

  // Get the current values of a clip
  // Return a Map like this [ field1: value1, field2: value2, etc ]
  private getClipControlValues(AbstractClip clip) {
    // If we're stopped, these values will be null
    if (clip == null) {
      return [clipId: null, values: null]
    }

    Map clipMeta = ClipMetadata.getClipMetadata().find { it.clipId == clip.clipId }

    Map values = clipMeta.controls.inject([:]) { Map result, Map data ->
      // If the value is defined, use the current value.  if the value is null, use the default value
      def newValue = clip."${data.fieldName}" == null ? data.defaultValue : clip."${data.fieldName}"
      result[data.fieldName] = newValue
      result
    }

    [clipId: clip.clipId, values: values]
  }

  // it would be get to get the current values of the controls here too
  // This gets the 'active state' of the application
  // Things like which playlist / scene are we playing, stuff like that
  // We also want to send the current values of the clip controls
  public Map getActiveState() {
    String playlistItemId = PlaylistManager.get().getCurrentPlaylist()?.getCurrentItem()?.getId()

    // This is to help track down any issues with telling the UI to play a playlist item that no longer exists
    if (playlistItemId != null) {
      Playlist containingPlaylist = PlaylistStore.get().items.find { p ->
        p.items.find { item -> item.id == playlistItemId }
      }

      if (containingPlaylist == null) {
        throw new RuntimeException("Error: active playlist item does not belong to a playlist");
      }
    }

    // It doesn't matter if a bunch of these are null.  If the UI requests the data before we've played a playlist,
    // there won't be a currentPlaylist.  When we start playing a playlist, we will update the activeState in the UI
    Map activeState = [
            playlistItemId               : playlistItemId,
            playlistId                   : PlaylistManager.get().getCurrentPlaylist()?.getId(),
            currentSceneDurationRemaining: PlaylistManager.get().getCurrentSceneDurationRemaining(),
            playlistPlayState            : PlaylistManager.get().getCurrentPlaylist()?.getCurrentPlayState()?.name(),
            clipControlValues            : this.getClipControlValues(this.getActiveClip())
    ]

    return activeState
  }

  public void sendActiveState() {
    this.sendStateUpdate("activeState", this.getActiveState());
  }

  // Sends the state of the relevant objects to the front end for initial hydration
  public void sendInitialState(WebSocketImpl conn, Map inData) {
    println "[StateManager] Sending initial state to Client".cyan()

    Map data = [
            clipData    : ClipMetadata.getClipMetadata(),
            sceneData   : SceneStore.get().asJsonObj(),
            playlistData: PlaylistStore.get().asJsonObj(),
            mediaData   : MediaStore.get().asJsonObj(),
            activeState : this.getActiveState(),
    ]

    ws.sendMessage(conn, 'sendInitialState', data);
  }

  // Refresh the Scenes and Playlists
  // Necessary after deleting a Scene or Playlist
  public void sendStoreRefresh() {
    println "[StateManager] Sending store refresh to Clients".cyan()

    Map data = [
            sceneData   : SceneStore.get().asJsonObj(),
            playlistData: PlaylistStore.get().asJsonObj(),
    ]

    this.sendStateUpdate('storeRefresh', data)
  }

  // A 'stateUpdate' event means something changed in the
  // state on the backend and we need to update the frontend to reflect the change
  // Send this to all clients for now
  // In the future, we will want something like 'send to all clients except one'
  public void sendStateUpdate(String stateKey, value) {
    println "[StateManager] Sending stateUpdate event: ${stateKey} ${value}".cyan()

    def data = [
            key  : stateKey,
            value: value,
    ]

    ws.broadcastMessage('stateUpdate', data)
  }

  // Handle receiving a stateUpdate event from a client
  public void handleStateUpdate(conn, inData) {
    if (inData.stateKey == "activeControls") {
      this.handleActiveControlsUpdate(inData.value);
    } else if (inData.stateKey == "playlist") {
      this.handlePlaylistUpdate(inData.value);
    } else if (inData.stateKey == "scene") {
      this.handleSceneUpdate(inData.value);
    } else if (inData.stateKey == "sceneDelete") {
      this.handleSceneDelete(inData.value);
    } else if (inData.stateKey == "playState") {
      this.handlePlayStateUpdate(inData.value);
    } else {
      // The reason I use RuntimeException is because they can't be caught (by Processing), so you are always guaranteed to see the stack trace
      throw new RuntimeException("Error: No handler for state key '${inData.stateKey}'")
    }

    // todo: here is where I would determine if the stateUpdate should be broadcast to other clients and send the data
  }

  // Handle an update to one of the active controls
  public void handleActiveControlsUpdate(Map inData) {
    // find the active clip.  this is gonna be kinda hacky for now
    AbstractClip clip = this.getActiveClip()
    String fieldName = inData.fieldName

    // Handle video file changes, they are special
    if (inData.fieldName == 'filename') {
      String newValue = inData.newValue;
      clip.setFilename(newValue)
      return
    }

    // Handle all other clip control value changes (floats)
    float newValue = inData.newValue

    // Set the field in 'fieldName' to the value in 'newValue'
    // e.g., this will set 'p1' to '0.589378' or whatever the Control on the frontend is set to do
    // Groovy is cool because we can do stuff like this: obj."${variableHoldingFieldName}" to dynamically set a property on an object
    clip."${fieldName}" = newValue

//    println "Set clip field '${fieldName}' to value '${newValue}'"
  }

  // Create a new playlist object and shove it into the store, then write data to disk
  public void handlePlaylistUpdate(Map inData) {
    Playlist p = PlaylistStore.get().createPlaylistFromJson(inData)
    PlaylistStore.get().addOrUpdate(p)
    PlaylistStore.get().saveDataToDisk()
  }

  // Create a new scene object and shove it into the store, then write data to disk
  // This can change the active scene, so send an activeState update to the frontend
  public void handleSceneUpdate(Map inData) {
    Scene s = SceneStore.get().createSceneFromJson(inData)
    SceneStore.get().addOrUpdate(s)
    SceneStore.get().saveDataToDisk()
    this.sendActiveState()
  }

  // Create a new scene object and shove it into the store, then write data to disk
  // This can change the active scene, so send an activeState update to the frontend
  public void handleSceneDelete(Map inData) {
    Scene s = SceneStore.get().find('id', inData.id)

    if (s == null) {
      throw new RuntimeException("[StateManager] Could not find scene to delete with id ${inData.id}")
    }

    SceneStore.get().remove(s)
    SceneStore.get().saveDataToDisk()

    // Need to also remove the Scene from all playlists
    // This also handles playing the next item, since the current item won't exist
    PlaylistManager.get().removeSceneFromPlaylists(s)

    this.sendStoreRefresh()
    this.sendActiveState()
  }

  // Handles updates to the 'play state'
  // The playState is: whether we are playing, looping the current scene, or stopped, and the current playlistId and sceneId
  public void handlePlayStateUpdate(Map inData) {
    int playlistId = inData.activePlaylistId
    String playlistItemId = inData.activePlaylistItemId
    Playlist.PlayState playState = inData.playState as Playlist.PlayState

    // if we're already playing the correct playlist and item and we're in the correct playState, don't do anything
    // this should prevent the playlist from restarting if we click it again in the UI and we're already on it
    if (playlistId == PlaylistManager.get().getCurrentPlaylist().getId()
            && playlistItemId == PlaylistManager.get().getCurrentPlaylist().getCurrentItem()?.getId()
            && playState == PlaylistManager.get().getCurrentPlayState()) {
      println "[StateManager] Already in the correct state, don't do anything"
      return
    }

    // Just stop
    if (playState == Playlist.PlayState.STOPPED) {
      println "[StateManager] playState updated to STOPPED"
      PlaylistManager.get().stop(playlistId, playlistItemId)
      return
    }

    // If we made it this far, we should play the incoming playlist, item, and playState
    PlaylistManager.get().play(playlistId, playlistItemId, playState)
  }
}
