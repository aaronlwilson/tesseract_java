package mock;

import clip.AbstractClip;

public class ColorWashClip extends AbstractClip {

    String clipId = "color_wash";
    String displayName = "Color Wash";

    //constructor
    ColorWashClip(String theClipName, String theChannel) {
        super(theClipName, theChannel);
    }
}
