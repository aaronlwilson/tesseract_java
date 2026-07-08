package websocket

import app.TesseractApp
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import java.nio.ByteBuffer

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

/**
 * Websocket Interface for providing sending and receiving data from Tesseract front-end
 */
class WebsocketInterface extends WebSocketServer {

  private static WebsocketInterface instance

  private static TesseractApp mainApp

  // map has a String key and the value is a list of closures (functions)
  Map<String, List<Closure>> actionHandlers = [:]

  WebsocketInterface(String addr, int port) {
    super(new InetSocketAddress(addr, port))
    this.mainApp = TesseractApp.get()
  }

  WebsocketInterface(InetSocketAddress address) {
    super(address)
  }

  public static WebsocketInterface get() {
    if (instance == null) {
      instance = createInterface()
    }

    instance
  }

  static WebsocketInterface createInterface() {
    String addr = "0.0.0.0"
    int port = 8883

    WebsocketInterface s = new WebsocketInterface(addr, port)
    s.start()
    println("WebsocketInterface started on port: ${s.getPort()}".cyan())

    // groovy functions (or blocks) always return the last expression, so you will often see omitted return statements
    s
  }

  // Sends a websocket message in the format the front end expects:
  // [ action: 'action-name', data: [arbitrary: 'data'] ]
  void sendMessage(WebSocket conn, String action, data) {
//    println "Sending websocket message: ${action}".cyan()
//    println new JsonBuilder(data).toPrettyString().cyan()

    Map message = [
        action: action,
        data  : data
    ]

    String jsonStr = new JsonBuilder(message).toPrettyString()
    conn.send(jsonStr)
  }

  // Sends a websocket message in the format the front end expects:
  // [ action: 'action-name', data: [arbitrary: 'data'] ]
  // Sends the message to all clients
  void broadcastMessage(String action, data) {
//    println "Broadcasting websocket message: ${action}".cyan()
//    println new JsonBuilder(data).toPrettyString().cyan()

    Map message = [
        action: action,
        data  : data
    ]

    String jsonStr = new JsonBuilder(message).toPrettyString()
    this.broadcast(jsonStr)
  }

  @Override
  void onOpen(WebSocket conn, ClientHandshake handshake) {
    this.sendMessage(conn, 'logMessage', "You have connected to the Tesseract Backend")
    println("New websocketed opened from: ${conn.getRemoteSocketAddress().getAddress().getHostAddress()}")
  }

  @Override
  void onClose(WebSocket conn, int code, String reason, boolean remote) {
    println("${conn} has disconnected")
  }

  @Override
  void onMessage(WebSocket conn, String message) {
//    println("Got message external: ${conn}: ${message}")

    // Benefit of groovy: Json parsing is built in and easy, unlike Java
    def jsonSlurper = new JsonSlurper()

    // A malformed or unhandled message must never take down the WS thread — log and ignore.
    def jsonObj
    try {
      jsonObj = jsonSlurper.parseText(message)
    } catch (Exception e) {
      System.err.println("[WS] Could not parse JSON message, ignoring: ${message}")
      return
    }

    if (!jsonObj['action'] || !(jsonObj['action'] instanceof String)) {
      System.err.println("[WS] Ignoring message with missing/invalid 'action': ${jsonObj}")
      return
    }

    List<Closure> handlers = this.actionHandlers[jsonObj['action'] as String]

    if (!handlers) {
      System.err.println("[WS] No handler registered for action '${jsonObj['action']}', ignoring")
      return
    }

    // Call all handlers with the payload; never let a handler exception kill the socket thread
    handlers.each { Closure handler ->
      try {
        handler(conn, jsonObj['data'])
      } catch (Exception e) {
        System.err.println("[WS] Handler for action '${jsonObj['action']}' threw: ${e.message}")
        e.printStackTrace()
      }
    }
  }

  @Override
  // I think this is unused in our case
  void onMessage(WebSocket conn, ByteBuffer message) {
    println("onMessage (bytebuffer): ${conn}: ${message}")
  }

  @Override
  void onError(WebSocket conn, Exception ex) {
    // Log, never rethrow — a per-connection error (or a bind error with conn == null)
    // must not crash the server thread.
    System.err.println("[WS] WebSocket error" + (conn != null ? " on ${conn.getRemoteSocketAddress()}" : "") + ": ${ex.message}")
    ex.printStackTrace()
  }

  @Override
  void onStart() {
    println "Websocket server started".yellow()
    setConnectionLostTimeout(0)
    setConnectionLostTimeout(100)
  }

  // This will register a handler for a message coming from the front end over the websocket
  // For example, the UI will send a message with action 'requestInitialState', and the handler will send the initial state back in another message
  void registerActionHandler(String actionType, Closure handler) {
//    println "Registering action handler: ${actionType}".cyan()
    if (this.actionHandlers[actionType]) {
      this.actionHandlers[actionType].push(handler)
    } else {
      this.actionHandlers[actionType] = [handler]
    }
  }

  // Shutdown server properly so we don't leave the port open when we hard kill the Processing app
  void shutdownServer() {
    try {
      println("Trying to kill the websocket server".yellow());
      WebsocketInterface.get().stop();
      println("The server is shut down".green());
    } catch (IOException | InterruptedException e) {
      println("Error!  Could not shut down the websocket server".red());
      e.printStackTrace();
    }
  }
}
