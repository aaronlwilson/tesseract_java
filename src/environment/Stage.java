package environment;

import processing.core.PApplet;


public class Stage {

    private PApplet p;

    //	An array of all the LEDs, used for render
    public Node[] nodes;

    public Stage(PApplet pApplet) {
        p = pApplet;

        nodes = new Node[20*20*20];
    }


    public void buildStage(){

        int counter = 0;

        // Initialize all nodes
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                for (int k = 0; k < 20; k++) {

                    nodes[counter] = new Node(20 * i, 20 * j, 20 * k, counter, null);

                    counter++;
                }
            }
        }


    }


}
