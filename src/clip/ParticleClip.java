package clip;

import java.util.*;
import processing.core.PVector;
import static processing.core.PApplet.dist;


import environment.Node;
import util.Util;




public class ParticleClip  extends AbstractClip {

    private ArrayList<Particle> _particles;

    private float _pSize;
    private float _pSpeed;
    private float _pAccel;
    private float _pDensity;

    private float _pRamp;

    private int _counter;


    //constructor
    public ParticleClip(String theClipName) {
        super(theClipName);
    }

    public void init() {

        clipId = "particle_clip";
        super.init();

        _particles = new ArrayList<Particle>();
    }

    public void run() {

        //map local vars to abstract clip parameters
        _pSize = p1*200.0f;
        _pSpeed = p2*20.0f;
        _pAccel = p3;
        _pDensity = (p4*30)+1;


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


    }

    public static float randFloatRange(float min, float max) {
        Random rand = new Random();
        float result = rand.nextFloat() * (max - min) + min;
        return result;
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
        //println(hex(theC));


        float lowVel = -_pSpeed;
        float highVel = _pSpeed;

        PVector theSpeed = new PVector(randFloatRange(lowVel,highVel), randFloatRange(lowVel,highVel), randFloatRange(lowVel,highVel));

        PVector theAccel = new PVector(0.0f, 0.0f, 0.0f);


        _particles.add(new Particle(theLoc, theC,100.0f, theSpeed, theAccel));

    }

    public int[] drawNode(Node node) {

        int[] nodestate = new int[3];

        float brightness = 0.0f;

        int newRed = 0;
        int newGreen = 0;
        int newBlue = 0;


        for(Particle particle : _particles) {

            PVector l = particle.position;
            float dist = dist(node.x, node.y, node.z, l.x, l.y, l.z);
            float surface = particle.size;

            if (dist < surface) {
                brightness = 0.5f;
            }

            newRed +=   (int)(Util.getR(particle.color) * brightness);
            newGreen += (int)(Util.getG(particle.color) * brightness);
            newBlue  += (int)(Util.getB(particle.color) * brightness);


        }

        nodestate[0] = newRed;
        nodestate[1] = newGreen;
        nodestate[2] = newBlue;


        return nodestate;
    }


}
