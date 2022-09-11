package clip;

import hardware.Fixture;
import hardware.Tile;
import environment.Node;
import util.Util;

public class TilesTestClip  extends AbstractClip implements ClipInterface {

    //constructor
    public TilesTestClip() {
    }

    public void init() {
        clipId = "tiles_test_clip";
        super.init();
    }

    @Override
    public void run() {

    }

    @Override
    public int[] drawNode(Node node) {
        int[] nodestate = new int[3];

        // Checking if t.equals null or works fine.
        try
        {
            // This line of code throws NullPointerException
            // because t is null
            Tile t = (Tile)node.fixture;

            //int c = t.numberPImage.pixels[node.index];
            int c = t.numberColorForNodeIndex(node.index);

            //different color for each panel
            if(Util.getR(c) > 2){
                c = t.myController.testColor;
            }

            nodestate[0] = Util.getR(c);
            nodestate[1] = Util.getG(c);
            nodestate[2] = Util.getB(c);
        }
        catch(NullPointerException e)
        {
            System.out.println("NullPointerException Caught: TilesTestClip");
            nodestate[0] = 0;
            nodestate[1] = 0;
            nodestate[2] = 255;
        }
        catch(ClassCastException e) {
            System.out.println("ClassCastException Caught: TilesTestClip");
            nodestate[0] = 255;
            nodestate[1] = 0;
            nodestate[2] = 0;
        }

        return nodestate;
    }
}