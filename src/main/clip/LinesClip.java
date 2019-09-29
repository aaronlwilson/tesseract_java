package clip;

import java.util.*;


import hardware.Tile;
import processing.core.PVector;
import static processing.core.PApplet.dist;
import static processing.core.PApplet.map;
import static processing.core.PApplet.constrain;


import environment.Node;
import util.Util;




public class LinesClip  extends AbstractClip {

    private ArrayList<Particle> _particles;


    private float _pSize;
    private float _pRamp;
    private float _pSpeed;
    private float _pAccel;
    private float _pDensity;


    private int _counter;


    //constructor
    public LinesClip() {
    }

    public void init() {

        clipId = "lines_clip";
        super.init();

        _particles = new ArrayList<Particle>();
    }

    public void run() {

        /*

        //map local vars to abstract clip parameters
        _pSize = p1*200.0f;
        _pRamp = p2*200.0f;

        _pSpeed = p3*20.0f;
        _pAccel = p4;
        _pDensity = (p5*30)+1;


        int length = _particles.size()-1;
        for (int i = length; i >= 0; i--) {
            Particle p = _particles.get(i);
            p.run();
            if (p.isDead()) {
                _particles.remove(i);
            }
        }

        _counter++;
        if(_counter >= _pDensity){
            _counter = 0;
            addParticle();
        }

        */
    }



    public void addParticle(){

        //birthing point
        PVector theLoc = new PVector(0.0f, -30.0f, 0.0f);

        //int theC = _myMain.color(255, 255, 255);

        //each particle is a little different
        int rBlue = (int)(Math.random()*255);
        int rRed =  (int)(Math.random()*255);
        int rGreen = (int)(Math.random()*255);

        int theC = _myMain.color(rRed, rBlue, rGreen);


        float lowVel = -_pSpeed;
        float highVel = _pSpeed;

        PVector theSpeed = new PVector(Util.randFloatRange(lowVel,highVel), Util.randFloatRange(lowVel,highVel), Util.randFloatRange(lowVel,highVel));
        PVector theAccel = new PVector(0.01f, 0.01f, 0.01f);

        _particles.add(new Particle(theLoc, theC,_pSize, _pRamp, theSpeed, theAccel));

    }

    public int[] drawNode(Node node) {

        int[] nodestate = new int[3];

        /*
        float brightness = 0.0f;

        int newRed = 0;
        int newGreen = 0;
        int newBlue = 0;

        for(Particle particle : _particles) {

            PVector l = particle.position;
            float dist = dist(node.x, node.y, node.z, l.x, l.y, l.z);
            float surface = particle.size;

            if (dist < surface) {
                brightness = 1.0f;

                newRed = (int) (Util.getR(particle.color) * brightness);
                newGreen = (int) (Util.getG(particle.color) * brightness);
                newBlue = (int) (Util.getB(particle.color) * brightness);

            }else if (dist >= surface+particle.ramp) {
                //this particle has no effect on this node
            }else{
                //ramp calculates a soft leading edge to the brightness threshold
                brightness = map(dist, surface, surface+particle.ramp, 1.0f, 0);
                brightness = constrain(brightness, 0, 1.0f);

                newRed += (int)(Util.getR(particle.color) * brightness);
                newGreen += (int)(Util.getG(particle.color) * brightness);
                newBlue  += (int)(Util.getB(particle.color) * brightness);
            }

        }

        if(newRed > 0) nodestate[0] = newRed;
        if(newGreen > 0) nodestate[1] = newGreen;
        if(newBlue > 0) nodestate[2] = newBlue;
        */

        // Checking if t.equals null or works fine.
        try
        {
            // This line of code throws NullPointerException
            // because t is null
            Tile t = node.fixture;
            //int c = t.numberPImage.pixels[node.index];
            int c = t.numberColorForNodeIndex(node.index);

            nodestate[0] = Util.getR(c);
            nodestate[1] = Util.getG(c);
            nodestate[2] = Util.getB(c);
        }
        catch(NullPointerException e)
        {
            //System.out.println("NullPointerException Caught");
        }



        return nodestate;
    }


}