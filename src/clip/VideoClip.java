package clip;

import app.TesseractMain;
import processing.video.*;

import environment.Node;
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

    private TesseractMain _myMain;



    //constructor
    public VideoClip(String theClipName) {
        super(theClipName);
    }

    public void init() {

        clipId = "video";
        _myMain = app.TesseractMain.getMain();
        super.init();

        // Initialize columns and rows
        _cols = _videoW/_videoScale;
        _rows = _videoH/_videoScale;

        // Step 2. Initialize Movie object. The file "testmovie.mov" should live in the data folder.
        _movie = new Movie(_myMain, "videos/24K_loop-nosound.mp4");

        // Step 3. Start playing movie. To play just once play() can be used instead.
        _movie.loop();
    }

    public void run() {
        _movie.loadPixels();


    }

    // Step 4. Read new frames from the movie.
    void movieEvent(Movie movie) {
        movie.read();
    }


    public int[] drawNode(Node node) {

        int[] nodestate = new int[3];

        int c = _movie.pixels[0];

        //int values 0-255 for R G and B
        nodestate[0] = Util.getR(c);
        nodestate[1] = Util.getG(c);
        nodestate[2] = Util.getB(c);

        return nodestate;
    }

    public void die() {

    }


}
