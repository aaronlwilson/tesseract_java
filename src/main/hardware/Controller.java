package hardware;

import output.BufferDatagram;
import output.StreamingACNDatagram;

import java.net.InetAddress;
import java.net.UnknownHostException;
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

    fixtureArray = new ArrayList<Fixture>();

  }


  public void addFixture(Fixture fixture) {
    fixtureArray.add(fixture);

  }



  private InetAddress resolveHostAddress() {
    try {
      InetAddress address = InetAddress.getByName(ip);
      return address;
    } catch (UnknownHostException uhx) {
      System.out.println("Unknown host for fixture datagram: " + uhx.getLocalizedMessage());
    }
    return null;
  }


  public void addDatagram() {
    BufferDatagram datagram = null;

    int chunkIndexBuffer[] = new int[512];
    datagram = new StreamingACNDatagram(chunkIndexBuffer, 16);


    InetAddress address = resolveHostAddress();


    if (datagram != null) {
      //datagram.enabled.setValue(address != null);

      if (address != null) {
        datagram.setAddress(address);
      }

    }
  }


}
