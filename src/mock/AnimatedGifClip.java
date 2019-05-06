package mock;

import clip.AbstractClip;

public class AnimatedGifClip extends AbstractClip {

    String clipId = "animated_gif";
    String displayName = "Animated Gif";

    //constructor
    AnimatedGifClip(String theClipName, String theChannel) {
        super(theClipName, theChannel);
    }
}
