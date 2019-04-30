package clip;

import model.*;

class AbstractClip  {
  
  //CLASS VARS
  protected int nodeSpacing = 6;
 
  public String clipName;
  public String channel;
 
  protected int[] nodestate = new int[5]; // RGBXY
  protected int r, g, b, x, y;
  
  // Params
  public float p1, p2, p3, p4, p5, p6, p7; // p 1-3 knobs, p 4-7 sliders
  protected float[] paramDefaults = new float[7];
  
  //constructor
  AbstractClip(String theClipName, String theChannel) {
    clipName = theClipName;
    channel = theChannel;
    
    //init mem for array to be passed back
    nodestate[0] = 0;
    nodestate[1] = 0;
    nodestate[2] = 0;
    nodestate[3] = 0;
    nodestate[4] = 0;
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
    return nodestate; 
  }   

  public boolean setParamDefault(int param, int value) {
    if(param < 1 || param > 6){
      return false;
    }
    paramDefaults[param-1] = value;
    return true;
  }
  
} 
