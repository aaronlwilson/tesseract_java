package clip;

import environment.Node;


import static processing.core.PApplet.*;

public class PerlinNoiseClip  extends AbstractClip {



    //constructor
    public PerlinNoiseClip() {
    }

    public void init() {

        clipId = "perlin_noise";
        super.init();

    }

    public void run() {


    }

    public int[] drawNode(Node node) {

        int[] nodestate = new int[3];


        nodestate[0] = 255;
        nodestate[1] = 255;
        nodestate[2] = 255;


        return nodestate;
    }
}
