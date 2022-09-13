package clip;

import java.util.*;


import processing.core.PVector;

import environment.Node;
import util.Util;

import static processing.core.PApplet.*;


public class LinesClip  extends AbstractClip {

    //private ArrayList<Particle> _particles;
    private float _pSpeed; //p1
    private float _pSize; //p2
    private float _pRamp; //p3
    private float _pXAlpha; //p4
    private float _pYAlpha; //p5
    private float _pZAlpha; //p6
    private float _pSpinnerAlpha; //p7
    private float _pColorShift; //p8

    private float _speed;


    //constructor
    public LinesClip() {
    }

    public void init() {
        clipId = "lines_clip";
        super.init();

        // _particles = new ArrayList<Particle>();

        //X
        PVector xLoc = new PVector(200.0f, 0.0f, 0.0f);
        int xC = _myMain.color(255, 0, 0);
        _myMain.particleX = addParticle(xLoc, xC);

        //Y
        PVector yLoc = new PVector(0.0f, 200.0f, 0.0f);
        int yC = _myMain.color(0, 255, 0);
        _myMain.particleY = addParticle(yLoc, yC);

        //Z
        PVector zLoc = new PVector(0.0f, 0.0f, 200.0f);
        int zC = _myMain.color(0, 0, 255);
        _myMain.particleZ = addParticle(zLoc, zC);
    }

    public void run() {
        _pSize = p1*200.0f;
        _pRamp = p2*200.0f;

        _pSpeed = p3*5.0f;
        _speed += _pSpeed;

        _pXAlpha = p4;
        _pYAlpha = p5;
        _pZAlpha = p6;
        _pSpinnerAlpha = p7;
        _pColorShift = p8;

        float x = (float) ((_myMain.stage.maxW/2) * Math.cos(radians(_speed)));
        _myMain.particleX.position = new PVector(x, 0.0f, 0.0f);

        float y = (float) ((_myMain.stage.maxH/2) * Math.cos(radians(_speed)));
        _myMain.particleY.position = new PVector(0.0f, y, 0.0f);

        float z = (float) ((_myMain.stage.maxD/2) * Math.cos(radians(_speed)));
        _myMain.particleZ.position = new PVector(0.0f, 0.0f, z);
    }


    public Particle addParticle(PVector theLoc, int theC) {
        PVector theSpeed = new PVector(0.0f, 0.0f, 0.0f);
        PVector theAccel = new PVector(0.0f, 0.0f, 0.0f);

        Particle p = new Particle(theLoc, theC, _pSize, _pRamp, theSpeed, theAccel);
        //_particles.add(p);

        return p;
    }

    public int[] drawNode(Node node) {

        int[] nodestate = new int[3];
        float brightness = 0.0f;
        int newRed = 0;
        int newGreen = 0;
        int newBlue = 0;

//        Particle particle = _myMain.particleX;
//        PVector l = particle.position;

        float distX = Math.abs(node.x - _myMain.particleX.position.x);
        float distY = Math.abs(node.y - _myMain.particleY.position.y);
        float distZ = Math.abs(node.z - _myMain.particleZ.position.z);

        brightness = 1.0f; //TODO: add ramp

        if (distX < _pSize) {
            newRed += (int) (Util.getR(_myMain.particleX.color) * brightness * _pXAlpha);
            newGreen += (int) (Util.getG(_myMain.particleX.color) * brightness * _pXAlpha);
            newBlue += (int) (Util.getB(_myMain.particleX.color) * brightness * _pXAlpha);
        }

        if (distY < _pSize) {
            newRed += (int) (Util.getR(_myMain.particleY.color) * brightness * _pYAlpha);
            newGreen += (int) (Util.getG(_myMain.particleY.color) * brightness * _pYAlpha);
            newBlue += (int) (Util.getB(_myMain.particleY.color) * brightness * _pYAlpha);
        }

        if (distZ < _pSize) {
            newRed += (int) (Util.getR(_myMain.particleZ.color) * brightness * _pZAlpha);
            newGreen += (int) (Util.getG(_myMain.particleZ.color) * brightness * _pZAlpha);
            newBlue += (int) (Util.getB(_myMain.particleZ.color) * brightness * _pZAlpha);
        }

        if(newRed > 0) nodestate[0] = newRed;
        if(newGreen > 0) nodestate[1] = newGreen;
        if(newBlue > 0) nodestate[2] = newBlue;

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


        return nodestate;
    }


}