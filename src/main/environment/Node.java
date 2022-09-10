package environment;

import hardware.*;

public class Node  {
  public float x;
  public float y;
  public float z;

  public float screenX;
  public float screenY;


  public int index; //0-? usually relative to the fixture
  public int port;  //1-8 on pixel pusher, or 'pin' on Teensy
  public Fixture fixture;

  //public int nodeType;

  public int r;
  public int g;
  public int b;

  //constructor
  public Node(float theX, float theY, float theZ, int theIndex, Fixture theFixture) {
    x = theX;
    y = theY;
    z = theZ;

    index = theIndex;
    fixture = theFixture;
  }

}
