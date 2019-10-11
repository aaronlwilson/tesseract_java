package clip;


import hardware.Tile;


import environment.Node;
import util.Util;




public class TilesTestClip  extends AbstractClip {


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


        // Checking if t.equals null or works fine.
        try
        {
            // This line of code throws NullPointerException
            // because t is null
            Tile t = node.fixture;
            //int c = t.numberPImage.pixels[node.index];

            int c = t.numberColorForNodeIndex(node.index);

            if(Util.getR(c) > 2){
                c = t.parentRabbit.testColor;
            }

            nodestate[0] = Util.getR(c);
            nodestate[1] = Util.getG(c);
            nodestate[2] = Util.getB(c);
        }
        catch(NullPointerException e)
        {
            //System.out.println("NullPointerException Caught");
        }

        return nodestate;
    }


}