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
                Node node = new Node(x - 3, y - 3, startZ, n, this);
                tileNodeArray[j][i] = nodeArray[n] = node;
                tileMappingArray[col][row] = n;
                n++;
                y += ySpacer; // sometimes negative
                row += rowInc;
            }

            x -= xSpacing;
            col -= 1;

            if (i % 2 == 0) {//even number col
                y = startY + ySpacing;
                row = 0;
                ySpacer = ySpacing;
                rowInc = 1;

            } else {
                //reset to bottom
                y = startY + (12 * ySpacing);
                ySpacer = -ySpacing;
                row = 11;
                rowInc = -1;
            }
        }

        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
//                Node node = new Node(j * xSpacing, i * ySpacing, startZ, n, this);
//                tileNodeArray[j][i] = nodeArray[n] = node;
                System.out.printf(" %d,", tileMappingArray[j][i]);
            }
            System.out.println();
        }

        return nodeArray;
    }

    @Override
    public int numberColorForNodeIndex(int index) {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                if (tileMappingArray[j][i] == index) {
                    return numberPImageArray[j][i];
                }
            }
        }
        return 0;
    }

}
