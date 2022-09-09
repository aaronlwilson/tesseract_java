package hardware;

public class Rabbit extends Controller {

    //public int numTiles;
    public TilePP[] tileArray;

    //constructor
    public Rabbit(String theIp, int theId, String theMac, int theTestColor) {
        super(theIp, theId, theMac, theTestColor);

        tileArray = new TilePP[9];
    }

}
