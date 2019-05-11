package show;

import app.TesseractMain;
import clip.*;

public class Scene {



    public AbstractClip clip; //use getter setter

    //these are the saved values for the current clips parameters
    // all parameters should be normalized to a range 0.00 - 1.00
    public float p1, p2, p3, p4, p5, p6, p7; // p 1-3 knobs, p 4-7 sliders


    //CONSTRUCTOR
    public Scene() {


    }

    public void constructNewClip(int clipClass) {

        AbstractClip newClip = new AbstractClip("Abstract Clip");
        switch (clipClass) {
            case TesseractMain.NODESCAN:
                newClip = new NodeScanClip(TesseractMain.clipNames[clipClass]);
                break;
            case TesseractMain.SOLID:
                newClip = new SolidColorClip(TesseractMain.clipNames[clipClass]);
                break;
            case TesseractMain.COLORWASH:
                newClip = new ColorWashClip(TesseractMain.clipNames[clipClass]);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + clipClass);
        }

        if (newClip != null) {
            newClip.init();
            clip = newClip;

            //apply the saved values on this scene to the clip
            clip.p1 = p1;
            clip.p2 = p2;
            clip.p3 = p3;
            clip.p4 = p4;
            clip.p5 = p5;
            clip.p6 = p6;
        }
    }


}

