package show;

import app.TesseractMain;
import clip.*;
import stores.SceneStore;

public class Scene {

    public AbstractClip clip; //use getter setter

    //these are the saved values for the current clips parameters
    // all parameters should be normalized to a range 0.00 - 1.00
    public float p1, p2, p3, p4, p5, p6, p7; // p 1-3 knobs, p 4-7 sliders

    public String displayName;
    public int id;

    // CONSTRUCTORS
    // Use this constructor if you don't want to set clip values right away
    public Scene(String displayName, int clipClass) {
        this(displayName, clipClass, new float[]{0, 0, 0, 0, 0, 0, 0});
    }

    // Use this constructor when creating a new clip w/o an existing ID
    public Scene(String displayName, int clipClass, float[] clipValues) {
        this(SceneStore.get().getNextId(), displayName, clipClass, clipValues);
    }

    // Use this constructor when rehydrating from json
    public Scene(int id, String displayName, int clipClass, float[] clipValues) {
        this.id = id;
        this.displayName = displayName;
        this.setSceneValues(clipValues);
        this.constructNewClip(clipClass);
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setSceneValues(float[] clipValues) {
        p1 = clipValues[0];
        p2 = clipValues[1];
        p3 = clipValues[2];
        p4 = clipValues[3];
        p5 = clipValues[4];
        p6 = clipValues[5];
        p7 = clipValues[6];
    }

    public float[] getSceneValues() {
        return new float[]{p1, p2, p3, p4, p5, p6, p7};
    }

    // set the values on the clip
    private void setClipValues(float[] clipValues) {
        this.clip.p1 = clipValues[0];
        this.clip.p2 = clipValues[1];
        this.clip.p3 = clipValues[2];
        this.clip.p4 = clipValues[3];
        this.clip.p5 = clipValues[4];
        this.clip.p6 = clipValues[5];
        this.clip.p7 = clipValues[6];
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
            case TesseractMain.VIDEO:
                newClip = new VideoClip(TesseractMain.clipNames[clipClass]);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + clipClass);
        }

        if (newClip != null) {
            newClip.init();
            clip = newClip;
            this.setClipValues(this.getSceneValues());
        }
    }


}

