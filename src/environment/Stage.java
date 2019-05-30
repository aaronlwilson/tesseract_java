package environment;


import app.TesseractMain;
import hardware.Rabbit;
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


    public void buildStage(){

        int counter = 0;

        /*
        // Initialize a crap-ton of nodes, just a big basic cubeotron
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                for (int k = 0; k < 30; k++) {
                    nodes[counter] = new Node(10*i, 10*j, 10*k, counter, null);
                    counter++;
                }
            }
        }
        */


        PixelPlane plane = new PixelPlane(p);
        //nodes = plane.buildFullCube(counter,-175,-175, -175, 0 );


        //one rabbit per 9 tiles
        _myMain.udpModel.rabbits[0] = new Rabbit("192.168.1.102", 1, "mac_address");
        _myMain.udpModel.rabbits[1] = new Rabbit("192.168.1.105", 2, "mac_address");
        _myMain.udpModel.rabbits[2] = new Rabbit("192.168.1.103", 3, "mac_address");
        _myMain.udpModel.rabbits[3] = new Rabbit("192.168.1.104", 4, "mac_address");
        _myMain.udpModel.rabbits[4] = new Rabbit("192.168.1.101", 5, "mac_address");
        _myMain.udpModel.rabbits[5] = new Rabbit("192.168.1.101", 6, "mac_address");


        nodes = plane.buildPanel(_myMain.udpModel.rabbits[0], counter,0,-72,0, 0 );

        Node[] planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[1], counter,(72*3),-72,0, 0 );
        nodes = (Node[]) p.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[2], counter,(72*6),-72,0, 0 );
        nodes = (Node[]) p.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[3], counter,-(72*3),-72,0, 0 );
        nodes = (Node[]) p.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[4], counter,-(72*6),-72,0, 0 );
        nodes = (Node[]) p.concat( nodes, planeNodes );

        planeNodes = plane.buildPanel(_myMain.udpModel.rabbits[5], counter,-(72*9),-72,0, 0 );
        nodes = (Node[]) p.concat( nodes, planeNodes );


        //set the boundaries of the stage
        for (Node n: nodes) {
            if(n.x>maxW) maxW = n.x;
            if(n.y>maxH) maxH = n.y;
            if(n.z>maxD) maxD = n.z;
        }

    }


}
