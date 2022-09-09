package hardware;

import environment.Node;

public class TileAPA extends Tile {

    public Node[] nodeArray = new Node[numLEDperTile];

    private int[][] tileMappingArray = new int[12][12];

    //constructor
    public TileAPA(int theId, int thePinNum) {
        super(theId);

        pinNum = thePinNum;
    }

    public Node[] zigZagNodes(int startX, int startY, int startZ) {
        int n = 0;
        int x = startX + (12 * xSpacing);
        int y = startY + (12 * ySpacing);
        int ySpacer = -ySpacing;

        //for tile mapping
        int col = 11;
        int row = 11;
        int rowInc = -1;

        //make some nodes in x y z space
        for (int i = 0; i < 12; i++) { //rows
            for (int j = 0; j < 12; j++) { //cols
                Node node = new Node(x - xSpacing, y - ySpacing, startZ, n, this);
                tileNodeArray[col][row] = nodeArray[n] = node;
                tileMappingArray[col][row] = n;
                n++;
                y += ySpacer; // sometimes negative
                row += rowInc;
            }

            x -= xSpacing;
            col -= 1;

            if (i % 2 == 0) {//even number col
                y = startY + ySpacing;
                ySpacer = ySpacing;
                row = 0;
                rowInc = 1;

            } else {
                //reset to bottom
                y = startY + (12 * ySpacing);
                ySpacer = -ySpacing;
                row = 11;
                rowInc = -1;
            }
        }

        //HACK
        int numRotations = panelRotation;
        if (numRotations % 2 != 0){
            numRotations += 2;
        }

        //apply matrix transformations at the tile level
        if (this.flipVertical) {
            flipNodeMatrixVertical(tileNodeArray);
            //flipImageMatrixVertical(numberPImageArray);
        }

        if (this.flipHorizontal) {
            flipNodeMatrixHorizontal(tileNodeArray);
            //flipImageMatrixHorizontal(numberPImageArray);
        }

        for (int r = 0; r < numRotations; r++) {
            rotateNodeMatrix(tileNodeArray); //no change
            //rotateImageMatrix(numberPImageArray); //correct, 90 Clockwise
            // rotateImageMatrix(tileMappingArray); //incorrect, 90 CounterClockwise
        }

        // prints the node zigZag layout in a 12 x 12 grid
        n = 0;
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                System.out.printf(" %d n+%d,", tileNodeArray[j][i].index, n);
                n++;
                //nodeArray[n++] = tileNodeArray[j][i];
            }
            System.out.println();
        }

        System.out.println("______________________");

        return nodeArray;
    }

    @Override
    public int numberColorForNodeIndex(int index) {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {

                //if (tileMappingArray[j][i] == index) {
                if (tileNodeArray[j][i].index == index) {
                    return numberPImageArray[j][i];
                }
            }
        }

        return 0;
    }

}
