package clip;


import processing.video.*;

import environment.Node;
import stores.MediaStore;
import util.Util;



public class VideoClip extends AbstractClip{

    //CLASS VARS

    // Size of each cell in the grid, ratio of window size to video size
    private int _videoScale = 16;
    // Number of columns and rows in the system
    private int _cols, _rows;
    // Step 1. Declare a Movie object.
    private Movie _movie;

    private int _videoW = 640;
    private int _videoH = 360;


    //constructor
    public VideoClip(String theClipName) {
        super(theClipName);
    }

    public void init() {

        clipId = "video";

        super.init();

        // Initialize columns and rows
        _cols = _videoW/_videoScale;
        _rows = _videoH/_videoScale;
    }

    // set the filename for the movie
    // handle creating the movie object and all that
    public void setFilename(String filename) {
        if (!MediaStore.get().containsMedia("videos", filename)) {
            System.out.println("[VideoClip] Warning: Tried to set non-existent mediafile of type 'video' and filename '" + filename + "'");
            return;
        }

        this.filename = filename;

        if (_movie != null) {
            _movie.stop();
        }

        // Step 2. Initialize Movie object. The file should live in the data/videos folder.
        _movie = new Movie(_myMain, "videos/" + filename);

        // Step 3. Start playing movie. To play just once play() can be used instead.
        _movie.loop();
    }

    public void run() {
        _movie.loadPixels();
    }

    public int[] drawNode(Node node) {

        int[] nodestate = new int[3];

        //cool mirroring
        /*
        int x = Math.abs(node.x);
        int y = Math.abs(node.y);

        int vidX = (int) _myMain.map(x, 0, _myMain.stage.maxW, 0, _videoW-1);
        int vidY = (int) _myMain.map(y,0, _myMain.stage.maxH, 0, _videoH-1);
         */


        int vidX = (int) _myMain.map(node.screenX, 0, 1100, 0, _videoW - 1);
        int vidY = (int) _myMain.map(node.screenY, 0, 800, 0, _videoH - 1);

        int loc = vidX + vidY * _videoW;

        // This is to handle a case when we are switching the video file and don't want to get exceptions
        int c = this._movie != null && this._movie.pixels.length > loc ? _movie.pixels[loc] : 0;

        //int values 0-255 for R G and B
        nodestate[0] = Util.getR(c);
        nodestate[1] = Util.getG(c);
        nodestate[2] = Util.getB(c);

        return nodestate;
    }

    public void die() {

    }


}
