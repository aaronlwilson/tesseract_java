
import processing.core.PApplet;


public class OnScreen {

    private PApplet p;

    private float _xmag;
    private float _ymag;
    private float _newXmag;
    private float _newYmag;

    public OnScreen(PApplet pApplet) {
        p = pApplet;

        _xmag = 0;
        _ymag = 0;
        _newXmag = 0;
        _newYmag = 0;
    }

    public void draw() {

        p.noFill();
        p.strokeWeight (3);
        p.stroke (255, 0, 0);



        p.translate(p.width/2, p.height/2, 0);

        float diff = _xmag-_newXmag;
        if (Math.abs(diff) >  0.01) { _xmag -= diff/5.0; }

        diff = _ymag-_newYmag;
        if (Math.abs(diff) >  0.01) { _ymag -= diff/5.0; }

        if(p.mousePressed){
            _newXmag = ((p.mouseX*.5f) /p.width) * p.TWO_PI;
            _newYmag = ((p.mouseY*.5f) /p.height) * p.TWO_PI;
        }

        p.rotateX(-_ymag);
        p.rotateY(-_xmag);

        //println(width);

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                for (int k = 0; k < 20; k++) {
                    p.point(20 * i, 20 * j, 20 * k);
                }
            }
        }


    }
}
