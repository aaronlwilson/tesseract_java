package app;

import processing.core.PApplet;
import environment.*;


public class OnScreen {

    private PApplet p;

    private float _xrot;
    private float _yrot;
    private float _newXrot;
    private float _newYrot;
    private float _xStart;
    private float _yStart;
    private float _xDelta;
    private float _yDelta;
    private float _xMove;
    private float _yMove;

    private TesseractMain _myMain;


    public OnScreen(PApplet pApplet) {
        p = pApplet;

        _xrot = 0;
        _yrot = 0;
        _newXrot = 0;
        _newYrot = 0;
        _xStart = 0;
        _yStart = 0;
        _xDelta = 0;
        _yDelta = 0;
        _xMove = 0;
        _yMove = 0;

        _myMain = TesseractMain.getMain();
    }

    private void drawAxes(float size){
        p.strokeWeight (1);
        //X  - red
        p.stroke(220,0,0);
        p.line(0,0,0,size,0,0);
        //Y - green
        p.stroke(0,220,0);
        p.line(0,0,0,0,size,0);
        //Z - blue
        p.stroke(0,0,220);
        p.line(0,0,0,0,0,size);
    }

    public void draw() {

        p.background(20);
        p.noFill();
        p.ortho();

        drawFramerate();

        //center camera and move backward
        p.translate(p.width/2, (p.height/2), 0);

        //explore the world
        if(p.mousePressed) {
            _xDelta = _xStart - p.mouseX;
            _yDelta = _yStart - p.mouseY;

        }else{
            _xDelta = 0;
            _yDelta = 0;
        }

        _newXrot = _xMove - _xDelta;
        _newYrot = _yMove - _yDelta;

        //easing
        float diff = _xrot-_newXrot;
        if (Math.abs(diff) >  0.01) { _xrot -= diff/6.0; }

        diff = _yrot-_newYrot;
        if (Math.abs(diff) >  0.01) { _yrot -= diff/6.0; }

        p.rotateX(p.map(_yrot,0, p.height, p.PI, -p.PI));
        p.rotateY(p.map(_xrot,0, p.width, p.PI, -p.PI));

        drawAxes(600);


        p.pushMatrix();

        //because the coordinate system changes with every rotate call, the axes of rotation "sticks" to our object. This is not what we want.
        //we want to translate the object on multiple axes using the current global coordinates.
        float valueX = 45, valueY = 0, valueZ = 35.3f;

        //for Tesseract only
        //rotateXYZ(p.radians(valueX), p.radians(valueY), p.radians(valueZ));


        //draw nodes
        p.strokeWeight(4);

        if(_myMain.stage.nodes != null) {
            int l = _myMain.stage.nodes.length;
            for (int i = 0; i < l; i++) {
                Node node = _myMain.stage.nodes[i];
                p.stroke(node.r, node.g, node.b);
                p.point(node.x, node.y, node.z);

                //record the "projected" node position in 2D space
                float nX = (float)node.x;
                float nY = (float)node.y;
                float nZ = (float)node.z;

                node.screenX = p.screenX(nX, nY, nZ);
                node.screenY = p.screenY(nX, nY, nZ);
            }
        }

        p.popMatrix();


    }

    private void drawFramerate()
    {
        //show framerate, upper right corner
        p.textSize (14);
        p.fill(160);
        p.text("FPS " + p.str(p.floor(p.frameRate)), p.width-60, 20);
    }



    public void mousePressed() {
        _xStart = p.mouseX;
        _yStart = p.mouseY;
        //p.println(_xStart);
    }

    public void mouseReleased() {
        _xMove = _xMove - _xDelta;
        _yMove = _yMove - _yDelta;
    }

    private void rotateXYZ(float xx, float yy, float zz) {
        float cx, cy, cz, sx, sy, sz;

        cx = p.cos(xx);
        cy = p.cos(yy);
        cz = p.cos(zz);
        sx = p.sin(xx);
        sy = p.sin(yy);
        sz = p.sin(zz);

        p.applyMatrix(cy*cz, (cz*sx*sy)-(cx*sz), (cx*cz*sy)+(sx*sz), 0.0f,
                cy*sz, (cx*cz)+(sx*sy*sz), (-cz*sx)+(cx*sy*sz), 0.0f,
                -sy, cy*sx, cx*cy, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f);
    }
}
