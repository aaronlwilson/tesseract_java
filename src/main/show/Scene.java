package show;

import app.TesseractMain;
import clip.*;
import stores.SceneStore;
import util.Util;

public class Scene {

    public AbstractClip clip; //use getter setter

    //these are the saved values for the current clips parameters
    // all parameters should be normalized to a range 0.00 - 1.00
    public float p1, p2, p3, p4, p5, p6, p7, p8;

    // Only used for Video clips right now
    public String filename;

    public String displayName;
    public int id;

    // CONSTRUCTORS
    // Use this constructor if you don't want to set clip values right away
    public Scene(String displayName, int clipClass) {
        this(displayName, clipClass, new float[]{0, 0, 0, 0, 0, 0, 0, 0}, null);
    }

    // Use this constructor when creating a new clip w/o an existing ID
    public Scene(String displayName, int clipClass, float[] clipValues, String filename) {
        this(SceneStore.get().getNextId(), displayName, clipClass, clipValues, filename);
    }

    // Use this constructor when rehydrating from json
    public Scene(int id, String displayName, int clipClass, float[] clipValues) {
        this(id, displayName, clipClass, clipValues, null);
    }

    //other constructors all lead here
    public Scene(int id, String displayName, int clipClass, float[] clipValues, String filename) {
        this.id = id;
        this.displayName = displayName;
        this.setSceneValues(clipValues);
        this.filename = filename;
        this.constructNewClip(clipClass);
    }

    // When we change the current Scene's clip, we call this to load the new clip
    public void setClipByClipId(String clipId) {
        // if we've already got the correct clip, do nothing so we don't interrupt the animation
        if (this.clip.clipId == clipId) {
            return;
        }
        Integer clipClass = Util.getClipEnumValue(clipId);
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
        p8 = clipValues[7];
    }

    public float[] getSceneValues() {
        return new float[]{p1, p2, p3, p4, p5, p6, p7, p8};
    }

    // set the values on the clip
    private void setClipValues(float[] clipValues, String filename) {
        this.clip.p1 = clipValues[0];
        this.clip.p2 = clipValues[1];
        this.clip.p3 = clipValues[2];
        this.clip.p4 = clipValues[3];
        this.clip.p5 = clipValues[4];
        this.clip.p6 = clipValues[5];
        this.clip.p7 = clipValues[6];
        this.clip.p8 = clipValues[7];

        // todo: make the controls for different clips more consistent
        this.clip.setFilename(filename);
    }

    public void constructNewClip(int clipClass) {

        AbstractClip newClip = new AbstractClip();
        switch (clipClass) {
            case TesseractMain.NODESCAN:
                newClip = new NodeScanClip();
                break;
            case TesseractMain.SOLID:
                newClip = new SolidColorClip();
                break;
            case TesseractMain.COLORWASH:
                newClip = new ColorWashClip();
                break;
            case TesseractMain.VIDEO:
                newClip = new VideoClip();
                break;
            case TesseractMain.PARTICLE:
                newClip = new ParticleClip();
                break;
            case TesseractMain.PERLINNOISE:
                newClip = new PerlinNoiseClip();
                break;


            default:
                throw new IllegalStateException("Unexpected value: " + clipClass);
        }

        if (newClip != null) {
            newClip.init();
            clip = newClip;
            this.setClipValues(this.getSceneValues(), this.filename);
        }
    }


}

