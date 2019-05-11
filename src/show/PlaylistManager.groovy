package show

import groovy.json.JsonSlurper
import util.Util;

import java.io.File;

public class PlaylistManager {

    public Playlist[] playlists

    public Playlist activePlaylist

    public PlaylistManager() {
        this.loadPlaylists()
    }

    // Load playlists from the file system
    public void loadPlaylists() {
        // read flat files from fs
        // each persistable type will have a manager that will handle reading the json files from the fs
        // the playlist manager will also handle reading the default playlist and setting it to the activePlaylist on startup

        File dataFile = new File("${Util.getDataDir()}/playlists.json")

        if (dataFile.exists()) {
//            String jsonStr = new File("${}").text
//
//            def jsonSlurper = new JsonSlurper()
//            def jsonObj = jsonSlurper.parseText(jsonStr)
//
//            println "Loaded json from file:"
//            println jsonObj

        }


        // need to loop over and create playlist objects

    }

    // Persist playlists to the file system
    public void savePlaylists() {

    }

    // whenever a playlist changes, we need to broadcast the change to all connected clients
}
