package hardware;

import java.util.ArrayList;

public class Controller {

  public String ip;
  public int id;
  public String mac;

  public ArrayList<Fixture> fixtureArray;


  //constructor
  public Controller(String theIp, int theId, String theMac) {
    ip = theIp;
    id = theId;
    mac = theMac;

    fixtureArray = new ArrayList<>();

  }


  public void addFixture(Fixture fixture) {
    fixtureArray.add(fixture);

  }


}
