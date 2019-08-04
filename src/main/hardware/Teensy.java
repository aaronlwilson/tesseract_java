package hardware;

import java.util.ArrayList;

public class Teensy extends Controller {


    public ArrayList<StrandPanel> strandPanelArray;


    //constructor
    public Teensy(String theIp, int theId, String theMac) {
        ip = theIp;
        id = theId;
        mac = theMac;

        strandPanelArray = new ArrayList<StrandPanel>();
    }


    public void addStrandPanel(StrandPanel strandPanel){
        strandPanelArray.add(strandPanel);

    }



}
