package hardware;

import environment.Node;

public class TilePP extends Tile {

    public Rabbit parentRabbit;

    //this is a work-around to correct one v1 tile that has swapped blue and green channels
    public boolean channelSwap;

    //CONSTRUCTOR
    public TilePP(Rabbit theParentRabbit, int theId) {
        super(theId);
        parentRabbit = theParentRabbit;
        this.setMyController(theParentRabbit);
    }//end constructor


    //called by sceneManager to construct a scene of nodes
    public Node[] getNodeLayout(int xOffset, int yOffset, int zOffset) {
        //TODO this could be redone to use the rotation matrix

        Node[] nodeArray = new Node[Tile.numLEDperTile];
        int index = 0;

        //rotation normal(north)
        int xPos = 3;
        int yPos = 3;

        if (rotation == 1) {//rotation (east)
            xPos = 3 + (12 * xSpacing);
            yPos = -3;
        } else if (rotation == 2) {//rotation (south)
            xPos = 3 + (13 * xSpacing);
            yPos = 3 + (11 * ySpacing);
        } else if (rotation == 3) {//rotation (west)
            xPos = 9;
            yPos = 3 + (12 * ySpacing);
        }

        for (int j = 0; j < 12; j++) {
            for (int i = 0; i < 12; i++) {

                if (rotation == 0) {//rotation normal(north)
                    xPos += xSpacing;
                } else if (rotation == 1) {//rotation (east)
                    yPos += ySpacing;
                } else if (rotation == 2) {//rotation (south)
                    xPos -= xSpacing;
                } else if (rotation == 3) {//rotation (west)
                    yPos -= ySpacing;
                }

                //Use orientation for very basic 3D rotation
                int nodeX = xPos + xOffset - 3;
                int nodeY = yPos + yOffset + 3;
                int nodeZ = zOffset;

                if (orientation == 1) {
                    nodeY = zOffset;
                    nodeZ = yPos + yOffset + 3;

                } else if (orientation == 2) {
                    nodeX = zOffset;
                    nodeZ = xPos + xOffset - 3;
                }

                Node node = new Node(nodeX, nodeY, nodeZ, index, this);

                tileNodeArray[i][j] = node;
                index++;

            }//end i

            //NEW "ROW"
            if (rotation == 0) {//rotation normal(north)
                xPos = 3;
                yPos += ySpacing;
            } else if (rotation == 1) {//rotation (east)
                xPos -= xSpacing;
                yPos = -3;
            } else if (rotation == 2) {//rotation (south)
                xPos = 3 + (13 * xSpacing);
                yPos -= ySpacing;
            } else if (rotation == 3) {//rotation (west)
                xPos += xSpacing;
                yPos = 3 + (12 * ySpacing);
            }

        }//end j

        //apply matrix transformations at the tile level
        if (this.flipVertical) {
            //HACK
            //if(parentRabbit.ip != "192.168.0.108") {
            flipNodeMatrixVertical(tileNodeArray);
            flipImageMatrixVertical(numberPImageArray);
            //}
        }

        if (this.flipHorizontal) {
            flipNodeMatrixHorizontal(tileNodeArray);
            flipImageMatrixHorizontal(numberPImageArray);
        }

        for (int r = 0; r < panelRotation; r++) {
            rotateNodeMatrix(tileNodeArray);
            rotateImageMatrix(numberPImageArray);
        }

        //return 2D array as 1D
        int n = 0;
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                nodeArray[n++] = tileNodeArray[i][j];
            }
        }
        return nodeArray;

    }//end getNodeLayout

}
