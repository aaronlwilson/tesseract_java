package clip;

import environment.Node;
import util.Util;

public class ColorWashClip extends AbstractClip {

    //CLASS VARS
    private float _speed;
    private float _angle;
    private float _rCut = 1;
    private float _gCut = 1;
    private float _bCut = 1;
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

        //_rCut = Util.getPercent(p4, 100) *.01f;
        //_gCut = Util.getPercent(p5, 100) *.01f;
        //_bCut = Util.getPercent(p6, 100) *.01f;
    }

    /*
    public void die() {

    }
    */

    public int[] drawNode(Node node) {
        _angleCalc =  node.y * _angle1;
        _angleCalc += node.x * _angle2;
        //System.out.printf("%.3f ", _angleCalc);

        _angle = _angleCalc*_spreadCalc;

        _myMain.colorMode(_myMain.HSB, 100);  // Use HSB with scale of 0-100

        int c = _myMain.color((_angle+_speed)%100, 100, 100);

        //_color = _myMain.color(50, 100, 100);
        _myMain.colorMode(_myMain.RGB, 255);


        int[] nodestate = new int[3];

        //int values 0-255 for R G and B
        nodestate[0] = Util.getR(c);
        nodestate[1] = Util.getG(c);
        nodestate[2] = Util.getB(c);

        return nodestate; // RGB
    }

}
