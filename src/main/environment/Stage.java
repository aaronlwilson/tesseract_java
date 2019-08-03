package environment;


import app.TesseractMain;
import hardware.Rabbit;
import hardware.Teensy;
import processing.core.PApplet;


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

    _myMain.println("maxW: " + maxW);
    _myMain.println("maxH: " + maxH);
    _myMain.println("maxD: " + maxD);
  }

    private void buildTesseractStage() {
        int counter = 0;


        PixelPlane plane = new PixelPlane(_myMain);
        //nodes = plane.buildFullCube(counter,-175,-175, -175, 0 );

        _myMain.udpModel.rabbits = new Rabbit[6];

        //one rabbit per 9 tiles
        _myMain.udpModel.rabbits[0] = new Rabbit("192.168.1.101", 1, "mac_address");
        _myMain.udpModel.rabbits[1] = new Rabbit("192.168.1.100", 2, "mac_address");
        _myMain.udpModel.rabbits[2] = new Rabbit("192.168.1.102", 3, "mac_address");
        _myMain.udpModel.rabbits[3] = new Rabbit("192.168.1.103", 4, "mac_address");
        _myMain.udpModel.rabbits[4] = new Rabbit("192.168.1.105", 5, "mac_address");
        _myMain.udpModel.rabbits[5] = new Rabbit("192.168.1.104", 6, "mac_address");


        nodes = plane.buildPanel(_myMain.udpModel.rabbits[0], counter,0,-72,0, 0, false );

        Node[] planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[1], counter,(72*3),-72,0, 0, false );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[2], counter,(72*6),-72,0, 0, false );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[3], counter,-(72*3),-72,0, 0, false  );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[4], counter,-(72*6),-72,0, 0, true );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter,-(72*9),-72,0, 0, false );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

    }


    private void buildDracoStage() {

        int counter = 0;
        int h = 4; //number of teensies
        int w = 8; //number of pins per OCTO

        _myMain.udpModel.teensies = new Teensy[h];
        _myMain.udpModel.teensies[0] = new Teensy("192.168.1.200", 1, "mac_address");
        _myMain.udpModel.teensies[1] = new Teensy("192.168.1.201", 2, "mac_address");
        _myMain.udpModel.teensies[2] = new Teensy("192.168.1.202", 3, "mac_address");
        _myMain.udpModel.teensies[3] = new Teensy("192.168.1.203", 4, "mac_address");

        /*//OLD APOGAEA setup
        nodes = new Node[h*w];

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                Node dracoNode = new Node(30 * i, 40 * j, 0, counter, null);
                nodes[counter] = dracoNode;
                _myMain.udpModel.teensies[i].nodeArray[j] = dracoNode;
                counter++;
            }
        }
        */

        StrandPanel panel = new StrandPanel();

        nodes = panel.buildPanel(_myMain.udpModel.teensies[0], 1, "center_pillar_level_1_A", counter, 0, 0, 0, 0);
        counter = nodes.length;

        Node[] panelNodes = panel.buildPanel(_myMain.udpModel.teensies[0], 2, "center_pillar_level_1_B", counter, -150, 0, 0, 0);
        nodes = (Node[]) _myMain.concat( nodes, panelNodes );
        counter = nodes.length;

        panelNodes = panel.buildPanel(_myMain.udpModel.teensies[0], 3, "center_pillar_level_1_A", counter, 100, 0, 0, 0);
        nodes = (Node[]) _myMain.concat( nodes, panelNodes );
        counter = nodes.length;

        panelNodes = panel.buildPanel(_myMain.udpModel.teensies[0], 4, "center_pillar_level_1_B", counter, 200, 0, 0, 0);
        nodes = (Node[]) _myMain.concat( nodes, panelNodes );

        panelNodes = panel.buildPanel(_myMain.udpModel.teensies[0], 4, "talon_bottom", counter, 300, 0, 0, 0);
        nodes = (Node[]) _myMain.concat( nodes, panelNodes );

        panelNodes = panel.buildPanel(_myMain.udpModel.teensies[0], 4, "talon_top", counter, 300, 50, 0, 0);
        nodes = (Node[]) _myMain.concat( nodes, panelNodes );
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
