package environment;


import app.TesseractMain;
import hardware.Rabbit;
import hardware.Teensy;
import processing.core.PApplet;


public class Stage {

    private PApplet p;

    //used to automatically define bounding box
    public int maxW;
    public int maxH;
    public int maxD;

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
      if (n.x > maxW) maxW = n.x;
      if (n.y > maxH) maxH = n.y;
      if (n.z > maxD) maxD = n.z;
    }
  }

    private void buildTesseractStage(){
        int counter = 0;


        PixelPlane plane = new PixelPlane(p);
        //nodes = plane.buildFullCube(counter,-175,-175, -175, 0 );

        _myMain.udpModel.rabbits = new Rabbit[6];

        //one rabbit per 9 tiles
        _myMain.udpModel.rabbits[0] = new Rabbit("192.168.1.100", 1, "mac_address");
        _myMain.udpModel.rabbits[1] = new Rabbit("192.168.1.101", 2, "mac_address");
        _myMain.udpModel.rabbits[2] = new Rabbit("192.168.1.102", 3, "mac_address");
        _myMain.udpModel.rabbits[3] = new Rabbit("192.168.1.103", 4, "mac_address");
        _myMain.udpModel.rabbits[4] = new Rabbit("192.168.1.104", 5, "mac_address");
        _myMain.udpModel.rabbits[5] = new Rabbit("192.168.1.105", 6, "mac_address");


        nodes = plane.buildPanel(_myMain.udpModel.rabbits[0], counter,0,-72,0, 0, false );

        Node[] planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[1], counter,(72*3),-72,0, 0, true );
        nodes = (Node[]) p.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[2], counter,(72*6),-72,0, 0, false );
        nodes = (Node[]) p.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[3], counter,-(72*3),-72,0, 0, false  );
        nodes = (Node[]) p.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[4], counter,-(72*6),-72,0, 0, false );
        nodes = (Node[]) p.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter,-(72*9),-72,0, 0, false );
        nodes = (Node[]) p.concat( nodes, planeNodes );

    }


    private void buildDracoStage() {

        int counter = 0;

        int h = 4; //number of teensies
        _myMain.udpModel.teensies = new Teensy[h];

        _myMain.udpModel.teensies[0] = new Teensy("192.168.1.200", 1, "mac_address");
        _myMain.udpModel.teensies[1] = new Teensy("192.168.1.201", 2, "mac_address");
        _myMain.udpModel.teensies[2] = new Teensy("192.168.1.202", 3, "mac_address");
        _myMain.udpModel.teensies[3] = new Teensy("192.168.1.203", 4, "mac_address");


        int w = 8; //number of pins per OCTO

        nodes = new Node[h*w];

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                Node dracoNode = new Node(30 * i, 40 * j, 0, counter, null);

                nodes[counter] = dracoNode;
                _myMain.udpModel.teensies[i].nodeArray[j] = dracoNode;

                counter++;
            }
        }
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
