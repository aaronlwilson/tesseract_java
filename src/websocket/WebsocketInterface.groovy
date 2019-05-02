package websocket

import app.TesseractMain
import groovy.json.JsonSlurper

import java.nio.ByteBuffer

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

/**
 * Websocket Interface for providing sending and receiving data from Tesseract front-end
 */
class WebsocketInterface extends WebSocketServer {

  private static TesseractMain _main;

  WebsocketInterface(int port) {
    super(new InetSocketAddress(port))
    this._main = TesseractMain.getMain()
  }

  WebsocketInterface(InetSocketAddress address) {
    super(address)
  }

  @Override
  void onOpen(WebSocket conn, ClientHandshake handshake) {
    conn.send("You have connected to the Tesseract Backend") //This method sends a message to the new client

    broadcast("new connection: " + handshake.getResourceDescriptor())

    println("New websocketed opened from: ${conn.getRemoteSocketAddress().getAddress().getHostAddress()}")
  }

  @Override
  void onClose(WebSocket conn, int code, String reason, boolean remote) {
    println("${conn} has disconnected")
  }

  @Override
  void onMessage(WebSocket conn, String message) {
//    broadcast(message)
    println("Got message external: ${conn}: ${message}")

    // Benefit of groovy: Json parsing is built in and easy, unlike Java
    def jsonSlurper = new JsonSlurper()
    def jsonObj = jsonSlurper.parseText(message)

    println jsonObj

    // example of what our API can look like for sending commands to the backend
    if (jsonObj['action'] == 'rotate_left') {
//      this.rotateLeft()
    } else {
      throw new Exception("Error: Unhandled action specified in websocket message: ${jsonObj['action']}")
    }
  }

  @Override
  void onMessage(WebSocket conn, ByteBuffer message) {
//    broadcast(message.array())
    println("Got message external (bytebuffer): ${conn}: ${message}")
  }

  static WebsocketInterface createInterface() {
    int port = 8883

    WebsocketInterface s = new WebsocketInterface(port)
    s.start()
    println("WebsocketInterface started on port: ${s.getPort()}")

    // groovy functions (or blocks) always return the last expression, so you will often see omitted return statements
    s
  }

  @Override
  void onError(WebSocket conn, Exception ex) {
    ex.printStackTrace()
    if (conn != null) {
      // some errors like port binding failed may not be assignable to a specific websocket
    }
  }

  @Override
  void onStart() {
    println("Websocket server started")
    setConnectionLostTimeout(0)
    setConnectionLostTimeout(100)
  }
}
