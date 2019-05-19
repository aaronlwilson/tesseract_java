package show

import model.Channel
import state.StateManager;

public class Playlist {
  int id;
  String displayName;
  Channel channel;
  Integer defaultDuration;
  List<PlaylistItem> items;

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
  }

  public void setChannel(Channel channel) {
    this.channel = channel;
  }

  public PlaylistItem getCurrentItem() {
    this.items[this.currentIdx];
  }

  // Plays a scene
  private void playItem(PlaylistItem item) {
    this.channel.setScene(item.scene, false, 10);

    System.out.println("Playing scene '${item.scene.getDisplayName()}' on playlist '${this.displayName}'");
  }

  private void scheduleNextScene(long delay) {
    TimerTask task = new TimerTask() {
      public void run() {
        // always repeat for now
        currentIdx++;
        if (currentIdx >= items.size()) {
          currentIdx = 0;
        }

        play();

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

  // todo: add ability to play a specific playlist
  public void play(boolean shouldScheduleNextItem = true) {
    PlaylistItem currentItem = this.items[this.currentIdx];
    this.playItem(currentItem);

    // Schedule the next item
    if (shouldScheduleNextItem) {
      PlaylistItem nextItem = this.items[this.currentIdx];
      long delay = nextItem.duration * 1000 as long;
      this.scheduleNextScene(delay);
    }

    // Send a 'stateUpdated' event to the UI.  we will need to send one of these whenever state changes and we need to update the frontend
    StateManager.get().sendActiveState()
  }

  public void stop() {
    // here is where we would cancel the timer
  }
}
