package environment;


import app.TesseractMain;
import hardware.Rabbit;
import hardware.StrandPanel;
import hardware.Teensy;
import processing.core.PApplet;


public class Stage {

    private PApplet p;

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



    public Stage(PApplet pApplet) {
        p = pApplet;
        _myMain = TesseractMain.getMain();
    }

  public void buildStage(String stageType) {
    if (stageType.equals("CUBOTRON")) {
      buildCubotron();
    } else if (stageType.equals("TESSERACT")) {
      buildTesseractStage();
    } else if (stageType.equals("DRACO")) {
      buildDracoStage();
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

    p.println("maxW: " + maxW);
    p.println("maxH: " + maxH);
    p.println("maxD: " + maxD);
  }

    private void buildTesseractStage() {
        int counter = 0;


        PixelPlane plane = new PixelPlane(p);
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

        nodes = plane.buildPanel(_myMain.udpModel.rabbits[0], counter,-(72*9),startY,0, 0, false );

        Node[] planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[1], counter,-(72*6),startY,0, 0, true );
        nodes = (Node[]) p.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[2], counter,-(72*3),startY,0, 0, false );
        nodes = (Node[]) p.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[3], counter,0,startY,0, 0, false  );
        nodes = (Node[]) p.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[4], counter,(72*3),startY,0, 0, false );
        nodes = (Node[]) p.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter,(72*6),startY,0, 0, false );
        nodes = (Node[]) p.concat( nodes, planeNodes );

    }


    private void buildDracoStage() {

        int counter = 0;
        int h = 4; //number of teensies
        int w = 8; //number of pins per OCTO

        _myMain.udpModel.teensies = new Teensy[h];
        Teensy teensyOne = new Teensy("192.168.1.200", 1, "mac_address");
        _myMain.udpModel.teensies[0] = teensyOne;

        _myMain.udpModel.teensies[1] = new Teensy("192.168.1.201", 2, "mac_address");
        _myMain.udpModel.teensies[2] = new Teensy("192.168.1.202", 3, "mac_address");
        _myMain.udpModel.teensies[3] = new Teensy("192.168.1.203", 4, "mac_address");


        StrandPanel panelOne = new StrandPanel(p);
        nodes = panelOne.buildPanel(teensyOne, 1, 1, counter, 0, 0, 0, 0);
        teensyOne.addStrandPanel(panelOne);
        counter = nodes.length;

        StrandPanel panelTwo = new StrandPanel(p);
        Node[] panelNodes = panelTwo.buildPanel(teensyOne, 2, 2, counter, -150, 0, 0, 0);
        nodes = (Node[]) p.concat( nodes, panelNodes );
        teensyOne.addStrandPanel(panelTwo);
        counter = nodes.length;

        StrandPanel panelThree = new StrandPanel(p);
        panelNodes = panelThree.buildPanel(teensyOne, 5, 1, counter, 100, 0, 0, 0);
        nodes = (Node[]) p.concat( nodes, panelNodes );
        teensyOne.addStrandPanel(panelThree);
        counter = nodes.length;

        StrandPanel panelFour = new StrandPanel(p);
        panelNodes = panelFour.buildPanel(teensyOne, 6, 2, counter, 200, 0, 0, 0);
        nodes = (Node[]) p.concat( nodes, panelNodes );
        teensyOne.addStrandPanel(panelFour);

    }

    private void buildCubotron() {

        int counter = 0;
        nodes = new Node[30*30*30];

        // Initialize a crap-ton of nodes, just a big basic cubeotron
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                for (int k = 0; k < 30; k++) {
                    nodes[counter] = new Node(10*i, 10*j, 10*k, counter, null);
                    counter++;
                }
            }
        }

    }
}
