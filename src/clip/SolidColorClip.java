package clip;

import environment.Node;


public class SolidColorClip extends AbstractClip{

    //CLASS VARS
    private int numColors = 3;
    private int[] c = new int[numColors];
    private int currentNode = 0;
    private int count = 0;

    //HSB
    private float hue = 100;
    private float saturation = 100;
    private float brightness = 100;

    //RGB
    private int red = 0xff;
    private int green = 0xff;
    private int blue = 0xff;

    String clipId = "solid_color";


    //constructor
    public SolidColorClip(String theClipName) {
        super(theClipName);
    }

    public void init() {
        super.init();

        c[0] = 0xff0000;
        c[1] = 0x00ff00;
        c[2] = 0x0000ff;

    }

    public void run() {
        currentNode++;//increment node for scanning effect
        if(currentNode >= 144){
            currentNode = 0;

            count+=3;//increment color
            if(count >= (numColors*3)){
                count = 0;
            }
        }

        //map local vars to abstract clip parameters
        hue = p1;
        saturation = p2;
        brightness = p3;

        red =   (int)(p4*255);
        green = (int)(p5*255);
        blue =  (int)(p6*255);
    }

    public void die() {

    }

    public int[] drawNode(Node node) {

        int[] nodestate = new int[3];

        //FOR QUICKLY TESTING TILES, JUST ROTATE ALL RED, GREEN, BLUE
        /*
        nodestate[0] = c[count]; //b
        nodestate[1] = c[count]; //r
        nodestate[2] = c[count]; //g
        */

        /*
        //PUT BACK this bit for production
        colorMode(HSB, 100);  // Use HSB with scale of 0-100
        int color = color(hue, saturation, brightness);
        colorMode(RGB, 255);

        nodestate[0] = int(Utils.getR(color));
        nodestate[1] = int(Utils.getG(color));
        nodestate[2] = int(Utils.getB(color));
        */

        nodestate[0] = red;
        nodestate[1] = green;
        nodestate[2] = blue;

        return nodestate;
    }


}
