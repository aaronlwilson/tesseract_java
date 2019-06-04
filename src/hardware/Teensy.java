package hardware;
import environment.Node;

public class Teensy extends Controller {

    public Node[] nodeArray;

    //constructor
    public Teensy(String theIp, int theId, String theMac) {
        ip = theIp;
        id = theId;
        mac = theMac;

        nodeArray = new Node[8];
    }

}
