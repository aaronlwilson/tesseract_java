package clip;

import environment.Node;

public class AbstractClip  {

  //CLASS VARS
  protected int nodeSpacing = 6;

  //display name
  public String clipName;

  // Pretty string 'id' that is implemented by subclasses.  e.g., 'color_wash'.  Used for persistence / rehydration of data
  public String clipId;

  // Params
  // all parameters should be normalized to a range 0.00 - 1.00
  public float p1, p2, p3, p4, p5, p6, p7; // p 1-3 knobs, p 4-7 sliders
  protected float[] paramDefaults = new float[7];

  //constructor
  public AbstractClip(String theClipName) {
    clipName = theClipName;
  }


  // ---- TO BE OVERWRITTEN by Clip Class ------
  public void init() {

    //animationCanvas.getChannelCanvas(channel).resetAllControllers();
  }

  public void run() {// animation logic that runs per frame

  }

  public void loadFile(String filePath) {

  }

  public void die() {

  }

  public int[] drawNode(Node node) {// apply the animation logic calculated above to each node

    return new int[3];
  }

  public boolean setParamDefault(int param, int value) {
    if(param < 1 || param > 6){
      return false;
    }
    paramDefaults[param-1] = value;
    return true;
  }

  //COLOR utility methods
  //c in this case is a processing type "color" which is really just a 32 bit integer
  static public int getR(int c) {
    return c >> 16 & 0xFF;
  }
  static public int getG(int c) {
    return c >> 8 & 0xFF;
  }
  static public int getB(int c) {
    return c & 0xFF;
  }

}
