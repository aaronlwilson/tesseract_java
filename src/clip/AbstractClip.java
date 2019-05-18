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
  public String filename;

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


}
