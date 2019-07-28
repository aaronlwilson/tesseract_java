package clip;

import environment.Node;

public class NodeScanClip extends AbstractClip {

    //setting this in init() function, which is better?
    //String clipId = "node_scan";

    private int _scanNode;

    //constructor
    public NodeScanClip() {

    }

    public void init() {

        clipId = "node_scan";

        super.init();

        _scanNode = 0;
    }

    public void run() {
        _scanNode++;

        if(_scanNode >= _myMain.stage.nodes.length){
            _scanNode = 0;
        }

    }

    public int[] drawNode(Node node) {

        int[] nodestate = new int[3];

        if(node.index == _scanNode){
            nodestate[0] = 255;
            nodestate[1] = 255;
            nodestate[2] = 255;
        }

        return nodestate;
    }
}
