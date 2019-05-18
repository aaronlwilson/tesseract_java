package environment;

import processing.core.PApplet;


public class Stage {

    private PApplet p;

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

                    nodes[counter] = new Node(10 * i, 10 * j, 10 * k, counter, null);
                    counter++;
                }
            }
        }


    }


}
