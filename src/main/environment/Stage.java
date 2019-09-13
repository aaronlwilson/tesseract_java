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
        nodes = new Node[]{};
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
        _myMain.udpModel.rabbits[0] = new Rabbit("192.168.0.100", 1, "mac_address");
        _myMain.udpModel.rabbits[1] = new Rabbit("192.168.0.105", 2, "mac_address");
        _myMain.udpModel.rabbits[2] = new Rabbit("192.168.0.104", 3, "mac_address");
        _myMain.udpModel.rabbits[3] = new Rabbit("192.168.0.102", 4, "mac_address");
        _myMain.udpModel.rabbits[4] = new Rabbit("192.168.0.101", 5, "mac_address");
        _myMain.udpModel.rabbits[5] = new Rabbit("192.168.0.103", 6, "mac_address");

        int startY = -72;

        nodes = plane.buildPanel(_myMain.udpModel.rabbits[0], counter,-(72*9),startY,0, 0, false );

        Node[] planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[1], counter,-(72*6),startY,0, 0, true );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[2], counter,-(72*3),startY,0, 0, false );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[3], counter,0,startY,0, 0, false  );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[4], counter,(72*3),startY,0, 0, false );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter,(72*6),startY,0, 0, false );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );


    }


    private void buildDracoStage() {

        nodes = new Node[0];

        int h = 5; //number of teensies

        _myMain.udpModel.teensies = new Teensy[h];

        _myMain.udpModel.teensies[0] = new Teensy("192.168.1.200", 1, "mac_address");
        _myMain.udpModel.teensies[1] = new Teensy("192.168.1.201", 2, "mac_address");
        _myMain.udpModel.teensies[2] = new Teensy("192.168.1.202", 3, "mac_address");
        _myMain.udpModel.teensies[3] = new Teensy("192.168.1.203", 4, "mac_address");
        //center tower
        _myMain.udpModel.teensies[4] = new Teensy("192.168.1.204", 5, "mac_address");


        //pins are 0 -orange, 2 -blue, 4 -orange, 6 -blue

        //test "head"
        //nodes = new StrandPanel().buildPanel(_myMain.udpModel.teensies[0], 0, "center_pillar_level_4", 0, 0, 0, 0, 0);

        Node[] talonNodes = buildSmallTalon(_myMain.udpModel.teensies[0], -450,0, 300);
        nodes = (Node[]) _myMain.concat( nodes, talonNodes );

        talonNodes = buildSmallTalon(_myMain.udpModel.teensies[1], -150,0 , 300);
        nodes = (Node[]) _myMain.concat( nodes, talonNodes );

        talonNodes = buildSmallTalon(_myMain.udpModel.teensies[2], 150,0, 300);
        nodes = (Node[]) _myMain.concat( nodes, talonNodes );

        talonNodes = buildSmallTalon(_myMain.udpModel.teensies[3], 450,0 , 300);
        nodes = (Node[]) _myMain.concat( nodes, talonNodes );


        //center tower
        Node[] towerNodes = buildCenterTower(_myMain.udpModel.teensies[4], 0,0 , 0);
        nodes = (Node[]) _myMain.concat( nodes, towerNodes );


    }

    public Node[] buildSmallTalon(Teensy teensy, int startX, int startY, int startZ){

        Node[] talonNodes = new Node[0];

        Node[] panelNodes = new StrandPanel().buildPanel(teensy, 0, "talon_top_mirrored", talonNodes.length, startX-100, startY-100, startZ, 0);
        talonNodes = (Node[]) _myMain.concat( talonNodes, panelNodes );

        panelNodes = new StrandPanel().buildPanel(teensy, 2, "talon_bottom", talonNodes.length, startX-140, startY, startZ, 0);
        talonNodes = (Node[]) _myMain.concat( talonNodes, panelNodes );

        panelNodes = new StrandPanel().buildPanel(teensy, 4, "talon_top", talonNodes.length, startX, startY-100, startZ, 0);
        talonNodes = (Node[]) _myMain.concat( talonNodes, panelNodes );

        panelNodes = new StrandPanel().buildPanel(teensy, 6, "talon_bottom_mirrored", talonNodes.length, startX, startY, startZ, 0);
        talonNodes = (Node[]) _myMain.concat( talonNodes, panelNodes );

        return talonNodes;
    }

    public Node[] buildCenterTower(Teensy teensy, int startX, int startY, int startZ){

        Node[] towerNodes = new Node[0];

        Node[] panelNodes = new StrandPanel().buildPanel(teensy, 1, "center_pillar_all", towerNodes.length, startX-130, startY, startZ, 0);
        towerNodes = (Node[]) _myMain.concat( towerNodes, panelNodes );

        //panelNodes = new StrandPanel().buildPanel(teensy, 2, "center_pillar_all", towerNodes.length, startX, startY, startZ, 0);
        //towerNodes = (Node[]) _myMain.concat( towerNodes, panelNodes );

        /*
        panelNodes = new StrandPanel().buildPanel(teensy, 3, "center_pillar_all", towerNodes.length, startX+130, startY, startZ, 0);
        towerNodes = (Node[]) _myMain.concat( towerNodes, panelNodes );

        panelNodes = new StrandPanel().buildPanel(teensy, 4, "center_pillar_all", towerNodes.length, startX+260, startY, startZ, 0);
        towerNodes = (Node[]) _myMain.concat( towerNodes, panelNodes );
        */

        return  towerNodes;
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
