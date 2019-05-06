package state

import mock.MockData
import stores.*
import util.Util
import websocket.WebsocketInterface

// State manager is responsible for managing application state and synchronizing state
// between client(s) and server
class StateManager {

  WebsocketInterface ws
  PlaylistStore playlistStore
  SceneStore sceneStore
  ClipControlStore clipControlStore

  public StateManager() {
    Util.initDataDir()

    // These must be created in the proper order to ensure the objects exist when we try to hydrate them.  this would be fixed by using a db
    this.clipControlStore = new ClipControlStore()
    this.sceneStore = SceneStore.get()
    this.playlistStore = PlaylistStore.get()

    this.ws = WebsocketInterface.get()

    this.registerHandlers()
  }

  public registerHandlers() {
    // When the front end requests the initial state, we will send it
    ws.registerActionHandler('requestInitialState', this.&sendInitialState)
  }

  // Load the initial state from files and create the objects in the Stores
//  public loadInitialState() {
//
//  }

  // Sends the state of the relevant objects to the front end for initial hydration
  // This will really need to read the existing Java objects from the Stores
  public sendInitialState(conn, inData) {
    // Send objects in this order:
    // clips (clips I'll leave hardcoded for now)
    // scenes
    // playlists

    println "Sending initial state".cyan()

    Map data = [
        sceneData: SceneStore.get().asJsonObj(),
        playlistData: PlaylistStore.get().asJsonObj(),
    ]

    ws.sendMessage(conn, 'sendInitialState', data);
  }
}
