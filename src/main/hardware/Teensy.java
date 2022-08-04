package hardware;

import environment.StrandPanel;

import java.util.ArrayList;


public class Teensy extends Controller {


    public ArrayList<StrandPanel> strandPanelArray;


    //constructor
    public Teensy(String theIp, int theId, String theMac) {

        super(theIp, theId, theMac, 0xffcc11);

        strandPanelArray = new ArrayList<>();
    }


    public void addStrandPanel(StrandPanel strandPanel){
        strandPanelArray.add(strandPanel);

    }




}
