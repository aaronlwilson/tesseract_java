package environment;


import processing.core.PApplet;
import hardware.*;

public class PixelPlane {

    private PApplet p;

    public Tile[][] panelTileArray = new Tile[3][3];

    // This layout corresponds to the physical construction of the pixel plane panels. I had to invert the matrix to get the transformations to work, not sure why... aliens
    int layoutMatrix[][] =
            {
                    {9, 4, 3},
                    {8, 5, 2},
                    {7, 6, 1}
            };


    public PixelPlane(PApplet pApplet) {
        p = pApplet;
    }//end constructor



    private static void flipMatrixHorizontal(int mat[][])
    {
        int N = mat.length;

        for (int i = 0; i < N / 2; i++) {
            for (int j = 0; j < mat[i].length; j++) {
                int temp = mat[i][j];
                mat[i][j] = mat[N - 1 - i][j];
                mat[N - 1 -i][j] = temp;
            }
        }
    }

    private static void flipMatrixVertical(int mat[][])
    {
        int N = mat.length;

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < mat[i].length / 2; j++) {
                int temp = mat[i][j];
                mat[i][j] = mat[i][mat[i].length - 1 - j];
                mat[i][mat[i].length - 1 -j] = temp;
            }
        }
    }

    private static int[][] rotateMatrixClockwise(int[][] mat){
        //rotate a 2D array
        int N = mat.length;

        int[][] rotatedArray = new int[N][N];
        for (int j=0; j<N; j++){
            for (int i=0; i<N; i++){
                rotatedArray[j][i] = mat[i][N-j-1];
            }
        }
        return rotatedArray;
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



    public Node[] buildPanel(Rabbit rabbit, int startIndex, int startX, int startY, int startZ, int panelRotation, int orientation, boolean flipHorizontal, boolean flipVertical, boolean channelSwap){

        Node[] planeNodes = new Node[0];

        int inc = 6*12; //spacing 6 x 12 nodes

        //adjust the layoutMatrix prior to layout

        if(flipHorizontal)
            flipMatrixHorizontal(layoutMatrix);

        if(flipVertical)
            flipMatrixVertical(layoutMatrix);

        for(int r=0; r<panelRotation; r++) {
            layoutMatrix = rotateMatrixClockwise(layoutMatrix);
        }



        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                int xTilePos = startX + (inc*j);
                int yTilePos = startY + (inc*i);

                int tileId = layoutMatrix[j][i];

                int tileRot = (tileId >3 && tileId <7) ? 2 : 0;

                Tile tile = new Tile(rabbit, tileId);
                tile.rotation = tileRot;
                tile.panelRotation = panelRotation;
                tile.orientation = orientation;
                tile.flipHorizontal = flipHorizontal;
                tile.flipVertical = flipVertical;

                //hack for the old school pixel plane panel that has 8 of 9 tiles with rgb channels swapped
                tile.channelSwap = channelSwap;

                rabbit.tileArray[tileId - 1] = tile;

                Node[] tileNodes = tile.getNodeLayout(xTilePos, yTilePos, startZ);
                planeNodes = (Node[]) p.concat( planeNodes, tileNodes );

                panelTileArray[i][j] = tile;
            }
        }

        return planeNodes;
    }


}
