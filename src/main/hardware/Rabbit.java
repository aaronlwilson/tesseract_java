package hardware;

public class Rabbit extends Controller {

  //public int numTiles;
  public Tile[] tileArray;

  public int testColor;

  //constructor
  public Rabbit(String theIp, int theId, String theMac, int theTestColor) {
    super(theIp, theId, theMac);

    testColor = theTestColor;

    tileArray = new Tile[9];
  }


}
