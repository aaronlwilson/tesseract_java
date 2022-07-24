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
          buildTesseractStageCube();
          //buildTesseractStage();

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

        //PixelPlane plane = new PixelPlane(_myMain);
        //nodes = plane.buildFullCube(counter,-175,-175, -175, 0 );

        _myMain.udpModel.rabbits = new Rabbit[6];

        //one rabbit per 9 tiles
        _myMain.udpModel.rabbits[0] = new Rabbit("192.168.50.100", 1, "00-90-C2-F1-30-1E", 0xffcc11); //Grey-Yellow -corner notch
        _myMain.udpModel.rabbits[1] = new Rabbit("192.168.50.101", 2, "00-90-C2-F1-2F-EE", 0x00ff00); //Green -corner notch
        _myMain.udpModel.rabbits[2] = new Rabbit("192.168.50.102", 3, "00-90-C2-FA-59-2C", 0xffffff); //White -corner notch
        _myMain.udpModel.rabbits[3] = new Rabbit("192.168.50.103", 4, "00-90-C2-FA-58-ED", 0xff00ff); //Purple
        _myMain.udpModel.rabbits[4] = new Rabbit("192.168.50.104", 5, "00-90-C2-F1-2F-7D", 0xff0000); //Red
        _myMain.udpModel.rabbits[5] = new Rabbit("192.168.50.105", 6, "00-90-C2-FA-58-FF", 0x0000ff); //Blue

        int startY = -72;

        PixelPlane plane = new PixelPlane(_myMain);
        nodes = plane.buildPanel(_myMain.udpModel.rabbits[0], counter,-(72*9),startY,0, 0, 0,false,false,false );

        plane = new PixelPlane(_myMain);
        Node[] planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[1], counter,-(72*6),startY,0, 1, 0,false,false,false );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[2], counter,-(72*3),startY,0, 0,0,false,false, false );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[3], counter,0,startY,0, 0,0,false,false, false  );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[4], counter,(72*3),startY,0, 0, 0,false,false,false );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter,(72*6),startY,0, 0, 0,false,false,true );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

    }

    private void buildTesseractStageCube() {
        int counter = 0;

        _myMain.udpModel.rabbits = new Rabbit[6];

        //one rabbit per 9 tiles
        _myMain.udpModel.rabbits[0] = new Rabbit("192.168.50.103", 1, "",0xff00ff); //Purple -up
        _myMain.udpModel.rabbits[1] = new Rabbit("192.168.50.151", 2, "",0xffcc11); //Grey-Yellow
        _myMain.udpModel.rabbits[2] = new Rabbit("192.168.50.104", 3, "",0xff0000); //Red -up
        _myMain.udpModel.rabbits[3] = new Rabbit("192.168.50.196", 4, "",0x00ff00); //Green
        _myMain.udpModel.rabbits[4] = new Rabbit("192.168.50.105", 5, "",0x0000ff); //Blue -up
        _myMain.udpModel.rabbits[5] = new Rabbit("192.168.50.102", 6, "",0xffffff); //White

        // old order 103, 100, 102, 104, 101, 105 - does not mean shit, those mappings were lost on the old router
        // new order 100, 101, 102, 103, 104, 105 ?
        int ctr = 108;

        //front //purple //needs hack //up
        PixelPlane plane = new PixelPlane(_myMain);
        nodes = plane.buildPanel(_myMain.udpModel.rabbits[0], counter, -ctr, -ctr, ctr, 1, 0, true, true, false);

        //back //gray-yellow
        plane = new PixelPlane(_myMain);
        Node[] planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[1], counter, -ctr, -ctr, -ctr, 3, 0, false, true, false);
        nodes = (Node[]) _myMain.concat(nodes, planeNodes);

        //top //red //up
        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[2], counter, -ctr, -ctr, -ctr,0,1,true,true,false );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        //bottom //green
        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[3], counter, -ctr, -ctr, ctr,1,1,true,false,false);
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        //left //blue //up
        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[4], counter, -ctr, -ctr, -ctr,0,2,false,false,true );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

        //right //white
        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr,0,2,false,true,false );
        nodes = (Node[]) _myMain.concat( nodes, planeNodes );

    }


    private void buildDracoStage() {

        nodes = new Node[0];

        int h = 5; //number of teensies

        _myMain.udpModel.teensies = new Teensy[h];

        _myMain.udpModel.teensies[0] = new Teensy("192.168.0.200", 1, "mac_address");
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

    private Node[] buildSmallTalon(Teensy teensy, int startX, int startY, int startZ){

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

    private Node[] buildCenterTower(Teensy teensy, int startX, int startY, int startZ){

        Node[] towerNodes = new Node[0];

        Node[] panelNodes = new StrandPanel().buildPanel(teensy, 1, "center_pillar_all", towerNodes.length, startX-130, startY, startZ, 0);
        towerNodes = (Node[]) _myMain.concat( towerNodes, panelNodes );

        panelNodes = new StrandPanel().buildPanel(teensy, 2, "center_pillar_all", towerNodes.length, startX, startY, startZ, 0);
        towerNodes = (Node[]) _myMain.concat( towerNodes, panelNodes );


        panelNodes = new StrandPanel().buildPanel(teensy, 3, "center_pillar_all", towerNodes.length, startX+130, startY, startZ, 0);
        towerNodes = (Node[]) _myMain.concat( towerNodes, panelNodes );

        panelNodes = new StrandPanel().buildPanel(teensy, 4, "center_pillar_all", towerNodes.length, startX+260, startY, startZ, 0);
        towerNodes = (Node[]) _myMain.concat( towerNodes, panelNodes );


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
