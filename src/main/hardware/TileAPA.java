package hardware;

import environment.Node;

public class TileAPA extends Fixture {

  public static int numLEDperTile = 144;

  public Node[][] tileNodeArray = new Node[12][12];

  public Controller myController;


  //constructor
  public TileAPA(int theId, int thePinNum) {

    super(theId);

    pinNum = thePinNum;

    //not really needed, the nodes are added to the global nodeArray
    //nodeArray = new Node[numLEDperTile];
  }

  public Controller getMyController() {
    return myController;
  }

  public void setMyController(Controller myController) {
    this.myController = myController;

    //for each teensy, we loop over all the attached Strands to send UDP, so give it a reference to this one
    myController.addFixture(this);
  }

  public Node[] zigZagNodes() {
    Node[] tileNodes = new Node[TileAPA.numLEDperTile];

    int n = 0;

    //make some nodes in x y z space
    for (int i = 0; i < 12; i++) {
      for (int j = 0; j < 12; j++) {
        tileNodes[n] = new Node(i * 6, j * 6, 100, n, this);
        n++;
      }
    }
    return tileNodes;
  }

}
