package clip;

public class SolidColorClip extends AbstractClip {

    String clipId = "solid_color";
    String displayName = "Solid Color";

    //constructor
    SolidColorClip(String theClipName, String theChannel) {
        super(theClipName, theChannel);
    }
}
