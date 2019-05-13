package app;

import model.Channel;
import processing.core.*;

import output.*;
import environment.*;
import model.*;
import state.StateManager;
import stores.PlaylistStore;
import stores.SceneStore;
import util.Util;
import show.*;
import websocket.WebsocketInterface;


import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

import java.io.IOException;


public class TesseractMain extends PApplet {

  //single global instance
  private static TesseractMain _main;


  //CLIP CLASS ENUM
  public static final int NODESCAN = 0;
  public static final int SOLID = 1;
  public static final int COLORWASH = 2;
  public static final int STRIPE = 3;


  public static String[] clipNames = {
    "nodescan",
    "solid",
    "colorwash"
  };



  public Scene[] tempScenes; //just for demo until Playlists are working
  public int sceneIndex = 0;



  private UDPModel udpModel;
  private OnScreen onScreen;


  public Stage stage;

  public Channel channel1;
  public Channel channel2;

  public WebsocketInterface ws;
  public PlaylistManager playlistManager;
  public StateManager stateManager;
  public SceneStore sceneStore;
  public PlaylistStore playlistStore;


  //Click the arrow on the line below to run the program in .idea
  public static void main(String[] args) {
    PApplet.main("app.TesseractMain", args);
  }

  public static TesseractMain getMain() {
    return _main;
  }

  @Override
  public void settings() {

    size(1100, 800, P3D);

    //looks nice, but runs slower, one reason to put UI in browser
    //pixelDensity(displayDensity()); //for mac retna displays
    //pixelDensity(2);
  }

  @Override
  public void setup() {
    Util.enableColorization();

    _main = this;

    // Persistence / state update stuff
    ws = WebsocketInterface.get();
    stateManager = new StateManager();
    playlistManager = new PlaylistManager();
    sceneStore = SceneStore.get();
    // Doesn't make sense to fully implement this until we know more about how playlists are gonna work
    playlistStore = PlaylistStore.get();

    clear();

    udpModel = new UDPModel(this);

    onScreen = new OnScreen(this);

    stage = new Stage(this);

    //pass in an XML stage definition, eventually we might load a saved project which is a playlist and environment together
    stage.buildStage();

    //create channels
    channel1 = new Channel(1);
    channel2 = new Channel(2);

    //make a dummy clip, one way to use direct control and load a clip directly into a channel, no scene neccessary
    //channel1.constructNewClip(SOLID);

    // Make some dummy scenes in the store

    // These are hydrated from the json now.  creating them here will update the existing data in the store, but this can be commented out and it will load entirely from disk
    // If we specify the id in the constructor and it matches an existing Scene, it will update the data.  omitting the ID from the constructor will use the max id + 1 for the new scene
    Scene sYellow =  new Scene(1,"Yellow", TesseractMain.SOLID, new float[] {0, 0, 0, 1, 1, 0, 0});
    this.sceneStore.addOrUpdate(sYellow);

    Scene sPurple =  new Scene(2, "Purple", TesseractMain.SOLID, new float[] {0, 0, 0, 1, 0, 1, 0});
    this.sceneStore.addOrUpdate(sPurple);

    Scene sRed =  new Scene(3,"Red", TesseractMain.SOLID, new float[] {0, 0, 0, 1, 0, 0, 0});
    this.sceneStore.addOrUpdate(sRed);

    // Set temp scenes from store data
    tempScenes = new Scene[3];
    tempScenes[0] = this.sceneStore.find("displayName", "Yellow");
    tempScenes[1] = this.sceneStore.find("displayName", "Purple");
    tempScenes[2] = this.sceneStore.find("displayName", "Red");

    // Save the created scenes to disk, this will eventually happen whenever state changes in the store(s)
    this.sceneStore.saveDataToDisk();

    //load first scene into a channel
    //nextScene();



    TimerTask task = new TimerTask() {
      public void run() {
        nextScene();

        //System.out.println("Task performed on: " + new Date() + "n" + "Thread's name: " + Thread.currentThread().getName());
      }
    };
    Timer timer = new Timer("NextScene");
    //long delay = 4000L;
    //timer.schedule(task, delay); //run once

    // I needed this to be a bit longer to test my stuff, feel free to change it back
    timer.scheduleAtFixedRate(task, 0, 20000L);


    //load a default playlist file. We need to make shit happen on boot in case the power goes out.

    // The shutdown hook will let us clean up when the application is killed
    createShutdownHook();
  }

  public void nextScene(){
    //load next scene into a channel
    Scene nextScene = tempScenes[sceneIndex];

    channel1.setScene(nextScene);
    sceneIndex++;

    if(sceneIndex > tempScenes.length - 1)
      sceneIndex = 0;

    // Send a 'stateUpdated' event to the UI.  we will need to send one of these whenever state changes and we need to update the frontend
    // for now this is a one-off that sets the activeScene on the 'controls' panel of the UI
    stateManager.sendStateUpdate("activeScene", nextScene.id);

    System.out.println("nextScene: " + nextScene.getDisplayName());
  }

  @Override
  public void draw() {
    clear();


    //call run() on the current clips inside channels
    channel1.run();
    channel2.run();


    //get the full list of hardware nodes
    int l= stage.nodes.length;

    Node[] nextNodes = stage.nodes;

    for(int i=0; i<l; i++) {
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
  }

  @Override
  public void mousePressed() {
    onScreen.mousePressed();
  }

  @Override
  public void mouseReleased() {
    onScreen.mouseReleased();
  }

  //determine final color for each node each frame
  public int[] renderNode(Node node){
    //return
    int[] rgb1 = channel1.drawNode(node);


    //apply channel brightness
    //rgb1[0] = int(rgb1[0] * (channelCanvas1.channelBrightness/100) * (channelCanvas1.rBrightness/100));
    //rgb1[1] = int(rgb1[1] * (channelCanvas1.channelBrightness/100) * (channelCanvas1.gBrightness/100));
    //rgb1[2] = int(rgb1[2] * (channelCanvas1.channelBrightness/100) * (channelCanvas1.bBrightness/100));

    //mix the 2 channels together

    return rgb1;

  }//end render node


  private void createShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        WebsocketInterface.get().shutdownServer();
      }
    });
  }
}
