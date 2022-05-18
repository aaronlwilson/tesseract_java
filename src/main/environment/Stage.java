package environment;


import app.TesseractMain;
import hardware.Controllers.Rabbit;
import hardware.Fixtures.Strip;
import hardware.Controllers.Teensy;

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

    } else if (stageType.equals("AJUBATUBES")) {
      buildAjUbaTubesSmall();

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

    _myMain.udpModel.rabbits = new Rabbit[6];

    //one rabbit per 9 tiles
    //Apollo Central Router has reserved IP addresses for each mac address
    _myMain.udpModel.rabbits[0] = new Rabbit("192.168.50.100", 1, "00-90-C2-F1-30-1E");
    _myMain.udpModel.rabbits[1] = new Rabbit("192.168.50.101", 2, "00-90-C2-F1-2F-EE");
    _myMain.udpModel.rabbits[2] = new Rabbit("192.168.50.102", 3, "00-90-C2-FA-59-2C");
    _myMain.udpModel.rabbits[3] = new Rabbit("192.168.50.103", 4, "00-90-C2-FA-58-ED");
    _myMain.udpModel.rabbits[4] = new Rabbit("192.168.50.104", 5, "00-90-C2-F1-2F-7D");
    _myMain.udpModel.rabbits[5] = new Rabbit("192.168.50.105", 6, "00-90-C2-FA-58-FF");

    //nodes = plane.buildFullCube(counter,-175,-175, -175, 0 );


    //The following lays out the panels as a big 1x6 screen, like in my basement
    int startY = -72;

    nodes = plane.buildPanel(_myMain.udpModel.rabbits[0], counter, -(72 * 9), startY, 0, 0, false);

    Node[] planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[1], counter, -(72 * 6), startY, 0, 0, false);
    nodes = (Node[]) TesseractMain.concat(nodes, planeNodes);

    planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[2], counter, -(72 * 3), startY, 0, 0, false);
    nodes = (Node[]) TesseractMain.concat(nodes, planeNodes);

    planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[3], counter, 0, startY, 0, 0, false);
    nodes = (Node[]) TesseractMain.concat(nodes, planeNodes);

    planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[4], counter, (72 * 3), startY, 0, 0, false);
    nodes = (Node[]) TesseractMain.concat(nodes, planeNodes);

    planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, (72 * 6), startY, 0, 0, true);
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

    //Teensy 4.1
    //_myMain.udpModel.teensies[0] = new Teensy("192.168.50.203", 1, "mac_address");

    //ESP8266
    _myMain.udpModel.teensies[0] = new Teensy("192.168.50.101", 1, "mac_address");

    nodes = new Node[0];

    //with 8 pins of data, the Teensy could not handle 200 nodes per strip. Even over-clocked
    //800 pixels per teensy 3.2 is the current max. That should be higher...
    int numLedsPerStrip = 214;

    //pins on the teensy are 1 through 8
    int pinz = 8; //gets decremented
    int numPins = pinz;

    for (int i = 0; i < numPins; i++) {
      Node[] stripNodes = new Node[numLedsPerStrip];

      Strip strip = new Strip(i, numLedsPerStrip, pinz);
      pinz--;
      strip.setMyController(_myMain.udpModel.teensies[0]);

      //make some nodes in x y z space
      for (int j = 0; j < numLedsPerStrip; j++) {
        stripNodes[j] = new Node(3*j, 10 + (i*10), 10, j, strip);
      }

      strip.addNodes(stripNodes);

      nodes = (Node[]) TesseractMain.concat(nodes, stripNodes);
    }

  }

  private void buildAjUbaTubesSmall() {
    int numtubes = 16;
    int [][] tubelocation = {{123, 203}, {165, 193}, {84, 185}, {191, 160}, {119, 166}, {58, 149}, {202, 115}, {159, 155}, {60, 103}, {91, 131}, {161, 112}, {91, 69}, {175, 77}, {116, 96}, {134, 59}, {128, 132}};
    //End to End outside tube lengths of Glass tubes in meters
    double [] tubelengths = {495.3,546.1,571.5,650.875,698.5,666.75,701.675,765.175,758.825,841.375,901.7,952.5,1009.65,1022.35,1060.45,1165.225};


  }


}
