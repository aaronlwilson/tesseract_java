package clip;

import java.util.*;

import render.Vec3;
import static render.ProcessingCompat.dist;
import static render.ProcessingCompat.map;
import static render.ProcessingCompat.constrain;

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


        //X
        Vec3 xLoc = new Vec3(200.0f, 0.0f, 0.0f);
        int xC = _myMain.color(255, 0, 0);
        _myMain.particleX = addParticle(xLoc, xC);

        //Y
        Vec3 yLoc = new Vec3(0.0f, 200.0f, 0.0f);
        int yC = _myMain.color(0, 255, 0);
        _myMain.particleY = addParticle(yLoc, yC);

        //Z
        Vec3 zLoc = new Vec3(0.0f, 0.0f, 200.0f);
        int zC = _myMain.color(0, 0, 255);
        _myMain.particleZ = addParticle(zLoc, zC);




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



    public Particle addParticle(Vec3 theLoc, int theC ){

        float lowVel = -_pSpeed;
        float highVel = _pSpeed;

        //Vec3 theSpeed = new Vec3(Util.randFloatRange(lowVel,highVel), Util.randFloatRange(lowVel,highVel), Util.randFloatRange(lowVel,highVel));

        Vec3 theSpeed = new Vec3(0.0f, 0.0f, 0.0f);
        Vec3 theAccel = new Vec3(0.0f, 0.0f, 0.0f);


        Particle p = new Particle(theLoc, theC,_pSize, _pRamp, theSpeed, theAccel);
        _particles.add(p);

        return p;
    }

    public int[] drawNode(Node node) {

        int[] nodestate = new int[3];


        float brightness = 0.0f;

        int newRed = 0;
        int newGreen = 0;
        int newBlue = 0;

        for(Particle particle : _particles) {

            Vec3 l = particle.position;
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



        return nodestate;
    }


}