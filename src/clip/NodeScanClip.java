package clip;

import environment.Node;

public class NodeScanClip extends AbstractClip {

    private int _scanNode;

    //constructor
    public NodeScanClip(String theClipName) {
        super(theClipName);
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
