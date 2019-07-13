package clip;

import environment.Node;
import model.Palette;
import util.Util;

import static processing.core.PApplet.map;
import static processing.core.PConstants.TWO_PI;

public class PerlinNoiseClip  extends AbstractClip {

    private float _noiseScale;
    private float _theta;

    private float _threshold;

    private int _counter;

    private Palette _palette;

    //constructor
    public PerlinNoiseClip() {
    }

    public void init() {

        clipId = "perlin_noise";
        super.init();

        _counter = 0;

        _palette = new Palette(_myMain);


    }

    public void run() {

        _counter++;

        _noiseScale = 0.01f;

        _theta += TWO_PI / 100;

        _threshold = 0.5f;



    }

    public int[] drawNode(Node node) {

        int[] nodestate = new int[3];


        // Calculate noise and scale by 255
        float nX = node.x * _noiseScale;
        float nY = node.y * _noiseScale;
        float nZ = node.z * _noiseScale;

        float n = _myMain.noise(nX, nY, _theta);
        //int brightness = (int)(n*255);

        // Try using this line instead for white noise
        //int brightness = (int)(Math.random()*255);

        //black and white, this would be cool as a mask over video or other clip
        /*
        int brightness = 0;
        if(n > _threshold){
            brightness = 255;
        }

        nodestate[0] = brightness;
        nodestate[1] = brightness;
        nodestate[2] = brightness;
        */

        //System.out.println(n);


        int l = _palette.colors.length;
        float snap = map(n, 0.0f, 1.0f, 0, l-1);

        int element = (int)Math.floor(snap);
        int c = _palette.colors[element];

        nodestate[0] =   Util.getR(c);
        nodestate[1] = Util.getG(c);
        nodestate[2]  = Util.getB(c);


        return nodestate;
    }
}
