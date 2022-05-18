package environment;


import hardware.Controllers.Rabbit;
import hardware.Fixtures.Tile;
import processing.core.PApplet;

public class PixelPlane {

    private PApplet p;

    public PixelPlane(PApplet pApplet) {
        p = pApplet;
    }


    public Node[] buildFullCube(int startIndex, int startX, int startY, int startZ, int rotation){

        int total = (36*36)*6;
        Node[] planeNodes = new Node[total];

        int counter = 0;

        //matrix math to perform rotations on the whole grid...

        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 36; j++) {
                int x = (10*i) + startX;
                int y = (10*j) + startY;
                int z = startZ-10;

                planeNodes[counter] = new Node(x, y, z, counter, null);
                counter++;
            }
        }

        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 36; j++) {
                int x = (10*i) + startX;
                int y = startY-10;
                int z = (10*j) + startZ;

                planeNodes[counter] = new Node(x, y, z, counter, null);
                counter++;
            }
        }

        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 36; j++) {
                int x = startX-10;
                int y = (10*i) + startY;
                int z = (10*j) + startZ;

                planeNodes[counter] = new Node(x, y, z, counter, null);
                counter++;
            }
        }

        // other half
        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 36; j++) {
                int x = (10*i) + startX;
                int y = (10*j) + startY;
                int z = startZ+360;

                planeNodes[counter] = new Node(x, y, z, counter, null);
                counter++;
            }
        }

        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 36; j++) {
                int x = startX+360;
                int y = (10*i) + startY;
                int z = (10*j) + startZ;

                planeNodes[counter] = new Node(x, y, z, counter, null);
                counter++;
            }
        }

        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 36; j++) {
                int x = (10*i) + startX;
                int y = startY+360;
                int z = (10*j) + startZ;

                planeNodes[counter] = new Node(x, y, z, counter, null);
                counter++;
            }
        }
        return planeNodes;
    }



    public Node[] buildPanel(Rabbit rabbit, int startIndex, int startX, int startY, int startZ, int rotation, boolean channelSwap){

        Node[] planeNodes = new Node[0];
        int nodeCounter = startIndex;
        int tileCounter = 1;

        int inc = 6*12; //spacing 6 x 12 nodes
        int xTilePos = startX + (inc*2);
        int yTilePos = startY;


        //bottom 3
        for(int i=0; i<3; i++) {
            Tile tile = new Tile(rabbit, tileCounter);
            tile.rotation = 0;

            //big hack for the old school pixel plane panel that has 8 of 9 tiles with rgb channels swapped
            if(i>0 && channelSwap)
                tile.channelSwap = true;

            rabbit.tileArray[tileCounter - 1] = tile;

            Node[] tileNodes = tile.getNodeLayout(xTilePos, yTilePos, nodeCounter);
            planeNodes = (Node[]) p.concat( planeNodes, tileNodes );
            nodeCounter += 144;
            tileCounter++;

            xTilePos -= inc;
        }

        xTilePos += inc;
        yTilePos -= inc;


        //middle 3
        for(int i=0; i<3; i++) {
            Tile tile = new Tile(rabbit, tileCounter);
            tile.rotation = 2;//upside down

            //big hack for the old school pixel plane panel that has 8 of 9 tiles with rgb channels swapped
            if(channelSwap)
                tile.channelSwap = true;

            rabbit.tileArray[tileCounter - 1] = tile;

            Node[] tileNodes = tile.getNodeLayout(xTilePos, yTilePos, nodeCounter);
            planeNodes = (Node[]) p.concat( planeNodes, tileNodes );
            nodeCounter += 144;
            tileCounter ++;

            xTilePos += inc;
        }

        xTilePos -= inc;
        yTilePos -= inc;


        //top 3
        for(int i=0; i<3; i++) {
            Tile tile = new Tile(rabbit, tileCounter);

            //big hack for the old school pixel plane panel that has 8 of 9 tiles with rgb channels swapped
            if(channelSwap)
                tile.channelSwap = true;

            rabbit.tileArray[tileCounter - 1] = tile;

            Node[] tileNodes = tile.getNodeLayout(xTilePos, yTilePos, nodeCounter);
            planeNodes = (Node[]) p.concat( planeNodes, tileNodes );
            nodeCounter += 144;
            tileCounter ++;

            xTilePos -= inc;
        }

        return planeNodes;
    }


}
