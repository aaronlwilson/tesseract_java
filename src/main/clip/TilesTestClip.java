package clip;

import hardware.Fixture;
import hardware.Controller;
import environment.Node;

// Repurposed as the ScaredGeometry mapping tool: every node lights dim red except the
// strand whose controller id + pin match the current map selection (adjusted via arrow
// keys -> TesseractApp.mapController/mapPort), which lights bright blue. Used to physically
// identify which strip is wired to which teensy pin. The original Tile test-pattern logic
// was replaced here; see git history for the Tile-based version.
public class TilesTestClip  extends AbstractClip {

    private int _mapController;
    private int _mapPort;

    //constructor
    public TilesTestClip() {
    }

    public void init() {
        clipId = "tiles_test_clip";
        super.init();
    }

    @Override
    public void run() {
        _mapController = _myMain.mapController;
        _mapPort = _myMain.mapPort;
    }

    @Override
    public int[] drawNode(Node node) {
        int[] nodestate = new int[3];

        nodestate[0] = 150;
        nodestate[1] = 0;
        nodestate[2] = 0;

        Fixture f = node.fixture;
        if (f == null) return nodestate;

        Controller c = f.myController;
        if (c == null) return nodestate;

        if (f.pinNum == _mapPort && c.id == _mapController) {
            nodestate[0] = 0;
            nodestate[1] = 0;
            nodestate[2] = 255;
        }

        return nodestate;
    }
}
