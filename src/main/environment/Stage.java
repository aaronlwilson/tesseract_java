package environment;


import app.TesseractMain;
import hardware.Rabbit;
import hardware.Teensy;

public class Stage {

  //used to automatically define bounding box
  public int maxX;
  public int maxY;
  public int maxZ;

  public int minX;
  public int minY;
  public int minZ;

  public float maxW;
  public float maxH;
  public float maxD;

  //	An array of all the LEDs, used for render
  public Node[] nodes;
  public Node[] prevNodes; //last frames data

  private TesseractMain _myMain;


  public Stage() {
    _myMain = TesseractMain.getMain();
  }

  public void buildStage(String stageType) {
    if (stageType.equals("CUBOTRON")) {
      buildCubotron();

    } else if (stageType.equals("TESSERACT")) {
      buildTesseractStage();

    } else if (stageType.equals("DRACO")) {
      buildDracoStage();

    } else if (stageType.equals("SCARED")) {
      buildScared();

    } else {
      throw new RuntimeException("ERROR: Invalid stage of type: " + stageType);
    }

    //set the boundaries of the stage
    for (Node n : nodes) {
      if (n.x > maxX) maxX = n.x;
      if (n.y > maxY) maxY = n.y;
      if (n.z > maxZ) maxZ = n.z;

      if (n.x < minX) minX = n.x;
      if (n.y < minY) minY = n.y;
      if (n.z < minZ) minZ = n.z;
    }

    maxW = maxX + Math.abs(_myMain.stage.minX);
    maxH = maxY + Math.abs(_myMain.stage.minY);
    maxD = maxZ + Math.abs(_myMain.stage.minZ);

    TesseractMain.println("maxW: " + maxW);
    TesseractMain.println("maxH: " + maxH);
    TesseractMain.println("maxD: " + maxD);
  }

  private void buildTesseractStage() {
    int counter = 0;

    PixelPlane plane = new PixelPlane(_myMain);
    //nodes = plane.buildFullCube(counter,-175,-175, -175, 0 );

    _myMain.udpModel.rabbits = new Rabbit[6];

    //one rabbit per 9 tiles
    _myMain.udpModel.rabbits[0] = new Rabbit("192.168.1.100", 1, "mac_address");
    _myMain.udpModel.rabbits[1] = new Rabbit("192.168.1.105", 2, "mac_address");
    _myMain.udpModel.rabbits[2] = new Rabbit("192.168.1.104", 3, "mac_address");
    _myMain.udpModel.rabbits[3] = new Rabbit("192.168.1.102", 4, "mac_address");
    _myMain.udpModel.rabbits[4] = new Rabbit("192.168.1.101", 5, "mac_address");
    _myMain.udpModel.rabbits[5] = new Rabbit("192.168.1.103", 6, "mac_address");

    int startY = -72;

    nodes = plane.buildPanel(_myMain.udpModel.rabbits[0], counter, -(72 * 9), startY, 0, 0, false);

    Node[] planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[1], counter, -(72 * 6), startY, 0, 0, true);
    nodes = (Node[]) TesseractMain.concat(nodes, planeNodes);

    planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[2], counter, -(72 * 3), startY, 0, 0, false);
    nodes = (Node[]) TesseractMain.concat(nodes, planeNodes);

    planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[3], counter, 0, startY, 0, 0, false);
    nodes = (Node[]) TesseractMain.concat(nodes, planeNodes);

    planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[4], counter, (72 * 3), startY, 0, 0, false);
    nodes = (Node[]) TesseractMain.concat(nodes, planeNodes);

    planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, (72 * 6), startY, 0, 0, false);
    nodes = (Node[]) TesseractMain.concat(nodes, planeNodes);


  }


  private void buildDracoStage() {

    int h = 4; //number of teensies
    //int w = 8; //number of pins per OCTO

    _myMain.udpModel.teensies = new Teensy[h];

    _myMain.udpModel.teensies[0] = new Teensy("192.168.1.200", 1, "mac_address");
    _myMain.udpModel.teensies[1] = new Teensy("192.168.1.201", 2, "mac_address");
    _myMain.udpModel.teensies[2] = new Teensy("192.168.1.202", 3, "mac_address");
    _myMain.udpModel.teensies[3] = new Teensy("192.168.1.203", 4, "mac_address");


    //first call here is different than the others because it initializes the node array, the others concat to it.
    nodes = new StrandPanel().buildPanel(_myMain.udpModel.teensies[0], 1, "center_pillar_level_1_A", 0, -400, 0, 0, 0);

    Node[] panelNodes = new StrandPanel().buildPanel(_myMain.udpModel.teensies[0], 2, "center_pillar_level_1_B", nodes.length, -400, -100, 0, 0);
    nodes = (Node[]) TesseractMain.concat(nodes, panelNodes);

    panelNodes = new StrandPanel().buildPanel(_myMain.udpModel.teensies[0], 3, "center_pillar_level_1_A", nodes.length, 200, 0, 0, 0);
    nodes = (Node[]) TesseractMain.concat(nodes, panelNodes);

    panelNodes = new StrandPanel().buildPanel(_myMain.udpModel.teensies[0], 4, "center_pillar_level_1_B", nodes.length, 200, -100, 0, 0);
    nodes = (Node[]) TesseractMain.concat(nodes, panelNodes);


    panelNodes = new StrandPanel().buildPanel(_myMain.udpModel.teensies[0], 4, "talon_bottom", nodes.length, 0, 0, 0, 0);
    nodes = (Node[]) TesseractMain.concat(nodes, panelNodes);


    panelNodes = new StrandPanel().buildPanel(_myMain.udpModel.teensies[0], 4, "talon_top", nodes.length, 0, -100, 0, 0);
    nodes = (Node[]) TesseractMain.concat(nodes, panelNodes);


    panelNodes = new StrandPanel().buildPanel(_myMain.udpModel.teensies[0], 4, "center_pillar_level_2", nodes.length, -100, 0, 0, 0);
    nodes = (Node[]) TesseractMain.concat(nodes, panelNodes);


    panelNodes = new StrandPanel().buildPanel(_myMain.udpModel.teensies[0], 4, "center_pillar_level_3", nodes.length, -100, -100, 0, 0);
    nodes = (Node[]) TesseractMain.concat(nodes, panelNodes);

  }

  private void buildCubotron() {

    int counter = 0;
    nodes = new Node[30 * 30 * 30];

    // Initialize a crap-ton of nodes, just a big basic cubeotron
    for (int i = 0; i < 30; i++) {
      for (int j = 0; j < 30; j++) {
        for (int k = 0; k < 30; k++) {
          nodes[counter] = new Node(10 * i, 10 * j, 10 * k, counter, null);
          counter++;
        }
      }
    }

  }

  private void buildScared() {

    _myMain.udpModel.teensies = new Teensy[1];

    _myMain.udpModel.teensies[0] = new Teensy("192.168.50.203", 1, "mac_address");

    nodes = new Node[0];

    int numLedsPerStrip = 10;

    for (int i = 0; i < 6; i++) {
      Node[] stripNodes = new Node[numLedsPerStrip];

      Strip strip = new Strip(1, numLedsPerStrip, i);
      strip.setMyController(_myMain.udpModel.teensies[0]);

      //make some nodes in x y z space
      for (int j = 0; j < numLedsPerStrip; j++) {
        stripNodes[j] = new Node(2*j, 10 + (i*20), 10, j, strip);
      }

      strip.addNodes(stripNodes);

      nodes = (Node[]) TesseractMain.concat(nodes, stripNodes);
    }

  }


}
