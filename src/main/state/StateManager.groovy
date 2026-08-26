package state

import app.TesseractApp
import clip.AbstractClip
import clip.ClipMetadata
import org.java_websocket.WebSocketImpl
import show.Playlist
import show.Scene
import stores.ConfigStore
import stores.MasterBrightnessStore
import stores.MediaStore
import stores.PlaylistStore
import stores.SceneStore
import show.PlaylistManager
import util.Util
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
    TesseractApp.get().channel1.getActiveClip()
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

    // The active item can stop belonging to any playlist — e.g. Restore to Defaults rebuilds a
    // playlist with fresh item ids, or the currently-playing item was deleted. Degrade gracefully
    // (report no active item) rather than throwing on the WS path, which is called on every
    // requestInitialState / activeState broadcast.
    if (playlistItemId != null) {
      Playlist containingPlaylist = PlaylistStore.get().items.find { p ->
        p.items.find { item -> item.id == playlistItemId }
      }

      if (containingPlaylist == null) {
        System.err.println("[StateManager] Active playlist item '${playlistItemId}' no longer belongs to any playlist; reporting no active item")
        playlistItemId = null
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
            // Immutable for the process lifetime (set once from config at boot), so it's sent
            // here rather than in activeState, which gets rebroadcast on every mutation.
            stageType   : ConfigStore.get().getString("stageType"),
            // Global (non-clip-scoped) master brightness filter, current values for hydration.
            // Live updates come separately via a 'masterBrightness' stateUpdate broadcast.
            masterBrightness: MasterBrightnessStore.get().asJsonObj(),
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
    // Canonical protocol uses 'key' as the discriminator (matches the outbound stateUpdate shape).
    // Accept the legacy 'stateKey' as a fallback so older clients keep working.
    def key = inData.key != null ? inData.key : inData.stateKey

    if (key == "activeControls") {
      this.handleActiveControlsUpdate(inData.value);
    } else if (key == "playlist") {
      this.handlePlaylistUpdate(inData.value);
    } else if (key == "scene") {
      this.handleSceneUpdate(inData.value);
    } else if (key == "scenes") {
      this.handleScenesReplaceAll(inData.value);
    } else if (key == "playlists") {
      this.handlePlaylistsReplaceAll(inData.value);
    } else if (key == "sceneDelete") {
      this.handleSceneDelete(inData.value);
    } else if (key == "restoreDefaults") {
      this.handleRestoreDefaults();
    } else if (key == "playState") {
      this.handlePlayStateUpdate(inData.value);
    } else if (key == "masterBrightness") {
      this.handleMasterBrightnessUpdate(inData.value);
    } else {
      // Don't throw — a malformed/unknown message from a client must not take down the WS thread.
      System.err.println("[StateManager] Ignoring stateUpdate with unknown key '${key}'")
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

  // Handle an update to the global master brightness filter (not tied to any clip/scene).
  // Broadcasts the full {r,g,b} triple back out so every connected client (e.g. phone + another
  // open tab) stays in sync, rather than just the one channel that changed.
  public void handleMasterBrightnessUpdate(Map inData) {
    String channel = inData.channel
    double newValue = inData.newValue

    MasterBrightnessStore.get().setChannel(channel, newValue)
    MasterBrightnessStore.get().saveDataToDisk()

    this.sendStateUpdate("masterBrightness", MasterBrightnessStore.get().asJsonObj())
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

  // Replace-all reconcile of the full scene set from the UI (edits + adds). Scene *deletion* is NOT
  // done here — it goes through handleSceneDelete (key 'sceneDelete') so playlists get their
  // references cleaned up; this path only add-or-updates, never removes, to avoid orphaning
  // playlist items. Persists silently (no storeRefresh broadcast) so a client applying its own edit
  // doesn't get re-hydrated and echo the same edit back in a loop.
  public void handleScenesReplaceAll(incoming) {
    if (!(incoming instanceof List)) {
      System.err.println("[StateManager] 'scenes' replace-all: expected a List, ignoring")
      return
    }
    incoming.each { sceneJson ->
      Scene s = SceneStore.get().createSceneFromJson(sceneJson)
      SceneStore.get().addOrUpdate(s)
    }
    SceneStore.get().saveDataToDisk()
    // activeState (not storeRefresh) so an edit to the live scene re-syncs the control panel without
    // rebuilding the client stores.
    this.sendActiveState()
  }

  // Replace-all reconcile of the full playlist set from the UI. Playlists aren't referenced by
  // anything else, so delete-by-omission is safe and covers whole-playlist deletion (PL3). Persists
  // silently (no storeRefresh broadcast) to avoid the edit-echo loop. Ids are normalized to String
  // for the presence check so Integer/Long/BigInteger JSON parsing can't mis-match built-in ids.
  public void handlePlaylistsReplaceAll(incoming) {
    if (!(incoming instanceof List)) {
      System.err.println("[StateManager] 'playlists' replace-all: expected a List, ignoring")
      return
    }
    // Guard against wiping everything on an empty/malformed payload.
    if (incoming.isEmpty()) {
      System.err.println("[StateManager] 'playlists' replace-all: empty list, ignoring to avoid wiping all playlists")
      return
    }

    Set<String> incomingIds = incoming.collect { it.id?.toString() } as Set
    List toRemove = PlaylistStore.get().getItems().findAll { !incomingIds.contains(it.id?.toString()) }
    toRemove.each { PlaylistStore.get().remove(it) }

    incoming.each { playlistJson ->
      Playlist p = PlaylistStore.get().createPlaylistFromJson(playlistJson)
      PlaylistStore.get().addOrUpdate(p)
    }
    PlaylistStore.get().saveDataToDisk()
    this.sendActiveState()
  }

  // Restore to Defaults: overwrite the built-in scenes/playlists back to their code defaults and
  // regenerate the video-derived items, LEAVING user-created custom scenes/playlists intact. Then
  // persist and broadcast a storeRefresh so every client re-hydrates to the restored set.
  public void handleRestoreDefaults() {
    println "[StateManager] Restoring built-in scenes/playlists to defaults".yellow()
    Util.createBuiltInScenes(true)
    Util.createBuiltInPlaylists(true)
    SceneStore.get().saveDataToDisk()
    PlaylistStore.get().saveDataToDisk()
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
