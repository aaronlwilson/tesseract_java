package show

import clip.VideoClip
import model.Channel
import state.StateManager;

public class Playlist {
  int id;
  String displayName;
  Channel channel;
  Integer defaultDuration;
  List<PlaylistItem> items;

  // PLAYING: the playlist is playing a scene and advancing to the next scene after the specified duration
  // LOOP_SCENE: the playlist is playing a scene but NOT advancing to the next scene (play the same scene forever)
  // STOPPED: nothing is playing.  LEDs are dark
  public enum PlayState {
    PLAYING, LOOP_SCENE, STOPPED
  }

  // Track whether we're playing, looping, or stopped
  PlayState currentPlayState;

  // The current playlist item
  PlaylistItem currentItem;

  private Timer currentTimer;
  private long currentTimerStartTime;

  public Playlist(int id, String displayName, Integer defaultDuration = 60, List<PlaylistItem> items = []) {
    this.id = id;
    this.displayName = displayName;
    this.defaultDuration = defaultDuration;
    this.items = items;

    // start initially in the stopped state, when the initial data loads it will update
    this.currentPlayState = PlayState.STOPPED;
  }

  public void setChannel(Channel channel) {
    this.channel = channel;
  }

  private PlaylistItem getNextItem() {
    Integer nextIdx = this.getNextIdx()
    if (nextIdx == -1) {
      return null
    }

    this.items.get(this.getNextIdx())
  }

  // Get the index of the next item.  for now we will play everything sequentially (but we could add 'shuffle' later)
  private Integer getNextIdx() {
    if (this.items.size() == 0) {
      return -1; // this means there are no items in the playlist and we don't have a next item
    }

    Integer currentItemIndex = this.items.findIndexOf { it == this.getCurrentItem() }
    currentItemIndex = currentItemIndex ?: 0

    // always repeat for now
    currentItemIndex++;
    if (currentItemIndex >= items.size()) {
      currentItemIndex = 0;
    }

    currentItemIndex
  }

  // This is used when we delete the currently running Scene and we need to skip to the next item
  // Could also be used to add a 'next' button in the UI
  public void playNext() {
    this.cancelCurrentTimer()

    // Check to see if there are any more items in the playlist.  If not, stop
    PlaylistItem nextItem = this.getNextItem()
    if (nextItem == null) {
      // null current playlist item.  probably not the ideal way to do this, but don't want to send a 'non existent' playlist item
      // with our stateUpdate event
      this.currentItem = null
      this.stop()
      return
    }

    this._playPlaylist(nextItem, this.getCurrentPlayState());
  }

  private void scheduleNextScene(long delay) {
    TimerTask task = new TimerTask() {
      public void run() {
        _playPlaylist(getNextItem(), getCurrentPlayState());
      }
    };

    Timer timer = new Timer("Playlist.scheduleNextScene()");

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

  private void cancelCurrentTimer() {
    if (this.currentTimer) {
      this.currentTimer.cancel()
    }

    this.currentTimer = null
  }

  // Unloads the scene from the channel, sets the channel to null, and cancels the current timer
  public void unload() {
    // Is there a better way to stop the channel from playing (than unsetScene)?
    // this throws a NPE sometimes which is a problem
    if (this.channel) this.channel.unsetScene()
    this.channel = null
    this.cancelCurrentTimer()
  }

  // Stop.  Unload.  Maybe do some stuff.
  public void stop(boolean shouldSendState = true) {
    this.setCurrentPlayState(PlayState.STOPPED)
    this.unload()
    if (shouldSendState) StateManager.get().sendActiveState()
  }

  public void play(String playlistItemId, PlayState playState) {
    this.setCurrentPlayState(playState)

    // Handle empty playlist
    if (this.items.size() == 0) {
      println "[Playlist] Called play, but playlist is empty. Setting state to STOPPED"
      this.stop()
      return
    }

    // handle stopped. this might never be called but we want to make sure we return here anyway
    if (playState == PlayState.STOPPED) {
      this.stop()
      return
    }

    PlaylistItem item = playlistItemId == null ? this.items[0] : this.items.find { it.id == playlistItemId }

    if (!item) {
      //TODO I get runtime exceptions here
      throw new RuntimeException("[Playlist] ERROR!  Could not find the PlaylistItem on this playlist.  Make sure this PlaylistItem existings on this Playlist!")
    }

    this._playPlaylist(item, playState)
  }

  // This method handles playing an item on the playlist w/o changing the 'playState', which is whether we're playing/looping/stopped
  private void _playPlaylist(PlaylistItem item, PlayState playState) {
    // Exit early if we've had our channel removed
//    this means we've switched to a different playlist, but the timer
    if (!this.channel) {
      println "[Playlist] Called _playPlaylist but channel was null"
    }

    // If there is a current timer, cancel it.  one timer at a time folks
    this.cancelCurrentTimer()

    // This case should be handled before we ever get here, but if it happens nbd
    if (playState == PlayState.STOPPED) {
      println "[Playlist] WARNING: Called _playPlaylist while state is STOPPED.  Nothing to do.".yellow()
      return
    }

    this.setCurrentItem(item)

    /*
    // todo: refactor
    // this is a giant hack to stop a previously playing video
    if (this.channel?.scene?.clip instanceof VideoClip) {
      ((VideoClip)this.channel?.scene?.clip)?.movie?.stop();
    }
    */


    this.channel.setScene(item.scene, false, 10);

    //wrap this in a "debug"
    System.out.println("[Playlist] Playing scene '${item.scene.getDisplayName()}' on playlist '${this.displayName}' (Playstate: ${playState})");

    // Schedule the next item
    if (playState == PlayState.PLAYING) {
      long delay = item.duration * 1000 as long;
      this.scheduleNextScene(delay);
    }

    // Send a 'stateUpdated' event to the UI.  we will need to send one of these whenever state changes and we need to update the frontend
    // if we don't send it here, the UI won't get the update when the timer triggers this function

    //This will throw an exception if the clipMetaData doesn't match the clipId
    StateManager.get().sendActiveState()
  }
}
