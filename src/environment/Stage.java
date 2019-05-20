package environment;

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

    public Stage(PApplet pApplet) {
        p = pApplet;

        nodes = new Node[30*30*30];
    }


    public void buildStage(){

        int counter = 0;

        // Initialize all nodes
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                for (int k = 0; k < 30; k++) {

                    int x = 10*i;
                    int y = 10*j;
                    int z = 10*k;

                    if(x>maxW) maxW = x;
                    if(y>maxH) maxH = y;
                    if(z>maxD) maxD = z;

                    nodes[counter] = new Node(x, y, z, counter, null);
                    counter++;
                }
            }
        }
    }


}
