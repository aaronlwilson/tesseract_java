
package hardware;

import environment.Node;

public class Fixture {

  public int id;
  public int pinNum;

  public Node[] nodeArray;
  public Controller myController;

  //CONSTRUCTOR
  public Fixture(int theId) {

    id = theId;
    nodeArray = new Node[0];
  }


  public Controller getMyController() {
    return myController;
  }

  public void setMyController(Controller myController) {
    this.myController = myController;

    //for each teensy, we loop over all the attached Strands to send UDP, so give it a reference to this one
    myController.addFixture(this);
  }

  public void addNodesToFixture(Node[] newNodes) {

    int fal = nodeArray.length;    //determines length of firstArray
    int sal = newNodes.length;   //determines length of secondArray
    environment.Node[] resultArray = new environment.Node[fal + sal];  //resultant array of size first array and second array
    System.arraycopy(nodeArray, 0, resultArray, 0, fal);
    System.arraycopy(newNodes, 0, resultArray, fal, sal);

    nodeArray = resultArray;
  }

  public void addNodeToFixture(Node newNode) {

    environment.Node[] resultArray = new environment.Node[nodeArray.length + 1];
    resultArray[resultArray.length - 1] = newNode;

    nodeArray = resultArray;
  }
}

