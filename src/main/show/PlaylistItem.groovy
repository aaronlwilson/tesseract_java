package show

import stores.SceneStore
import util.Util

// Playlist item: One scene + one duration.  and any other fields we decide we want
public class PlaylistItem {
  // This needs its own ID so we know which item we're playing on the frontend and backend (and can agree)
  // There can be more than one of the same scene per playlist, so we can't use the scene id
  // This doesn't need to be persistent, but it needs to be unique per instance of the backend
  String id;

  // apparently omitting a visibility declaration (public/private) tells groovy to automatically create a private field
  // w/ getters and setters. whodathunkit. props to you groovy. http://groovy-lang.org/style-guide.html#_getters_and_setters
  Scene scene;
  Integer duration;

  public PlaylistItem(String id, String sceneDisplayName, Integer duration) {
    // We have to do this hacky-ish thing to throw an exception in our overloaded constructor.  We want to do the error
    // checking, but the call to the other constructor must be the first line in this constructor
    this(id, SceneStore.get().find("displayName", sceneDisplayName) ?: Util.throwException("ERROR: Tried to create PlaylistItem with non-existent Scene '${sceneDisplayName}'".red()), duration)
  }

  public PlaylistItem(String id, Scene scene, Integer duration) {
    this.id = id;
    this.scene = scene;
    this.duration = duration;
  }
}
