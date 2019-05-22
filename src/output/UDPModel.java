package output;

import hypermedia.net.UDP;
import com.heroicrobot.dropbit.devices.pixelpusher.Pixel;

import processing.core.PApplet;


public class UDPModel {

    private PApplet p;

    private UDP udp;

    int numTiles    = 1;
    String ip       = "192.168.1.102";  // the remote IP address, rabbit uses DHCP, so you might have to check the router or use the driver app to get the IP
    int port        = 7;   //the destination port

    int numColors = 3;
    int[] c = new int[numColors*3];
    int currentNode = 0;
    int count   = 0;
    int numNodes = 12*12;

    int[][] nodeMap = new int[12][12];


    public UDPModel(PApplet pApplet) {
        p = pApplet;

        // create a new datagram connection on 6000
        // and wait for incoming message
        udp = new UDP( p, 7777 );
        udp.setBuffer(10000);

        udp.log( false );     // <-- printout the connection activity, but performance is affected
        udp.listen( false );
    }

    public void send() {
        // clear background

        for (int t=0; t<numTiles; t++){
            int nodeCount = 0;

            byte[] data = new byte[(432+4)];

            data[0] = (byte) (p.unhex("ff"));
            data[1] = (byte) (p.unhex("ff")); //chip command
            data[2] = (byte) (p.unhex("00")); //start command
            data[3] = (byte) t; //tile address

            for (int y=0; y<12; y++){
                for (int x=0; x<12; x++){
                    if(nodeCount != currentNode){

                        data[nodeMap[y][x]+4] = (byte) (PApplet.unhex("00"));
                        data[nodeMap[y][x]+3+4] = (byte) (PApplet.unhex("00"));
                        data[nodeMap[y][x]+6+4] = (byte) (PApplet.unhex("00"));

                    }else{

                        data[nodeMap[y][x]+4] = (byte) c[count + 1]; //b
                        data[nodeMap[y][x]+3+4] = (byte) c[count + 0]; //r
                        data[nodeMap[y][x]+6+4] = (byte) c[count + 2]; //g
                    }

                    nodeCount++;
                }
            }

            // send the message for each tile
            udp.send( data, ip, port );
        }//end for num tiles

        //swap command
        byte[] data = new byte[2];
        data[0] = (byte) (p.unhex("FF"));
        data[1] = (byte) (p.unhex("FE"));
        udp.send( data, ip, port );


        currentNode++;//increment node
        if(currentNode >= numNodes){
            currentNode = 0;

            count+=3;//increment color
            if(count >= (numColors*3)){
                count = 0;
            }
        }

    }

}
