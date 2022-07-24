package hardware;

public class Rabbit extends Controller {

  //public int numTiles;
  public Tile[] tileArray;

  //constructor
  public Rabbit(String theIp, int theId, String theMac) {
    super(theIp, theId, theMac);

    tileArray = new Tile[9];
  }


}
