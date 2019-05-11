package clip;

public class ColorWashClip extends AbstractClip {

    //CLASS VARS
    private int _color;

    int numColors = 3;
    int[] c = new int[numColors*3];
    int currentNode = 0;
    int count   = 0;

    float hue = 100;
    float saturation = 100;
    float brightness = 100;

    //constructor
    public ColorWashClip(String theClipName) {
        super(theClipName);
    }


}
