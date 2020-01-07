package clip;


import processing.video.*;

import environment.Node;
import stores.MediaStore;
import util.Util;



public class VideoClip extends AbstractClip{

    //CLASS VARS
    private float _pThreshold;

    // Size of each cell in the grid, ratio of window size to video size
    private int _videoScale = 16;
    // Number of columns and rows in the system
    private int _cols, _rows;

    //Declare a Movie object.
    public Movie movie;

    private int _videoW = 640;
    private int _videoH = 360;

    // We have to define the field here too or Groovy tries to call setFilename when we do 'this.filename = filename' below, leading to an infinite loop.  Annoying
    public filename;

    //constructor
    public VideoClip() {
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

        if (movie != null) {
            movie.stop();
        }

        // Step 2. Initialize Movie object. The file should live in the data/videos folder.
        movie = new Movie(_myMain, "videos/" + filename);
    }

    public void run() {

        _pThreshold = p1*255.0f;

        if(movie != null) {
            if (!movie.playbin.isPlaying()) {
                // Step 3. Start playing movie. To play just once play() can be used instead.
                movie.loop();
            }

            movie.loadPixels();
        }
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


        int vidX = (int) _myMain.map(node.screenX, 0, 1400, 0, _videoW-1);
        int vidY = (int) _myMain.map(node.screenY,0, 800, 0, _videoH-1);



        int loc = vidX + vidY * _videoW;
        int c = 0;
        if(movie != null) {
            //make sure we don't overrun the array which can happen when pixels go offscreen
            // This is to handle a case when we are switching the video file and don't want to get exceptions
            if (loc >= 0 && loc < movie.pixels.length) {
                // Unfortunately, with the upgraded video library, the 'pixels' array on the 'movie' object is never filled with data (all 0s)
                //
                // The movie object has no 'bufferSync' property, so it never fills the 'pixel' array with the pixes
                // stored in 'copyPixels' (I stepped thru the code w/ a debugger).  It doesn't have a 'bufferSync' because
                // an internal call to 'this.parent.g.getCache(this);' returns null
                // I have no clue why any of this should be the case, or why the behavior differs from the v1 release of the library,
                // but using copyPixels works for our purposes.  The file needs to be a .groovy file, so we can access the 'protected' field 'copyPixels'.
                //
                // Colors from copyPixels are inverted. Using the - (negative) sign results in strange results, because its 32 bit and does not account for alpha. Using the -, the videos certainly did not look correct so lets leave it like this for now.
                // We will add some master filters like "invert", and "hue wheel"
                c = movie.copyPixels[loc];
            }
        }

        int r = Util.getR(c);
        int g = Util.getG(c);
        int b = Util.getB(c);

        if(r < _pThreshold) r = 0;
        if(g < _pThreshold) g = 0;
        if(b < _pThreshold) b = 0;

        //int values 0-255 for R G and B
        nodestate[0] = r;
        nodestate[1] = g;
        nodestate[2] = b;

        return nodestate;
    }

    public void die() {
        movie.stop();

        super.die();
    }


}
