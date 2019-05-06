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
    }

    private void drawAxes(float size){
        p.strokeWeight (1);
        //X  - red
        p.stroke(192,0,0);
        p.line(0,0,0,size,0,0);
        //Y - green
        p.stroke(0,192,0);
        p.line(0,0,0,0,size,0);
        //Z - blue
        p.stroke(0,0,192);
        p.line(0,0,0,0,0,size);
    }

    public void draw() {

        drawFramerate();

        p.noFill();

        p.translate(p.width/2, p.height/2, -200);


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



        drawAxes(500);


        //draw nodes
        p.strokeWeight(2);
        p.stroke (255, 0, 0);

        /*
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                for (int k = 0; k < 20; k++) {
                    p.point(20 * i, 20 * j, 20 * k);
                }
            }
        }
        */

        TesseractMain myMain = TesseractMain.getMain();

        if(myMain.stage.nodes == null) {
            return;

        }else{
            for (int i = 0; i < myMain.stage.nodes.length; i++) {

                Node node = myMain.stage.nodes[i];
                p.point(node.x, node.y, node.z);
            }
        }




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
}
