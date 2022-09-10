package environment;


import app.TesseractMain;
import hardware.*;
import processing.core.PApplet;

import static java.lang.Math.*;
import static processing.core.PApplet.radians;

public class Stage {

    //used to automatically define bounding box
    public float maxX;
    public float maxY;
    public float maxZ;

    public float minX;
    public float minY;
    public float minZ;

    public float maxW;
    public float maxH;
    public float maxD;

    //	An array of all the LEDs, used for render
    public Node[] nodes;
    public Node[] prevNodes; //last frames data

    public String stageType;

    private TesseractMain _myMain;


    public Stage() {
        _myMain = TesseractMain.getMain();
        nodes = new Node[]{};
    }

    public void buildStage(String stageType) {
        this.stageType = stageType;

        if (stageType.equals("CUBOTRON")) {
            buildCubotron();
        } else if (stageType.equals("TESSERACT")) {
            buildTesseractStageCube();
        } else if (stageType.equals("TESSERACT_WALL")) {
            buildTesseractWall();
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

        _myMain.println("maxW: " + maxW);
        _myMain.println("maxH: " + maxH);
        _myMain.println("maxD: " + maxD);
    }

    private void buildTesseractWall() {
        int counter = 0;

        _myMain.udpModel.pixelPushers = new PixelPusher[1];
        PixelPusher pixelPusher = new PixelPusher("192.168.50.119", 1, "d8:80:39:66:49:7b", 0xff0011);
        _myMain.udpModel.pixelPushers[0] = pixelPusher;

        int gap = Tile.xSpacing * 12; //spacing 6 x 12 nodes
        int startY = -gap;

        PixelPlane plane = new PixelPlane(_myMain);
        nodes = plane.buildPanelAPA(pixelPusher, counter, 0, 0, 0, 1, 0, true, true);

       // nodes = (Node[]) _myMain.concat(nodes, tileNodes);
    }

    private void buildTesseractWallOLD() {
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

        int gap = Tile.xSpacing * 12; //spacing 6 x 12 nodes
        int startY = -gap;

        PixelPlane plane = new PixelPlane(_myMain);
        nodes = plane.buildPanel(_myMain.udpModel.rabbits[0], counter, -(gap * 9), startY, 0, 0, 0, false, false, false);

        plane = new PixelPlane(_myMain);
        Node[] planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[1], counter, -(gap * 6), startY, 0, 1, 0, false, false, false);
        nodes = (Node[]) _myMain.concat(nodes, planeNodes);

        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[2], counter, -(gap * 3), startY, 0, 0, 0, false, false, false);
        nodes = (Node[]) _myMain.concat(nodes, planeNodes);

        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[3], counter, 0, startY, 0, 0, 0, false, false, false);
        nodes = (Node[]) _myMain.concat(nodes, planeNodes);

        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[4], counter, (gap * 3), startY, 0, 0, 0, false, false, false);
        nodes = (Node[]) _myMain.concat(nodes, planeNodes);

        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, (gap * 6), startY, 0, 0, 0, false, false, true);
        nodes = (Node[]) _myMain.concat(nodes, planeNodes);
    }

    private void buildTesseractStageCube() {
        int counter = 0;

        _myMain.udpModel.rabbits = new Rabbit[6];

        //one rabbit per 9 tiles
        _myMain.udpModel.rabbits[0] = new Rabbit("192.168.50.104", 1, "", 0x00ff00); //Green -up
        _myMain.udpModel.rabbits[1] = new Rabbit("192.168.50.151", 2, "", 0xffcc11); //Grey-Yellow
        _myMain.udpModel.rabbits[2] = new Rabbit("192.168.50.105", 3, "", 0xff0000); //Red -up
        _myMain.udpModel.rabbits[3] = new Rabbit("192.168.50.196", 4, "", 0xff00ff); //Purple
        _myMain.udpModel.rabbits[4] = new Rabbit("192.168.50.103", 5, "", 0x0000ff); //Blue -up
        _myMain.udpModel.rabbits[5] = new Rabbit("192.168.50.102", 6, "", 0xffffff); //White

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
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[2], counter, -ctr, -ctr, -ctr, 0, 1, true, true, true);
        nodes = (Node[]) _myMain.concat(nodes, planeNodes);

        //bottom //green
        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[3], counter, -ctr, -ctr, ctr, 1, 1, true, false, false);
        nodes = (Node[]) _myMain.concat(nodes, planeNodes);

        //left //blue //up
        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[4], counter, -ctr, -ctr, -ctr, 0, 2, false, false, false);
        nodes = (Node[]) _myMain.concat(nodes, planeNodes);

        //right //white
        plane = new PixelPlane(_myMain);
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 0, 2, false, true, false);
        nodes = (Node[]) _myMain.concat(nodes, planeNodes);

    }

    private void buildScared() {
        int numberTeensies = 1;
        _myMain.udpModel.teensies = new Teensy[numberTeensies];

        //Teensy 4.1
        _myMain.udpModel.teensies[0] = new Teensy("192.168.0.101", 1, "mac_address");
//        _myMain.udpModel.teensies[1] = new Teensy("192.168.0.102", 2, "mac_address");
//        _myMain.udpModel.teensies[2] = new Teensy("192.168.0.103", 3, "mac_address");
//        _myMain.udpModel.teensies[3] = new Teensy("192.168.0.104", 4, "mac_address");

        //ESP8266
        //_myMain.udpModel.teensies[0] = new Teensy("192.168.50.101", 1, "mac_address");

        nodes = new Node[0];

        int numLedsPerStrip = 200;
        float startRadius = 20;
        float radius = startRadius;
        float startAngle = 0;
        double exponent = 2.5;
        int yHeight = 600;

        for (int k = 0; k < numberTeensies; k++) {
            //pins on the teensy are 1 through 8
            int pinz = 16; //gets decremented
            int numPins = pinz;

            for (int i = 0; i < numPins; i++) {
                Node[] stripNodes = new Node[numLedsPerStrip];

                Strip strip = new Strip(i, numLedsPerStrip, pinz);
                pinz--;
                strip.setMyController(_myMain.udpModel.teensies[k]);

                float x;  // node position
                float y;
                float z;

                //make some nodes in x y z space
                for (int j = 0; j < numLedsPerStrip; j++) {
                    //distribute 200 into half circle
                    float angle = PApplet.map(j, 0, 200, 0, 180) + startAngle;

                    if (i < (numPins/2)) { // half spiral clockwise, the other half - counter clockwise
                        z = (float) (radius * Math.cos(radians(angle)));
                        x = (float) (radius * Math.sin(radians(angle)));
                    } else {
                        // x and z are the circle part
                        x = (float) (radius * Math.cos(radians(angle)));
                        z = (float) (radius * Math.sin(radians(angle)));
                    }

                    //increase the radius as we move down, "christmas tree"
                    radius += 2;

                    // y is the height (which makes the circle to a spiral)
                    //y = PApplet.map(j, 0, numLedsPerStrip, -250, 250);
                    //y = (j*2)-200;
                    float percent = PApplet.map(j, 0, numLedsPerStrip, 0, 1);
                    y = (float) ((pow(percent, exponent) * yHeight) - (yHeight/2));

                    stripNodes[j] = new Node(x, z, y, j, strip);
                    //stripNodes[j] = new Node((3 * j) -300, (10 + (i * 10) + (k * 90)) -175, 10, j, strip);
                }

                //if (i == 0) {
                    startAngle += 360 / (numPins/2);
                //}

                radius = startRadius;

                strip.addNodesToFixture(stripNodes);
                nodes = (Node[]) TesseractMain.concat(nodes, stripNodes);
            }
        }

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


        Node[] talonNodes = buildSmallTalon(_myMain.udpModel.teensies[0], -450, 0, 300);
        nodes = (Node[]) _myMain.concat(nodes, talonNodes);

        talonNodes = buildSmallTalon(_myMain.udpModel.teensies[1], -150, 0, 300);
        nodes = (Node[]) _myMain.concat(nodes, talonNodes);

        talonNodes = buildSmallTalon(_myMain.udpModel.teensies[2], 150, 0, 300);
        nodes = (Node[]) _myMain.concat(nodes, talonNodes);

        talonNodes = buildSmallTalon(_myMain.udpModel.teensies[3], 450, 0, 300);
        nodes = (Node[]) _myMain.concat(nodes, talonNodes);

        //center tower
        Node[] towerNodes = buildCenterTower(_myMain.udpModel.teensies[4], 0, 0, 0);
        nodes = (Node[]) _myMain.concat(nodes, towerNodes);
    }

    private Node[] buildSmallTalon(Teensy teensy, int startX, int startY, int startZ) {

        Node[] talonNodes = new Node[0];

        Node[] panelNodes = new StrandPanel().buildPanel(teensy, 0, "talon_top_mirrored", talonNodes.length, startX - 100, startY - 100, startZ, 0);
        talonNodes = (Node[]) _myMain.concat(talonNodes, panelNodes);

        panelNodes = new StrandPanel().buildPanel(teensy, 2, "talon_bottom", talonNodes.length, startX - 140, startY, startZ, 0);
        talonNodes = (Node[]) _myMain.concat(talonNodes, panelNodes);

        panelNodes = new StrandPanel().buildPanel(teensy, 4, "talon_top", talonNodes.length, startX, startY - 100, startZ, 0);
        talonNodes = (Node[]) _myMain.concat(talonNodes, panelNodes);

        panelNodes = new StrandPanel().buildPanel(teensy, 6, "talon_bottom_mirrored", talonNodes.length, startX, startY, startZ, 0);
        talonNodes = (Node[]) _myMain.concat(talonNodes, panelNodes);

        return talonNodes;
    }

    private Node[] buildCenterTower(Teensy teensy, int startX, int startY, int startZ) {

        Node[] towerNodes = new Node[0];

        Node[] panelNodes = new StrandPanel().buildPanel(teensy, 1, "center_pillar_all", towerNodes.length, startX - 130, startY, startZ, 0);
        towerNodes = (Node[]) _myMain.concat(towerNodes, panelNodes);

        panelNodes = new StrandPanel().buildPanel(teensy, 2, "center_pillar_all", towerNodes.length, startX, startY, startZ, 0);
        towerNodes = (Node[]) _myMain.concat(towerNodes, panelNodes);


        panelNodes = new StrandPanel().buildPanel(teensy, 3, "center_pillar_all", towerNodes.length, startX + 130, startY, startZ, 0);
        towerNodes = (Node[]) _myMain.concat(towerNodes, panelNodes);

        panelNodes = new StrandPanel().buildPanel(teensy, 4, "center_pillar_all", towerNodes.length, startX + 260, startY, startZ, 0);
        towerNodes = (Node[]) _myMain.concat(towerNodes, panelNodes);


        return towerNodes;
    }

    private void buildCubotron() {
        // a 30 x 30 x 30 solid cube
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
}
