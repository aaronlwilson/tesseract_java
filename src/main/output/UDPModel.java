package output;


import environment.*;
import hardware.*;

import hypermedia.net.UDP;
//import com.heroicrobot.dropbit.devices.pixelpusher.Pixel;

import processing.core.PApplet;

import stores.ConfigStore;


public class UDPModel {

  private PApplet p;
  private UDP udp;

  public Rabbit[] rabbits;
  public Teensy[] teensies;

  public int myPort = 7777; //6000 also works
  public int rabbitPort = 7;
  public int teensyPort = 1337;

  // the remote IP address, rabbit uses DHCP, so you might have to check the router or use the driver app to get the IP
  //private String broadcastIp = "255.255.255.255";


  private int[][] nodeMap = new int[12][12];


  public UDPModel(PApplet pApplet) {
    p = pApplet;

    initRabbits();
    initTeensies();

    //fill a hash map with hardware node positions
    createNodeMap();

    // create a new datagram connection on 6000
    // and wait for incoming message
    udp = new UDP(p, myPort);
    udp.setBuffer(10000);

    udp.log(false);     // <-- printout the connection activity, but performance is affected
    udp.listen(false);
  }

  // Initialize the rabbit controllers.  Number of controllers is set in env var NUM_RABBITS.  default is 0
  private void initRabbits() {
    Integer numRabbits = ConfigStore.get().getInt("numRabbits");
    rabbits = new Rabbit[numRabbits];
    // TODO: need to initialize from values in env vars
  }

  // Initialize the teensy (draco) controllers.  Number of controllers is set in env var NUM_TEENSIES.  default is 0
  private void initTeensies() {
    Integer numTeensies = ConfigStore.get().getInt("numTeensies");
    teensies = new Teensy[numTeensies];
    // TODO: need to objects initialize from values in env vars
  }

  private void createNodeMap() {
    for (int k = 0; k < 12; k++) {//y
      for (int j = 0; j < 12; j++) {//x

        int serial_port = (int) k / 4; //what serial port does this coordinate belong to?
        //println(serial_port);

        int chipRow = (k % 4) / 2;// either 0 or 1
        int chipCol = (int) ((1 - chipRow) * 4);
        int chip_address = (int) chipCol + (int) (j / 3);
        //println(chip_address);

        int node_number = 0;

        if (k % 2 == 0) {//calculate the node number now
          if (j % 3 == 0) {
            node_number = 4;
          } else if (j % 3 == 1) {
            node_number = 5;
          } else {
            node_number = 0;
          }

        } else {
          //node_number=(j%3)+2;
          if (j % 3 == 0) {
            node_number = 3;
          } else if (j % 3 == 1) {
            node_number = 2;
          } else {
            node_number = 1;
          }
        }

        //we now have serialport chipadress and node number
        //now we need to calculate the corresponding address in the outbuff
        int OUTBUFF_position = ((chip_address * 6 * 9) + node_number * 9);//this is the base chip position
        OUTBUFF_position = OUTBUFF_position + serial_port;// offset by the corrct amount for the correct serial port.
        nodeMap[k][j] = OUTBUFF_position;

      }
    }

    /*
     for (int y=0; y<12; y++){
       for (int x=0; x<12; x++){
         println(nodeMap[y][x]);
       }
     }
    */
  }

  public void send() {
    for (Rabbit rabbit : rabbits) {
      //System.out.printf("RABBIT IP: %s \n", rabbit.ip);

      for (Tile tile : rabbit.tileArray) {
        sendTileFrame(tile);
      }

      //swap command, makes all the tiles change at once
      byte[] data = new byte[2];
      data[0] = (byte) (p.unhex("FF"));
      data[1] = (byte) (p.unhex("FE"));
      udp.send(data, rabbit.ip, rabbitPort);
    }


    for (Teensy teensy : teensies) {
      sendTeensyNodes(teensy);

      sendPanelFrame(teensy);

      //swap command, makes all the tiles change at once
      byte[] data = new byte[1];
      data[0] = (byte) ('s');
      udp.send(data, teensy.ip, teensyPort);
    }

  }


  // Send tile to Tesseract
  public void sendTileFrame(Tile tile) {
    //data for one tile, one frame
    byte[] data = new byte[(432 + 4)]; //144 nodes * 3 channels per node = 432

    data[0] = (byte) (p.unhex("ff")); //listen for command
    data[1] = (byte) (p.unhex("ff")); //chip command
    data[2] = (byte) (p.unhex("00")); //start command
    data[3] = (byte) (tile.id - 1); //tile address

    //node map for one tile
    for (int y = 0; y < 12; y++) {
      for (int x = 0; x < 12; x++) {
        //one node, set each channel
        Node node = tile.tileNodeArray[x][y];

        data[nodeMap[y][x] + 4] = (byte) node.g;
        data[nodeMap[y][x] + 3 + 4] = (byte) node.r;
        data[nodeMap[y][x] + 6 + 4] = (byte) node.b;

        //hack for using v1 tiles
        if (tile.channelSwap) {
          data[nodeMap[y][x] + 4] = (byte) node.b;
          data[nodeMap[y][x] + 6 + 4] = (byte) node.g;
        }
      }
    }
    // send the message for 1 tile
    String ip = tile.parentRabbit.ip;

    //if(app.BROADCAST && ip!="X.X.X.X"){
    udp.send(data, ip, rabbitPort);
    //}

  }//end sendTileFrame


  // Send data to the Draco panels via Teensy
  public void sendFlameTest(int pin, int on) {

    for (Teensy teensy : teensies) {
      System.out.printf("FIRE TEENSY IP: %s \n", teensy.ip);

      //fire command
      byte[] data = new byte[3];
      data[0] = (byte) ('f');
      data[1] = (byte) pin;
      data[2] = (byte) on;
      udp.send(data, teensy.ip, teensyPort);
    }
  }


  // Send data to the Draco panels via Teensy
  public void sendPanelFrame(Teensy teensy) {

    //octo pin order is orange, blue, green, brown
    for (StrandPanel strandPanel : teensy.strandPanelArray) {

      int l = strandPanel.strandNodeArray.length;

      byte[] data = new byte[(l * 3) + 2];

      data[0] = (byte) ('l'); //LIGHTS command
      data[1] = (byte) strandPanel.pinNum; //pin address, once again we are doing one node per pin

      for (int i = 0; i < l; i++) {
        Node node = strandPanel.strandNodeArray[i];

        data[(i * 3) + 0 + 2] = (byte) node.r;
        data[(i * 3) + 1 + 2] = (byte) node.g;
        data[(i * 3) + 2 + 2] = (byte) node.b;

        //for the love of god, something please just happen on the lights so I know my life isn't a complete sham.
        //data[(i*3) + 0 +2] = (byte) (PApplet.unhex("FF"));
        //data[(i*3) + 1 +2] = (byte) (PApplet.unhex("FF"));
        //data[(i*3) + 2 +2] = (byte) (PApplet.unhex("FF"));
      }

      // send the bytes for each panel separately
      udp.send(data, teensy.ip, teensyPort);
    }
  }//end sendPanelFrame


  // Send test data
  private void sendTeensyNodes(Teensy teensy) {

    //int length = teensy.fixtureArray.size();

    //basically a node is an entire panel or tile
    for (Fixture fixture : teensy.fixtureArray) {

      int l = fixture.nodeArray.length;

      byte[] data = new byte[(l * 3) + 2];

      data[0] = (byte) ('l'); //LIGHTS command
      data[1] = (byte) fixture.pinNum; //pin address, once again we are doing one node per pin


      System.out.printf("pin num: %d\n", fixture.pinNum);

      for (int i = 0; i < l; i++) {
        Node node = fixture.nodeArray[i];

        data[(i * 3) + 0 + 2] = (byte) node.r;
        data[(i * 3) + 1 + 2] = (byte) node.g;
        data[(i * 3) + 2 + 2] = (byte) node.b;
      }

      // send the bytes for each tile separately
      udp.send(data, teensy.ip, teensyPort);

    }
  }


}
