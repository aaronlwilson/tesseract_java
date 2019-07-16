package app;


import processing.core.*;
import processing.video.Movie;

import output.*;
import environment.*;
import model.*;
import state.StateManager;
import stores.PlaylistStore;
import stores.SceneStore;
import util.AppSettings;
import util.Util;
import show.*;
import websocket.WebsocketInterface;


import java.io.File;
import java.util.*;


public class TesseractMain extends PApplet {

  //single global instance
  private static TesseractMain _main;


  //CLIP CLASS ENUM
  public static final int NODESCAN = 0;
  public static final int SOLID = 1;
  public static final int COLORWASH = 2;
  public static final int VIDEO = 3;
  public static final int PARTICLE = 4;
  public static final int PERLINNOISE = 5;

  private OnScreen onScreen;
  public UDPModel udpModel;
  public Stage stage;
  public Channel channel1;

  //Click the arrow on the line below to run the program in .idea
  public static void main(String[] args) {
    PApplet.main("app.TesseractMain", args);
  }

  public static TesseractMain getMain() {
    return _main;
  }

  @Override
  public void settings() {

    size(1400, 800, P3D);

    //looks nice, but runs slower, one reason to put UI in browser
    //pixelDensity(displayDensity()); //for mac retna displays
    //pixelDensity(2);
  }

  @Override
  public void setup() {
    frameRate( 30 );

    Util.enableColorization();

    _main = this;

    // Configure Data and Stores

    // Make some dummy data in the stores
    Util.createBuiltInScenes();
    Util.createBuiltInPlaylists();

    // Saves the default data
    SceneStore.get().saveDataToDisk();
    PlaylistStore.get().saveDataToDisk();

    // Initialize websocket connection
    WebsocketInterface.get();

    clear();

    // Start listening for UDP messages.  Handles sending/receiving all UDP data
    udpModel = new UDPModel(this);

    // Draw the on-screen visualization
    onScreen = new OnScreen(this);

    // The stage is the LED mapping
    stage = new Stage(this);

    // Get the configured stage value.  Controlled via environment variable
    String stageType = AppSettings.get("STAGE_TYPE");

    // eventually we might load a saved project which is a playlist and environment together
    stage.buildStage(stageType);

    // create channel
    channel1 = new Channel(1);

    // Tell the PlaylistManager which channel to play playlists in
    PlaylistManager.get().setChannel(this.channel1);

    // Play the playlist with id = 1, play the first item in the playlist, and start in the 'looping' state
    PlaylistManager.get().play(2, null, Playlist.PlayState.LOOP_SCENE);

    // The shutdown hook will let us clean up when the application is killed
    createShutdownHook();

  }

  @Override
  public void draw() {
    clear();

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

    onScreen.draw();

    //push packets out to LEDS
    //udpModel.sendTest();


    //PUT BACK
    //udpModel.send();
  }


  //determine final color for each node each frame
  public int[] renderNode(Node node) {
    //return
    int[] rgb1 = channel1.drawNode(node);


    //apply channel brightness
    rgb1[0] = (int)rgb1[0];
    rgb1[1] = (int)rgb1[1];
    rgb1[2] = (int)rgb1[2];

    //mix the 2 channels together

    return rgb1;

  }//end render node


  /*
  private void createBuiltInScenes() {
    // These are hydrated from the json now.  creating them here will update the existing data in the store, but this can be commented out and it will load entirely from disk
    // If we specify the id in the constructor and it matches an existing Scene, it will update the data.  omitting the ID from the constructor will use the max id + 1 for the new scene
    Scene sScan = new Scene(6, "Node Scanner", TesseractMain.NODESCAN, new float[]{0, 0, 0, 0, 0, 0, 0});
    SceneStore.get().addOrUpdate(sScan);

    Scene sVid = new Scene(5, "First Video", TesseractMain.VIDEO, new float[]{0, 0, 0, 0, 0, 0, 0}, "videos/Acid Shapes __ Free Vj Loop.mp4");
    SceneStore.get().addOrUpdate(sVid);

    Scene sWash = new Scene(4, "Color Wash", TesseractMain.COLORWASH, new float[]{0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f});
    SceneStore.get().addOrUpdate(sWash);

    Scene sYellow = new Scene(1, "Yellow", TesseractMain.SOLID, new float[]{0, 0, 0, 1, 1, 0, 0});
    SceneStore.get().addOrUpdate(sYellow);

    Scene sPurple = new Scene(2, "Purple", TesseractMain.SOLID, new float[]{0, 0, 0, 1, 0, 1, 0});
    SceneStore.get().addOrUpdate(sPurple);

    Scene sRed = new Scene(3, "Red", TesseractMain.SOLID, new float[]{0, 0, 0, 1, 0, 0, 0});
    SceneStore.get().addOrUpdate(sRed);
  }
  */

  /*
  private void createBuiltInPlaylists() {
    List<PlaylistItem> playlist1Items = new LinkedList<>(Arrays.asList(

        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 5), 6),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 6), 6),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 4), 4),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 1), 3),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 4), 4),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 2), 4),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 3), 3),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 4), 5),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 3), 5),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 2), 7)
    ));

    Playlist playlist1 = new Playlist(1, "Cubotron", 60, playlist1Items);
    PlaylistStore.get().addOrUpdate(playlist1);

    List<PlaylistItem> playlist2Items = new LinkedList<>(Arrays.asList(
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 1), 3),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 2), 3),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 1), 3),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 3), 3),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 1), 3),
        new PlaylistItem(UUID.randomUUID().toString(), SceneStore.get().find("id", 2), 3)
    ));

    Playlist playlist2 = new Playlist(2, "Color Cube", 60, playlist2Items);
    PlaylistStore.get().addOrUpdate(playlist2);
  }
  */

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
    onScreen.mousePressed();
  }

  @Override
  public void mouseReleased() {
    onScreen.mouseReleased();
  }

  //Custom event handler on pApplet for video library
  public void movieEvent(Movie movie) { movie.read(); }
}
