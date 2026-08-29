package environment;


import app.TesseractApp;
import hardware.*;

import static render.ProcessingCompat.map;
import static render.ProcessingCompat.radians;

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

    private TesseractApp _myMain;


    public Stage() {
        _myMain = TesseractApp.get();
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

        maxW = maxX + Math.abs(minX);
        maxH = maxY + Math.abs(minY);
        maxD = maxZ + Math.abs(minZ);

        System.out.println("maxW: " + maxW);
        System.out.println("maxH: " + maxH);
        System.out.println("maxD: " + maxD);
    }

    private void buildTesseractWall() {
        int counter = 0;

        _myMain.udpModel.pixelPushers = new PixelPusher[1];
        PixelPusher pixelPusher = new PixelPusher("192.168.50.119", 1, "d8:80:39:66:49:7b", 0xff0011);
        _myMain.udpModel.pixelPushers[0] = pixelPusher;

        int gap = Tile.xSpacing * 12; //spacing 6 x 12 nodes
        int startY = -gap;

        PixelPlane plane = new PixelPlane();
        nodes = plane.buildPanelAPA(pixelPusher, counter, 0, 0, 0, 1, 0, true, true);

       // nodes = concatNodes(nodes, tileNodes);
    }

    private void buildTesseractWallOLD() {
        int counter = 0;

        //PixelPlane plane = new PixelPlane();
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

        PixelPlane plane = new PixelPlane();
        nodes = plane.buildPanel(_myMain.udpModel.rabbits[0], counter, -(gap * 9), startY, 0, 0, 0, false, false, false);

        plane = new PixelPlane();
        Node[] planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[1], counter, -(gap * 6), startY, 0, 1, 0, false, false, false);
        nodes = concatNodes(nodes, planeNodes);

        plane = new PixelPlane();
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[2], counter, -(gap * 3), startY, 0, 0, 0, false, false, false);
        nodes = concatNodes(nodes, planeNodes);

        plane = new PixelPlane();
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[3], counter, 0, startY, 0, 0, 0, false, false, false);
        nodes = concatNodes(nodes, planeNodes);

        plane = new PixelPlane();
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[4], counter, (gap * 3), startY, 0, 0, 0, false, false, false);
        nodes = concatNodes(nodes, planeNodes);

        plane = new PixelPlane();
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, (gap * 6), startY, 0, 0, 0, false, false, true);
        nodes = concatNodes(nodes, planeNodes);
    }

    private void buildTesseractStageCube() {
        int counter = 0;
        _myMain.udpModel.rabbits = new Rabbit[6];

/* DAS ENERGY 2022 for reference
        _myMain.udpModel.rabbits[0] = new Rabbit("192.168.50.104", 1, "", 0x00ff00); //Green -up
        _myMain.udpModel.rabbits[1] = new Rabbit("192.168.50.151", 2, "", 0xffcc11); //Grey-Yellow
        _myMain.udpModel.rabbits[2] = new Rabbit("192.168.50.105", 3, "", 0xff0000); //Red -up
        _myMain.udpModel.rabbits[3] = new Rabbit("192.168.50.196", 4, "", 0xff00ff); //Purple
        _myMain.udpModel.rabbits[4] = new Rabbit("192.168.50.103", 5, "", 0x0000ff); //Blue -up
        _myMain.udpModel.rabbits[5] = new Rabbit("192.168.50.102", 6, "", 0xffffff); //White
*/

//one rabbit per 9 tiles
//        _myMain.udpModel.rabbits[0] = new Rabbit("192.168.50.196", 1, "00-90-C2-F1-30-1E", 0xffffff); //White :1 DOWN
//        _myMain.udpModel.rabbits[1] = new Rabbit("192.168.50.206", 6, "00-90-C2-FA-58-FF", 0x00ff00); //Green :6
//        _myMain.udpModel.rabbits[2] = new Rabbit("192.168.50.205", 5, "00-90-C2-F1-2F-7D", 0x0000ff); //Blue :5 UP
//        _myMain.udpModel.rabbits[3] = new Rabbit("192.168.50.203", 3, "00-90-C2-FA-59-2C", 0xff0000); //Red :3
//        _myMain.udpModel.rabbits[4] = new Rabbit("192.168.50.204", 4, "00-90-C2-FA-58-ED", 0xff00ff); //Purple :4 UP
//        _myMain.udpModel.rabbits[5] = new Rabbit("192.168.50.151", 2, "00-90-C2-F1-2F-EE", 0x442206); //Brown:2

        _myMain.udpModel.rabbits[0] = new Rabbit("192.168.50.204", 4, "00-90-C2-FA-58-ED", 0xff00ff); //Purple :4 UP
        _myMain.udpModel.rabbits[1] = new Rabbit("192.168.50.203", 3, "00-90-C2-FA-59-2C", 0x111111); //gray :3

        _myMain.udpModel.rabbits[2] = new Rabbit("192.168.50.205", 5, "00-90-C2-F1-2F-7D", 0x0000ff); //Blue :5 UP
        _myMain.udpModel.rabbits[3] = new Rabbit("192.168.50.196", 1, "00-90-C2-F1-30-1E", 0xffffff); //White :1

        _myMain.udpModel.rabbits[4] = new Rabbit("192.168.50.206", 6, "00-90-C2-FA-58-FF", 0x00ff00); //Green :6 UP
        _myMain.udpModel.rabbits[5] = new Rabbit("192.168.50.151", 2, "00-90-C2-F1-2F-EE", 0xff0000); //red:2


        int ctr = 112;

        //front //needs hack //up
        PixelPlane plane = new PixelPlane();
        nodes = plane.buildPanel(_myMain.udpModel.rabbits[0], counter, -ctr, -ctr, ctr, 2, 0, false, true, false);

        //back //needs channel swap
        plane = new PixelPlane();
        Node[] planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[1], counter, -ctr, -ctr, -ctr, 2, 0, false, false, false);
        nodes = concatNodes(nodes, planeNodes);

        //top  //up
        plane = new PixelPlane();
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[2], counter, -ctr, -ctr, -ctr, 3, 1, true, false, false);
        nodes = concatNodes(nodes, planeNodes);

        //bottom
        plane = new PixelPlane();
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[3], counter, -ctr, -ctr, ctr, 2, 1, true, true, false);
        nodes = concatNodes(nodes, planeNodes);

        //left //up
        plane = new PixelPlane();
        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[4], counter, -ctr, -ctr, -ctr, 3, 2, false, true, true);
        nodes = concatNodes(nodes, planeNodes);

        //right
        plane = new PixelPlane();
        // FESTIVAL HACK (Boulder Roots 2026): rabbits[5]'s panel (reused unit, previously "Brown")
        // doesn't match the tileId -> wiring-direction assumption the other 5 panels' tiles use.
        // Confirmed live on-site: every tile except 5 (center) needed its wiring-direction
        // overridden - backs up the theory that this specific panel has different internal wiring,
        // though (as it turns out) that doesn't matter since every tile is just a 12x12 square each
        // override can independently correct for. Each tileId below can be tuned without affecting
        // the other 5 (correctly-wired) panels; 5 is listed explicitly (same as its own default)
        // purely so every tile 1-9 has a visible entry here.
        java.util.Map<Integer, Integer> rightPanelTileRotationOverrides = java.util.Map.of(
            1, 0, 2, 0, 3, 0,
            4, 2, 5, 2, 6, 2,
            7, 0, 8, 0, 9, 0
        );
        planeNodes = plane.buildPanelWithTileRotationOverrides(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 3, 2, false, false, false, rightPanelTileRotationOverrides);
//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 0, 2, true, false, false);
//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 0, 2, false, true, false);
//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 0, 2, true, true, false);

//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 1, 2, false, false, false);
//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 1, 2, true, false, false);
//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 1, 2, false, true, false);
//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 1, 2, true, true, false);

//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 2, 2, false, false, false);
//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 2, 2, true, false, false);
//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 2, 2, false, true, false);
//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 2, 2, true, true, false);

//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 3, 2, false, false, false);
//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 3, 2, true, false, false);
//         planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 3, 2, false, true, false);

        // this was correct but outer panels flipped in wiring
        // planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter, -ctr, -ctr, ctr, 3, 2, true, true, false);

        nodes = concatNodes(nodes, planeNodes);

    }

    private void buildScared() {
        // SCARED = a "christmas-tree" spiral zome. Each of 4 Teensies drives 8 strips (octo board);
        // each tube carries 2 strips. Ported from the original Processing build (Grandma Millers mapping).
        // Set numberTeensies to 5 and uncomment teensies[4] below to add the Tesseract base.
        int numberTeensies = 4;
        _myMain.udpModel.teensies = new Teensy[numberTeensies];

        //Teensy 4.1
        _myMain.udpModel.teensies[0] = new Teensy("192.168.50.101", 1, "mac_address");
        _myMain.udpModel.teensies[1] = new Teensy("192.168.50.102", 2, "mac_address");
        _myMain.udpModel.teensies[2] = new Teensy("192.168.50.103", 3, "mac_address");
        _myMain.udpModel.teensies[3] = new Teensy("192.168.50.104", 4, "mac_address");
        //for Tesseract base (bump numberTeensies to 5 to enable)
        //_myMain.udpModel.teensies[4] = new Teensy("192.168.50.105", 5, "mac_address");

        nodes = new Node[0];
        int nodeIndex = 0;

        int numPins = 8;
        int numLedsPerStrip = 200;

        float startRadius = 20;
        float radius = startRadius;
        float startAngle = 0;
        double exponent = 2.5;
        int yHeight = 600;

        for (int k = 0; k < numberTeensies; k++) {
            //correct rotation when we flip directions (z axis is a quarter turn from x axis on the plane of rotation)
            if (k == 2) startAngle = 90;

            //pins on the teensy are 1 through 8
            int pinz = 1;

            for (int i = 0; i < numPins; i++) {
                Node[] stripNodes = new Node[numLedsPerStrip];

                Strip strip = new Strip(i, numLedsPerStrip, pinz);
                pinz++;
                strip.setMyController(_myMain.udpModel.teensies[k]);

                float x;  // node position
                float y;
                float z;

                //make some nodes in x y z space
                for (int j = 0; j < numLedsPerStrip; j++) {
                    //distribute 200 into 6/16th of a circle
                    float angle = map(j, 0, numLedsPerStrip, 0, 135) + startAngle;

                    if (k < 2) { // half spiral clockwise, the other half - counter clockwise
                        z = (float) (radius * Math.cos(radians(angle)));
                        x = (float) (radius * Math.sin(radians(angle)));
                    } else {
                        // x and z are the circle part
                        x = (float) (radius * Math.cos(radians(angle)));
                        z = (float) (radius * Math.sin(radians(angle)));
                    }

                    //increase the radius as we move down, "christmas tree"
                    radius += 2;

                    // y is the height (which makes the circle into a spiral)
                    float percent = map(j, 0, numLedsPerStrip, 0, 1);
                    y = (float) ((Math.pow(percent, exponent) * yHeight) - (yHeight / 2.0));

                    // each tube has 2 LED strips, so the second one is shown here with a lil more Z
                    if (i % 2 != 0) {
                        z -= 7;
                    }

                    //true Scared mapping
                    stripNodes[j] = new Node(x, z, y, nodeIndex, strip);
                    nodeIndex++;
                }

                if (i % 2 != 0) {
                    startAngle += 360 / 8; // based on an octagon
                }

                radius = startRadius;

                // NOTE: assign directly, not addNodesToFixture() — the latter doubled the array length (bug).
                strip.nodeArray = stripNodes;
                nodes = concatNodes(nodes, stripNodes);
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
        nodes = concatNodes(nodes, talonNodes);

        talonNodes = buildSmallTalon(_myMain.udpModel.teensies[1], -150, 0, 300);
        nodes = concatNodes(nodes, talonNodes);

        talonNodes = buildSmallTalon(_myMain.udpModel.teensies[2], 150, 0, 300);
        nodes = concatNodes(nodes, talonNodes);

        talonNodes = buildSmallTalon(_myMain.udpModel.teensies[3], 450, 0, 300);
        nodes = concatNodes(nodes, talonNodes);


        //center tower
        Node[] towerNodes = buildCenterTower(_myMain.udpModel.teensies[4], 0, 0, 0);
        nodes = concatNodes(nodes, towerNodes);


    }

    private Node[] buildSmallTalon(Teensy teensy, int startX, int startY, int startZ) {

        Node[] talonNodes = new Node[0];

        Node[] panelNodes = new StrandPanel().buildPanel(teensy, 0, "talon_top_mirrored", talonNodes.length, startX - 100, startY - 100, startZ, 0);
        talonNodes = concatNodes(talonNodes, panelNodes);

        panelNodes = new StrandPanel().buildPanel(teensy, 2, "talon_bottom", talonNodes.length, startX - 140, startY, startZ, 0);
        talonNodes = concatNodes(talonNodes, panelNodes);

        panelNodes = new StrandPanel().buildPanel(teensy, 4, "talon_top", talonNodes.length, startX, startY - 100, startZ, 0);
        talonNodes = concatNodes(talonNodes, panelNodes);

        panelNodes = new StrandPanel().buildPanel(teensy, 6, "talon_bottom_mirrored", talonNodes.length, startX, startY, startZ, 0);
        talonNodes = concatNodes(talonNodes, panelNodes);

        return talonNodes;
    }

    private Node[] buildCenterTower(Teensy teensy, int startX, int startY, int startZ) {

        Node[] towerNodes = new Node[0];

        Node[] panelNodes = new StrandPanel().buildPanel(teensy, 1, "center_pillar_all", towerNodes.length, startX - 130, startY, startZ, 0);
        towerNodes = concatNodes(towerNodes, panelNodes);

        panelNodes = new StrandPanel().buildPanel(teensy, 2, "center_pillar_all", towerNodes.length, startX, startY, startZ, 0);
        towerNodes = concatNodes(towerNodes, panelNodes);


        panelNodes = new StrandPanel().buildPanel(teensy, 3, "center_pillar_all", towerNodes.length, startX + 130, startY, startZ, 0);
        towerNodes = concatNodes(towerNodes, panelNodes);

        panelNodes = new StrandPanel().buildPanel(teensy, 4, "center_pillar_all", towerNodes.length, startX + 260, startY, startZ, 0);
        towerNodes = concatNodes(towerNodes, panelNodes);


        return towerNodes;
    }


    private void buildCubotron() {

        int counter = 0;
        int size = 30;  // 30x30x30 cube
        int spacing = 10;  // spacing between nodes
        nodes = new Node[size * size * size];

        // Initialize nodes in a volumetric cube grid
        System.out.println("Built CUBOTRON: " + size + "x" + size + "x" + size + " = " + (size*size*size) + " nodes");
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    nodes[counter] = new Node(spacing * i, spacing * j, spacing * k, counter, null);
                    counter++;
                }
            }
        }

    }

    /**
     * Concatenate two Node arrays.
     */
    private static Node[] concatNodes(Node[] a, Node[] b) {
        Node[] result = new Node[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
