package clip;

import processing.video.*;

import environment.Node;



public class VideoClip extends AbstractClip{

    //CLASS VARS

    // Size of each cell in the grid, ratio of window size to video size
    private int _videoScale = 16;
    // Number of columns and rows in the system
    private int _cols, _rows;
    // Step 1. Declare a Movie object.
    private Movie _movie;


    //constructor
    public VideoClip(String theClipName) {
        super(theClipName);
    }

    public void init() {
        super.init();

        //load a video


    }

    public void run() {

    }


    public int[] drawNode(Node node) {

        int[] nodestate = new int[3];

        //nodestate[0] = red;
        //nodestate[1] = green;
        //nodestate[2] = blue;

        return nodestate;
    }

    public void die() {

    }


}
