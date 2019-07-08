package clip;

import environment.Node;

public class ParticleClip  extends AbstractClip {

    //constructor
    public ParticleClip(String theClipName) {
        super(theClipName);
    }

    public void init() {

        clipId = "particle_clip";

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
