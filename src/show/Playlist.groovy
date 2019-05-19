package show

import model.Channel
import state.StateManager;

public class Playlist {
  int id;
  String displayName;
  Channel channel;
  Integer defaultDuration;
  List<PlaylistItem> items;

  // PLAYING: the playlist is playing a scene and advancing to the next scene after the specified duration
  // PAUSED: the playlist is playing a scene but NOT advancing to the next scene (play the same scene forever)
  // STOPPED: nothing is playing.  LEDs are dark
  public enum PlayState {
    PLAYING, PAUSED, STOPPED
  }

  // Track whether we're playing, paused, or stopped
  PlayState currentPlayState;

  // The index of the current playlist item
  private int currentIdx;

  private Timer currentTimer;
  private long currentTimerStartTime;

  public Playlist(int id, String displayName, Integer defaultDuration = 60, List<PlaylistItem> items = []) {
    this.id = id;
    this.displayName = displayName;
    this.defaultDuration = defaultDuration;
    this.items = items;
    this.currentIdx = 0;

    // start initially in the stopped state, when the initial data loads it will update
    this.currentPlayState = PlayState.STOPPED;
  }

  public void setChannel(Channel channel) {
    this.channel = channel;
  }

  public PlaylistItem getCurrentItem() {
    this.items[this.currentIdx];
  }

  private void scheduleNextScene(long delay) {
    TimerTask task = new TimerTask() {
      public void run() {
        // always repeat for now
        currentIdx++;
        if (currentIdx >= items.size()) {
          currentIdx = 0;
        }

        _playPlaylist();

        //System.out.println("Task performed on: " + new Date() + "n" + "Thread's name: " + Thread.currentThread().getName());
      }
    };

    Timer timer = new Timer("Playlist.play()");

    // Keep track of when we started the timer
    this.currentTimerStartTime = System.currentTimeMillis()
    timer.schedule(task, delay);

    // Keep track of the timer itself
    this.currentTimer = timer;
  }

  // figure out how long we have until our timer expires
  public long getCurrentSceneDurationRemaining() {
    if (!this.currentTimer) {
      return -1; // infinity
    }

    // Time thats elapsed since we started the timer
    long elapsedTime = System.currentTimeMillis() - this.currentTimerStartTime
    long timeLeft = (this.getCurrentItem().getDuration() * 1000) - elapsedTime
    timeLeft
  }

  public void unsetChannel() {
    this.channel = null;
  }

  public boolean isPlaying() {
    this.currentPlayState == PlayState.PLAYING;
  }

  public boolean isPaused() {
    this.currentPlayState == PlayState.PAUSED;
  }

  public boolean isStopped() {
    this.currentPlayState == PlayState.STOPPED;
  }

  private void cancelCurrentTimer() {
    if (this.currentTimer) {
      this.currentTimer.cancel()
    }

    this.currentTimer = null
  }

  // Pause playlist.  continues playing the scene, but won't advance to the next item
  // will play the scene if we are stopped
  public void pause() {
    println "[Playlist] pause: State is ${this.currentPlayState}"

    this.currentPlayState = PlayState.PAUSED
    this.cancelCurrentTimer()

    this._playPlaylist()
  }

  // Stop playlist.  continues playing the scene, but won't advance to the next item
  public void stop() {
    this.currentPlayState = PlayState.STOPPED

    // need to figure out how to actually stop the clip from playing, if this doesn't do it
    // this throws a NPE sometimes which is a problem
    this.channel.unsetScene()
    this.cancelCurrentTimer()

    StateManager.get().sendActiveState()
  }

  // todo: add ability to play a specific playlist
  public void play() {
    this.currentPlayState = PlayState.PLAYING
    this._playPlaylist()
  }

  // Plays a scene
  private void playItem(PlaylistItem item) {
    this.channel.setScene(item.scene, false, 10);

    System.out.println("Playing scene '${item.scene.getDisplayName()}' on playlist '${this.displayName}'");
  }

  // This method handles playing an item on the playlist w/o changing the 'playState', which is whether we're playing/paused/stopped
  private void _playPlaylist() {
    if (this.isStopped()) {
      throw new RuntimeException("[Playlist] Called _playPlaylist while state is ${this.currentPlayState}")
      return
    }

    println "[Playlist] _playPlaylist: State is ${this.currentPlayState}"

    PlaylistItem currentItem = this.items[this.currentIdx];
    this.playItem(currentItem);

    // Schedule the next item
    if (this.isPlaying()) {
      PlaylistItem nextItem = this.items[this.currentIdx];
      long delay = nextItem.duration * 1000 as long;
      this.scheduleNextScene(delay);
    }

    // Send a 'stateUpdated' event to the UI.  we will need to send one of these whenever state changes and we need to update the frontend
    StateManager.get().sendActiveState()
  }
}
