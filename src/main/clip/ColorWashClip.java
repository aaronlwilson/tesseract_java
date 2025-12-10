package clip;

import environment.Node;
import render.ProcessingCompat;
import util.Util;

public class ColorWashClip extends AbstractClip {

    //CLASS VARS
    private float _speed;
    private float _angle;
    private float _angle1;
    private float _angle2;
    private float _angleCalc;
    private float _spreadCalc;


    //constructor
    public ColorWashClip() {

    }

    public void init() {
        clipId = "color_wash";
        super.init();
    }


    public void run() {
        _angle1 = p1;
        _angle2 = p1;

        _speed += p2*3;
        _spreadCalc = p3;
    }

    public int[] drawNode(Node node) {
        _angleCalc =  node.y * _angle1;
        _angleCalc += node.x * _angle2;

        _angle = _angleCalc*_spreadCalc;

        // HSB color with scale 0-100
        int c = ProcessingCompat.colorHSB((_angle+_speed)%100, 100, 100, 100);

        int[] nodestate = new int[3];

        //int values 0-255 for R G and B
        nodestate[0] = Util.getR(c);
        nodestate[1] = Util.getG(c);
        nodestate[2] = Util.getB(c);

        return nodestate; // RGB
    }

}
