package environment;

import hardware.*;

public class Node  {
  // World-space coordinates. Float (not int) so geometry built from trig/Sketchup
  // mappings (e.g. the SCARED spiral) keeps sub-unit precision.
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

  //constructor (int coords widen to float; keeps existing int-literal call sites working)
  public Node(float theX, float theY, float theZ, int theIndex, Fixture theFixture) {
    x = theX;
    y = theY;
    z = theZ;

    index = theIndex;
    fixture = theFixture;
  }

  // Default constructor for temporary nodes
  public Node() {
    x = 0;
    y = 0;
    z = 0;
    index = 0;
    fixture = null;
  }

}
