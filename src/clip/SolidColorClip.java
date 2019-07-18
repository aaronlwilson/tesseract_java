package clip;

import environment.Node;
import processing.core.PApplet;


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


        //PUT BACK this bit for production
        /*
        _myMain.colorMode(PApplet.HSB, 100);  // Use HSB with scale of 0-100
        int color = color(hue, saturation, brightness);
        _myMain.colorMode(PApplet.RGB, 255);

        nodestate[0] = int(Utils.getR(color));
        nodestate[1] = int(Utils.getG(color));
        nodestate[2] = int(Utils.getB(color));
        */

        nodestate[0] = red;
        nodestate[1] = green;
        nodestate[2] = blue;

        return nodestate;
    }

    public void die() {

    }
}
