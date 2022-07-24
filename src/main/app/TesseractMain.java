package app;

//temp
import clip.Particle;


import processing.core.PApplet;
import processing.video.Movie;
import processing.serial.*;

import environment.Node;
import environment.Stage;
import model.Channel;
import output.UDPModel;
import show.Playlist;
import show.PlaylistManager;
import stores.ConfigStore;
import stores.PlaylistStore;
import stores.SceneStore;
import util.Util;
import websocket.WebsocketInterface;

public class TesseractMain extends PApplet {

  //single global instance
  private static TesseractMain _main;

  public Particle particleX;
  public Particle particleY;
  public Particle particleZ;

  //CLIP CLASS ENUM
  public static final int NODESCAN = 0;
  public static final int SOLID = 1;
  public static final int COLORWASH = 2;
  public static final int VIDEO = 3;
  public static final int PARTICLE = 4;
  public static final int PERLINNOISE = 5;
  public static final int LINESCLIP = 6;
  public static final int TILESTEST = 7;

  private OnScreen onScreen;//only the main class gets to draw
  public UDPModel udpModel;
  public Stage stage;
  public Channel channel1;
  public Boolean setupComplete = false;

  //for Rotary Encoder readings
  public Serial arduinoPort;    // The serial port
  public int lf = 10;      // ASCII linefeed
  public String inString;  // Input chars from serial port
  public double rotaryEncoderAngle;


  //Click the arrow on the line below to run the program in .idea
  public static void main(String[] args) {
    PApplet.main("app.TesseractMain", args);
  }

  public static TesseractMain getMain() {
    return _main;
  }

  @Override
  public void settings() {

    size(1400, 800, P3D); // for when all panels are in a row
    //size(450, 450, P3D); // for tesseract cube

    // Required for the application to launch on Ubuntu Linux (Intel NUC w/ Intel integrated graphics)
    // It has something to do with the specific OS/packages/video drivers/moon cycles/etc
    //https://github.com/processing/processing/issues/5476

    System.setProperty("jogl.disable.openglcore", "false");

    //looks nice, but runs slower, one reason to put UI in browser
    //pixelDensity(displayDensity()); //for mac retna displays
    //pixelDensity(2);
  }

  @Override
  public void setup() {
    // Clear screen
    clear();

    frameRate(30);

    Util.enableColorization();

    _main = this;


    // Start listening for UDP messages.  Handles sending/receiving all UDP data
    udpModel = new UDPModel(this);

    // Draw the on-screen visualization
    onScreen = new OnScreen(this);

    // The stage is the LED mapping
    stage = new Stage();

    // create channel
    channel1 = new Channel(1);

    //finish set up on a separate thread to avoid this common error seen in the console:
    /*
    java.lang.RuntimeException: Waited 5000ms for: <2b4dc6d4, 258a2dcd>[count 2, qsz 0, owner <main-FPSAWTAnimator#00-Timer0>] - <main-FPSAWTAnimator#00-Timer0-FPSAWTAnimator#00-Timer1>
    at processing.opengl.PSurfaceJOGL$2.run(PSurfaceJOGL.java:410)
    */

    thread("completeConfiguration");
  }

  private void setupSerial() {
    // Open the port you are using at the rate you want:
    if (Serial.list().length > 4) {
      String portName = Serial.list()[5];
      arduinoPort = new Serial(this, portName, 115200);
      arduinoPort.bufferUntil(lf);
    }else{

    }
  }

  public void completeConfiguration() {
    // Configure Data and Stores

    // Make some dummy data in the stores
    Util.createBuiltInScenes();
    Util.createBuiltInPlaylists();

    // Saves the default data
    SceneStore.get().saveDataToDisk();
    PlaylistStore.get().saveDataToDisk();


    // Load configuration from file.  This must happen AFTER we've created our initial playlists, or it will fail on a fresh install
    ConfigStore.get();

    // Initialize websocket connection
    WebsocketInterface.get();

    // Get the configured stage value.  Controlled via configuration option
    String stageType = ConfigStore.get().getString("stageType");

    // eventually we might load a saved project which is a playlist and environment together
    stage.buildStage(stageType);


    // Tell the PlaylistManager which channel to play playlists in
    PlaylistManager.get().setChannel(this.channel1);

    // Get initial playlist & playState from config
    Playlist initialPlaylist = PlaylistStore.get().find("displayName", ConfigStore.get().getString("initialPlaylist"));
    Playlist.PlayState initialPlayState = Util.getPlayState(ConfigStore.get().getString("initialPlayState"));

    // Play the playlist w/ the playState defined in our configuration
    PlaylistManager.get().play(initialPlaylist.getId(), null, initialPlayState);

    // The shutdown hook will let us clean up when the application is killed.  It is very important to clean up the websocket server so we don't leave the port in use
    createShutdownHook();

    //open up a serial "portal" to an Arduino dimension
    setupSerial();

    setupComplete = true;
  }

  @Override
  public void draw() {
    //wait for the "completeConfiguration" thread to complete
    if (!setupComplete) return;

    //call run() on the current clips inside channels
    channel1.run();
    //channel2.run();

    //get the full list of hardware nodes
    int l = stage.nodes.length;

    Node[] nextNodes = stage.nodes;
    stage.prevNodes = stage.nodes;

    for (int i = 0; i < l; i++) {
      Node n = nextNodes[i];
      int[] rgb = renderNode(n); //does the blending between the channels, apply master FX

      //now store that color on the node so we can send it as UDP data to the lights
      n.r = rgb[0];
      n.g = rgb[1];
      n.b = rgb[2];

      nextNodes[i] = n;
    }

    stage.nodes = nextNodes;

    //TODO: keyboard toggle drawing when not needed at shows
    onScreen.draw();

    //push dummy packets out to LEDS
    //udpModel.sendRabbitTest();

    //PUT BACK
    udpModel.send();
  }


  //determine final color for each node each frame
  public int[] renderNode(Node node) {
    //return
    int[] rgb1 = channel1.drawNode(node);

    //TODO apply global brightness
    //apply channel brightness
    rgb1[0] = Math.round(rgb1[0]/1);
    rgb1[1] = Math.round(rgb1[1]/1);
    rgb1[2] = Math.round(rgb1[2]/1);

    //TODO mix the 2 channels together

    return rgb1;

  }//end render node


  private void createShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        WebsocketInterface.get().shutdownServer();
      }
    });
  }

  //EVENT HANDLERS
  //calls happen on pApplet, then can be routed to the proper place in our code
  @Override
  public void mousePressed() {
    //udpModel.sendFlameTest(4, 1);
    onScreen.mousePressed();
  }

  @Override
  public void mouseReleased() {
    //udpModel.sendFlameTest(4, 0);
    onScreen.mouseReleased();
  }


  //event handler for processing.serial
  public void serialEvent(Serial p) {
    //inString = p.readString();
    inString = p.readStringUntil('\n');

    //System.out.printf("SERIAL IN: %s \n", inString);

    if (inString != null) {
      // trim off any whitespace:
      inString = trim(inString);
      // convert to an int and map to the screen height:
      int pulses = Integer.parseInt(inString);	 //0 to 2048

      //convert to radians
      rotaryEncoderAngle = (pulses*(this.PI/1024));
    }
  }

  //Custom event handler on pApplet for video library
  public void movieEvent(Movie movie) { movie.read(); }
}
