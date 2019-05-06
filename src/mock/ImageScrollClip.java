package mock;

import clip.AbstractClip;

public class ImageScrollClip extends AbstractClip {

    String clipId = "image_scroll";
    String displayName = "Image Scroll";

    //constructor
    ImageScrollClip(String theClipName, String theChannel) {
        super(theClipName, theChannel);
    }
}
