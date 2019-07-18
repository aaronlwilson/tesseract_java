package environment;


import app.TesseractMain;
import processing.core.PApplet;
import processing.data.XML;

import hardware.*;

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


    /*

    public void parseHardwareXML(XML xml){

        XML[] rabbits = xml.getChildren("rabbit");

        for(int j=0; j<rabbits.length; j++){
            //println(rabbits[j].getString("ip"));
            Rabbit rabbit = createRabbit(rabbits[j].getString("ip"), rabbits[j].getString("mac"), false);

            XML[] tiles = rabbits[j].getChildren("tile");
            stateManager.rabbitArray[j].tileArray = new Tile[tiles.length];

            //set the number of tiles to the text box
            cp5.get(Textfield.class, "TILE NUMBER "+rabbit.id).setText(str(tiles.length));

            for (int i = 0; i < tiles.length; i++) {
                int id = tiles[i].getInt("id");
                int snapX = tiles[i].getInt("snapX");
                int snapY = tiles[i].getInt("snapY");
                int rotation = tiles[i].getInt("rotation");

                Tile tile = createTile(stateManager.rabbitArray[j], id, 100, 100);
                tile.snapX = snapX;
                tile.snapY = snapY;

                if(rotation > 0){
                    for(int r = 0; r < rotation; r++) {//i is already in use
                        tile.rotateTile();
                    }
                }

                //place each tile in the grid, or outside the grid
                if(tile.snapX>=0 && tile.snapY>=0 && tile.snapX<gridSizeX && tile.snapY<gridSizeY){ //check array bounds
                    if(stateManager.tileGrid[tile.snapX][tile.snapY] == null){
                        stateManager.tileGrid[tile.snapX][tile.snapY] = tile;
                        tile.setPosition(gridStartX+(tile.snapX*72), gridStartY+(tile.snapY*72));
                        //println("TILE ADDED AT: "+tile.snapX+" , "+ tile.snapY);

                        //represent the state of this tile on the hardware
                        networkManager.sendNumberFrame(tile);
                    }else{
                        println("ERROR: attempting to add a tile to an occupied spot in the grid");
                    }
                }else{
                    println("The tile: " + tile.id + " is outside of the grid");
                    tile.setPosition(230+(82*(tile.id-1)), 20+(30*rabbit.id));
                }
            }//end tile for loop

            selectedRabbitGroup(1);
        }//end rabbit loop

        //save the state as Nodes array
        stateManager.saveHardwareConfiguration(gridSizeX, gridSizeY);

        //data latch
        networkManager.swapFrame(); //assuming network conf has not changed.

        //OK, now show the loaded hardware configuration view
        cp5.getTab("Hardware Configuration").bringToFront();
        viewId = 1;

        //now we have a new project loaded, scan the network to see if IP addresses are different
        networkManager.broadcastIpSearch();

    }//end parseHardwareXML

    */
}
