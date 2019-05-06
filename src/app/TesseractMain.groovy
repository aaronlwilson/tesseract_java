package app;

import processing.core.*;

import output.*;
import environment.*
import show.PlaylistManager
import state.StateManager
import util.Util;
import websocket.WebsocketInterface;

import java.io.IOException;


public class TesseractMain extends PApplet {

  //single global instance
  private static TesseractMain _main;

  //public static PApplet processing;


  private UDPModel udpModel;
  private OnScreen onScreen;


  public Stage stage;

  public WebsocketInterface ws
  public PlaylistManager playlistManager
  public StateManager stateManager


  public static void main(String[] args) {
    PApplet.main("app.TesseractMain", args);
  }

  public static TesseractMain getMain() {
    return _main;
  }

  @Override
  public void settings() {

    size(1100, 800, P3D);

    pixelDensity(displayDensity());
    //pixelDensity(2);
  }

  @Override
  public void setup() {
    Util.enableColorization()

    _main = this;
    //processing = this;

    ws = WebsocketInterface.get()
    stateManager = new StateManager()
    playlistManager = new PlaylistManager()

    clear();

    udpModel = new UDPModel(this);

    onScreen = new OnScreen(this);

    stage = new Stage(this);

    //pass in an XML stage definition, eventually we might load a saved project which is a playlist and environment together
    stage.buildStage();

    //create channels

    //load a default playlist file. We need to make shit happen on boot in case the power goes out.

    // The shutdown hook will let us clean up when the application is killed
    createShutdownHook()
  }

  @Override
  public void draw() {
    clear();

    onScreen.draw();


    //call run() on the current scene loaded into channel 1

    //call run() on the current scene loaded into channel 1

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
        WebsocketInterface.get().shutdownServer()
      }
    });
  }
}
