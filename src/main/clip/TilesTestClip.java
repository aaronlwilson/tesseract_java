package clip;

import hardware.Tile;
import environment.Node;
import util.Util;

// Original tile-mapping tool: shows each tile's number (from data/tiles/pixel_number_<id>.gif)
// in its controller's testColor against a black background. Used to verify that a panel's tiles
// are wired up, oriented, and numbered correctly, and that panels are assembled in the right order.
public class TilesTestClip extends AbstractClip {

    //constructor
    public TilesTestClip() {
    }

    public void init() {
        clipId = "tiles_test_clip";
        super.init();
    }

    public void run() {

    }

    public int[] drawNode(Node node) {

        int[] nodestate = new int[3];

        try {
            Tile t = (Tile) node.fixture;

            int c = t.numberColorForNodeIndex(node.index);

            //number glyph pixels light up in the panel's test color; background stays black
            if (Util.getR(c) > 2) {
                c = t.myController.testColor;
            }

            nodestate[0] = Util.getR(c);
            nodestate[1] = Util.getG(c);
            nodestate[2] = Util.getB(c);
        } catch (NullPointerException e) {
            System.out.println("NullPointerException Caught: TilesTestClip");
            nodestate[0] = 255;
            nodestate[1] = 255;
            nodestate[2] = 255;
        }

        return nodestate;
    }

}
