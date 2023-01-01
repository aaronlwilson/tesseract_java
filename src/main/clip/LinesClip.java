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
    private boolean _spinWallMode = true; // true = full-height wall, false = 3D segment spoke

    //constructor
    public LinesClip() {
    }

    public void init() {
        clipId = "lines_clip";
        super.init();

        // _particles = new ArrayList<Particle>();

        //X -red
        PVector xLoc = new PVector(200.0f, 0.0f, 0.0f);
        int xC = _myMain.color(255, 0, 0);
        _myMain.particleX = addParticle(xLoc, xC);

        //Y -green
        PVector yLoc = new PVector(0.0f, 200.0f, 0.0f);
        int yC = _myMain.color(0, 255, 0);
        _myMain.particleY = addParticle(yLoc, yC);

        //Z -blue
        PVector zLoc = new PVector(0.0f, 0.0f, 200.0f);
        int zC = _myMain.color(0, 0, 255);
        _myMain.particleZ = addParticle(zLoc, zC);

        //spin
        PVector spinLoc = new PVector(0.0f, 0.0f, 0.0f);
        int spinC = _myMain.color(255, 0, 255);
        _myMain.particleSpin = addParticle(spinLoc, spinC);
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

        float y = (float) ((_myMain.stage.maxH/2) * Math.sin(radians(_speed)));
        _myMain.particleY.position = new PVector(0.0f, y, 0.0f);

        float z = (float) ((_myMain.stage.maxD/2) * Math.cos(radians(_speed)));
        _myMain.particleZ.position = new PVector(0.0f, 0.0f, z);
        //------

        float spinX = (float) ((_myMain.stage.maxW/2) * Math.sin(radians(_speed)));
        float spinY = (float) ((_myMain.stage.maxH/2) * Math.cos(radians(_speed)));
        _myMain.particleSpin.position = new PVector(spinX, spinY, 0.0f);
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


        float distX = Math.abs(node.x - _myMain.particleX.position.x);
        float distY = Math.abs(node.y - _myMain.particleY.position.y);
        float distZ = Math.abs(node.z - _myMain.particleZ.position.z);

        brightness = 1.0f; //TODO: add ramp

        //X, Y, Z particles
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


        // Line from origin (0,0,0) to particleSpin position
        PVector spinPos = _myMain.particleSpin.position.copy();

        if (_spinWallMode) { // Spinning line/wall
            // Project spin direction into XZ plane — wall extends full height
            PVector lineDirXY = new PVector(spinPos.x, spinPos.y, 0);
            float lineLen2D = lineDirXY.mag();

            if (lineLen2D > 0.001f) {
                lineDirXY.normalize();

                // Ignore Z — wall extends full depth
                PVector nodeXY = new PVector(node.x, node.y, 0);
                float t = nodeXY.dot(lineDirXY);

                if (t >= 0 && t <= lineLen2D) {
                    PVector cross = nodeXY.cross(lineDirXY);
                    float distToWall = cross.mag();

                    if (distToWall < _pSize) {
                        newRed   += (int)(Util.getR(_myMain.particleSpin.color) * _pSpinnerAlpha);
                        newGreen += (int)(Util.getG(_myMain.particleSpin.color) * _pSpinnerAlpha);
                        newBlue  += (int)(Util.getB(_myMain.particleSpin.color) * _pSpinnerAlpha);
                    }
                }
            }

        } else {
            // 3D segment spoke — distance in full XYZ, bounded by segment endpoints
            float lineLen = spinPos.mag();

            if (lineLen > 0.001f) {
                PVector lineDir = spinPos.copy();
                lineDir.normalize();

                PVector nodeVec = new PVector(node.x, node.y, node.z);
                float t = nodeVec.dot(lineDir);

                if (t >= 0 && t <= lineLen) {
                    PVector cross = nodeVec.cross(lineDir);
                    float distToLine = cross.mag();

                    if (distToLine < _pSize) {
                        newRed   += (int)(Util.getR(_myMain.particleSpin.color) * _pSpinnerAlpha);
                        newGreen += (int)(Util.getG(_myMain.particleSpin.color) * _pSpinnerAlpha);
                        newBlue  += (int)(Util.getB(_myMain.particleSpin.color) * _pSpinnerAlpha);
                    }
                }
            }
        }

        if(newRed > 0) nodestate[0] = newRed;
        if(newGreen > 0) nodestate[1] = newGreen;
        if(newBlue > 0) nodestate[2] = newBlue;

/*
        //PINWHEEL effect
        int spokes = 1;
        float t = atan2(node.x, node.y);         // Convert cartesian to polar

        // Compute 2D polar coordinate function
        float val = sin((t*spokes) + _speed/10);
        int b = (int) ((val + 1.0) * (255.0/2.0));

        //leds[i] = setPixelBrightness(color, b);
        if(b > 0) nodestate[0] = b;
        if(b > 0) nodestate[1] = b;
        if(b > 0) nodestate[2] = b;
*/



        return nodestate;
    }


}