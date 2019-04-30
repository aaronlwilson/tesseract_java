package hardware;

public class Rabbit extends Controller  {
  public String ip;
  public int id;
  public String mac;
  public int numTiles;
  
  public Tile[] tileArray;

  //constructor
  Rabbit(String theIp, int theId, String theMac) {  
    ip = theIp;
    id = theId;
    mac = theMac;
  }    


} 
