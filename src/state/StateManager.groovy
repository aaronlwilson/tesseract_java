package state

import mock.MockData
import stores.PlaylistStore
import stores.SceneStore
import websocket.WebsocketInterface

// State manager is responsible for managing application state and synchronizing state
// between client(s) and server
class StateManager {

  WebsocketInterface ws = WebsocketInterface.get()

  public StateManager() {
    this.registerHandlers()
  }

  public registerHandlers() {
    ws.registerActionHandler('requestInitialState', this.&sendInitialState)
  }

  // Sends the state of the relevant objects to the front end for initial hydration
  public sendInitialState(conn, inData) {
    // Send objects in this order:
    // clips (clips I'll leave hardcoded for now)
    // scenes
    // playlists

    println "Sending initial state".cyan()

    def data = [
        sceneData: SceneStore.get().asJsonObj(),
        playlistData: PlaylistStore.get().asJsonObj(),
    ]

    ws.sendMessage(conn, 'sendInitialState', data);
  }

}
