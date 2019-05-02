package show;

public class PlaylistManager {

    public Playlist[] playlists;

    public Playlist activePlaylist;

    public PlaylistManager() {

    }

    // Load playlists from the file system
    public void loadPlaylists() {
        // read flat files from fs
        // each persistable type will have a manager that will handle reading the json files from the fs
        // the playlist manager will also handle reading the default playlist and setting it to the activePlaylist on startup
    }
}
