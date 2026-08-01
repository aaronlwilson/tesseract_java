package clip;

import environment.Node;


public class SolidColorClip extends AbstractClip{

    //CLASS VARS

    //RGB
    private int red = 0xff;
    private int green = 0xff;
    private int blue = 0xff;


    //constructor
    public SolidColorClip() {

    }

    public void init() {
        clipId = "solid_color";
        super.init();
    }

    public void run() {
        //map local vars to abstract clip parameters
        red =   (int)(p1*255);
        green = (int)(p2*255);
        blue =  (int)(p3*255);
    }

    public int[] drawNode(Node node) {

        int[] nodestate = new int[3];

        nodestate[0] = red;
        nodestate[1] = green;
        nodestate[2] = blue;

        return nodestate;
    }

    public void die() {

    }
}
