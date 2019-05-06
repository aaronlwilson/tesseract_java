package show

import clip.AbstractClip;

public class Scene {
    int id
    String displayName
    AbstractClip channel1Clip
    AbstractClip channel2Clip

    Scene(int id, String displayName, AbstractClip channel1Clip, AbstractClip channel2Clip) {
        this.id = id
        this.displayName = displayName
        this.channel1Clip = channel1Clip
        this.channel2Clip = channel2Clip
    }
}
