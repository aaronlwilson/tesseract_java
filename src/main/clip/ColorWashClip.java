package clip;

import environment.Node;
import render.ProcessingCompat;
import util.Util;

import static render.ProcessingCompat.map;

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
        // p1 selects the wash direction across the 7 axis combinations below (0..7)
        _angle1 = map(p1, 0, 1, 0, 7);
        _angle2 = p1;

        _speed += p2*3;
        _spreadCalc = p3;
    }

    public int[] drawNode(Node node) {
        // choose which spatial axis (or combination) the color wash runs along
        if (_angle1 < 1) {
            _angleCalc =  node.x;
        } else if (_angle1 < 2) {
            _angleCalc =  node.x;
            _angleCalc += node.y;
        } else if (_angle1 < 3) {
            _angleCalc =  node.y;
        } else if (_angle1 < 4) {
            _angleCalc =  node.y;
            _angleCalc += node.z;
        } else if (_angle1 < 5) {
            _angleCalc =  node.z;
        } else if (_angle1 < 6) {
            _angleCalc =  node.z;
            _angleCalc += node.x;
        } else {
            _angleCalc =  node.x;
            _angleCalc += node.y;
            _angleCalc += node.z;
        }

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
