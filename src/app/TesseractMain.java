package app;

import model.Channel;
import processing.core.*;

import output.*;
import environment.*;
import model.*;
import show.PlaylistManager;
import state.StateManager;
import util.Util;
import websocket.WebsocketInterface;

import java.io.IOException;


public class TesseractMain extends PApplet {

  //single global instance
  private static TesseractMain _main;


  //CLIP CLASS ENUM
  public static final int NODESCAN = 0;
  public static final int SOLID = 1;
  public static final int COLORWASH = 2;


  public static String[] clipNames = {
    "nodescan",
    "solid",
    "colorwash"
  };





  private UDPModel udpModel;
  private OnScreen onScreen;


  public Stage stage;

  public Channel channel1;
  public Channel channel2;

  public WebsocketInterface ws;
  public PlaylistManager playlistManager;
  public StateManager stateManager;

  public static void main(String[] args) {
    PApplet.main("app.TesseractMain", args);
  }

  public static TesseractMain getMain() {
    return _main;
  }

  @Override
  public void settings() {

    size(1100, 800, P3D);

    //pixelDensity(displayDensity()); //for mac retna displays
    //pixelDensity(2);
  }

  @Override
  public void setup() {
    Util.enableColorization();

    _main = this;

    ws = WebsocketInterface.get();
    stateManager = new StateManager();
    playlistManager = new PlaylistManager();

    clear();

    udpModel = new UDPModel(this);

    onScreen = new OnScreen(this);

    stage = new Stage(this);

    //pass in an XML stage definition, eventually we might load a saved project which is a playlist and environment together
    stage.buildStage();

    //create channels

    //load a default playlist file. We need to make shit happen on boot in case the power goes out.

    // The shutdown hook will let us clean up when the application is killed
    createShutdownHook();
  }

  @Override
  public void draw() {
    clear();


    //call run() on the current scene loaded into channel 1

    //call run() on the current scene loaded into channel 1

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

  private void createShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        WebsocketInterface.get().shutdownServer();
      }
    });
  }

}
