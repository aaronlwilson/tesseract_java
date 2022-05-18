package hardware.Fixtures;

import hardware.Controllers.Controller;


public class Strip extends Fixture {


  public Controller myController;


  //constructor
  public Strip(int theId, int theLedCount, int thePinNum) {

    super(theId);

    pinNum = thePinNum;

    //not really needed
    //nodeArray = new Node[theLedCount];
  }

  public Controller getMyController() {
    return myController;
  }

  public void setMyController(Controller myController) {
    this.myController = myController;

    //for each teensy, we loop over all the attached Strands to send UDP, so give it a reference to this one
    myController.addFixture(this);
  }
}


