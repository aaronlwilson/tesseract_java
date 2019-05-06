package mock;

import clip.AbstractClip;

public class TextScrollClip extends AbstractClip {

    String clipId = "text_scroll";
    String displayName = "Text Scroll";

    //constructor
    TextScrollClip(String theClipName, String theChannel) {
        super(theClipName, theChannel);
    }
}
