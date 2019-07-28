package clip;

import app.TesseractMain;
import environment.Node;

public class AbstractClip  {

  //CLASS VARS
  protected TesseractMain _myMain;

  //display name
  public String clipName;

  // Pretty string 'id' that is implemented by subclasses.  e.g., 'color_wash'.  Used for persistence / rehydration of data
  public String clipId;

  // Params
  // all parameters should be normalized to a range 0.00 - 1.00
  public float p1, p2, p3, p4, p5, p6, p7, p8;
  public String filename;

  protected float[] paramDefaults = new float[7];

  //constructor
  public AbstractClip() {

  }


  // ---- TO BE OVERWRITTEN by Clip Class ------
  public void init() {
    _myMain = app.TesseractMain.getMain();
  }

  public void run() {// animation logic that runs per frame

  }


  public void die() {

  }

  // This is used & overridden by VideoClip
  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getFilename() {
    return this.filename;
  }

  public int[] drawNode(Node node) {// apply the animation logic calculated above to each node

    return new int[3];
  }


}
