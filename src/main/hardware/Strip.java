package hardware;

import environment.Node;

public class Strip extends Fixture {

  //constructor
  public Strip(int theId, int theLedCount, int thePinNum) {

    super(theId);

    pinNum = thePinNum;

    //not really needed, the nodes are added to the global nodeArray
    nodeArray = new Node[theLedCount];
  }
}