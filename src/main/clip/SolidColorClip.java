package clip;

import environment.Node;


public class SolidColorClip extends AbstractClip{

    //CLASS VARS

    //HSB
    private float hue = 100;
    private float saturation = 100;
    private float brightness = 100;

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
        hue = p1;
        saturation = p2;
        brightness = p3;

        red =   (int)(p4*255);
        green = (int)(p5*255);
        blue =  (int)(p6*255);
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
