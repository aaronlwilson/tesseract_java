
package hardware;

import environment.Node;

public class Fixture {

  public int id;
  public int pinNum;

  public Node[] nodeArray;


  //CONSTRUCTOR
  public Fixture(int theId) {

    id = theId;
    nodeArray = new Node[0];

  }

  public void addNodes(Node[] newNodes) {

    int fal = nodeArray.length;    //determines length of firstArray
    int sal = newNodes.length;   //determines length of secondArray
    environment.Node[] resultArray = new environment.Node[fal + sal];  //resultant array of size first array and second array
    System.arraycopy(nodeArray, 0, resultArray, 0, fal);
    System.arraycopy(newNodes, 0, resultArray, fal, sal);

    nodeArray = resultArray;
  }

  public void addNode(Node newNode) {

    environment.Node[] resultArray = new environment.Node[nodeArray.length + 1];
    resultArray[resultArray.length - 1] = newNode;

    nodeArray = resultArray;
  }
}

