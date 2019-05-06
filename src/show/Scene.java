package show;

import clip.*;

public class Scene {
    int id;
    String displayName;
    AbstractClip channel1Clip;
    AbstractClip channel2Clip;

    private AbstractClip _clip; //use getter setter

    public Scene(int id, String displayName, AbstractClip channel1Clip, AbstractClip channel2Clip) {
        this.id = id;
        this.displayName = displayName;
        this.channel1Clip = channel1Clip;
        this.channel2Clip = channel2Clip;
    }
}
