package hardware.Controllers;

import environment.StrandPanel;
import hardware.Controllers.Controller;

import java.util.ArrayList;


public class Teensy extends Controller {


    public ArrayList<StrandPanel> strandPanelArray;


    //constructor
    public Teensy(String theIp, int theId, String theMac) {

        super(theIp, theId, theMac);

        strandPanelArray = new ArrayList<StrandPanel>();
    }


    public void addStrandPanel(StrandPanel strandPanel){
        strandPanelArray.add(strandPanel);

    }




}
