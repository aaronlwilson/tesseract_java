package clip;

import app.TesseractMain;
import environment.Node;
import util.Util;

public class ColorWashClip extends AbstractClip {

    //CLASS VARS
    private float _speed;
    private float _angle;
    private int   _color;
    private float _rCut = 1;
    private float _gCut = 1;
    private float _bCut = 1;
    private float _angle1;
    private float _angle2;
    private float _angleCalc;
    private float _spreadCalc;

    private TesseractMain _myMain;

    //constructor
    public ColorWashClip(String theClipName) {
        super(theClipName);
    }

    public void init() {
        clipId = "color_wash";
        TesseractMain _myMain = app.TesseractMain.getMain();

        super.init();
    }


    public void run() {
        //_rCut = Utils.getPercent(p1, 100) *.01f;
        //_gCut = Utils.getPercent(p2, 100) *.01f;
        //_bCut = Utils.getPercent(p3, 100) *.01f;

        _angle1 = p1/100;
        _angle2 = Math.abs((p1-100))/100;

        _speed += p2/10;
        _spreadCalc = p3/50;
    }

    public void die() {

    }

    public int[] drawNode(Node node) {
        _angleCalc =  node.y * _angle1;
        _angleCalc += node.x * _angle2;

        //_angleCalc = Utils.getPercent(node.x, stateManager.stageWidth);
        _angle = _angleCalc*_spreadCalc;

        _myMain.colorMode(_myMain.HSB, 100);  // Use HSB with scale of 0-100
        _color = _myMain.color((_angle+_speed)%100, 100, 100);

        //_color = Color.HSBtoRGB(_speed + _angle, 1, 1);
        //_color = color(50, 100, 100);
        _myMain.colorMode(_myMain.RGB, 255);


        int[] nodestate = new int[3];

        //int values 0-255 for R G and B
        nodestate[0] = Util.getR(_color);
        nodestate[1] = Util.getG(_color);
        nodestate[2] = Util.getB(_color);

        return nodestate; // RGB
    }

}
