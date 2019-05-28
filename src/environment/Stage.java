package environment;


import processing.core.PApplet;


public class Stage {

    //private PApplet p;

    //used to automatically define bounding box
    public int maxW;
    public int maxH;
    public int maxD;

    //	An array of all the LEDs, used for render
    public Node[] nodes;
    public Node[] prevNodes; //last frames data

    public Stage(PApplet pApplet) {
        //p = pApplet;

        //nodes = new Node[30*30*30];
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


        PixelPlane plane = new PixelPlane();
        nodes = plane.buildFullCube(counter,-175,-175, -175, 0 );

        //nodes = plane.buildFullWall(counter,0,0,0, 0 );

        //set the boundaries of the stage
        for (Node n: nodes) {
            if(n.x>maxW) maxW = n.x;
            if(n.y>maxH) maxH = n.y;
            if(n.z>maxD) maxD = n.z;
        }

    }


}
