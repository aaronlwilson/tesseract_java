package hardware;

import environment.Node;
import processing.core.PImage;

public class Tile extends Fixture {

    public Rabbit parentRabbit;

    public int[][] numberPImageArray;
    public PImage numberPImage;

    //this tile holds references to all its nodes, and where they are mapped considering rotation
    public Node[][] tileNodeArray = new Node[12][12];


    private int xSpacing = 6;
    private int ySpacing = 6;

    public int rotation = 0;
    public int panelRotation = 0;
    public int orientation = 0;

    public boolean flipHorizontal;
    public boolean flipVertical;

    //this is a work-around to correct one v1 tile that has swapped blue and green channels
    public boolean channelSwap;


    //CONSTRUCTOR
    public Tile(Rabbit theParentRabbit, int theId) {
        super(theId);

        parentRabbit = theParentRabbit;
        id = theId;

        String imagePath = "tiles/pixel_number_"+id+".gif";
        numberPImage = app.TesseractMain.getMain().loadImage(imagePath);


        numberPImageArray = new int[12][12];
        //convert the image to a 2d array so that it can be rotated
        int loc = 0;
        for (int j=0; j<12; j++){
            for (int i=0; i<12; i++){
                int c = numberPImage.pixels[loc];
                numberPImageArray[i][j] = c;
                loc++;
            }
        }//end convert


    }//end constructor


    private static void flipNodeMatrixHorizontal(Node mat[][])
    {
        int N = mat.length;

        for (int i = 0; i < N / 2; i++) {
            for (int j = 0; j < mat[i].length; j++) {
                Node temp = mat[i][j];
                mat[i][j] = mat[N - 1 - i][j];
                mat[N - 1 -i][j] = temp;
            }
        }
    }

    private static void flipImageMatrixHorizontal(int mat[][])
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

    private static void flipNodeMatrixVertical(Node mat[][])
    {
        int N = mat.length;

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < mat[i].length / 2; j++) {
                Node temp = mat[i][j];
                mat[i][j] = mat[i][mat[i].length - 1 - j];
                mat[i][mat[i].length - 1 -j] = temp;
            }
        }
    }

    private static void flipImageMatrixVertical(int mat[][])
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


    // An Inplace function to rotate a N x N matrix
    // by 90 degrees in anti-clockwise direction
    private static void rotateNodeMatrix(Node mat[][])
    {
        int N = mat.length;

        // Consider all squares one by one
        for (int x = 0; x < N / 2; x++) {
            // Consider elements in group of 4 in
            // current square
            for (int y = x; y < N-x-1; y++) {
                // store current cell in temp variable
                Node temp = mat[x][y];

                // move values from right to top
                mat[x][y] = mat[y][N-1-x];

                // move values from bottom to right
                mat[y][N-1-x] = mat[N-1-x][N-1-y];

                // move values from left to bottom
                mat[N-1-x][N-1-y] = mat[N-1-y][x];

                // assign temp to left
                mat[N-1-y][x] = temp;
            }
        }
    }

    //clockwise 90
    private static void rotateImageMatrix(int mat[][])
    {
        int N = mat.length;

        // Consider all squares one by one
        for (int x = 0; x < N / 2; x++) {
            // Consider elements in group of 4 in
            // current square
            for (int y = x; y < N-x-1; y++) {
                // store current cell in temp variable
                int temp = mat[x][y];

                // move values from right to top
                mat[x][y] = mat[y][N-1-x];

                // move values from bottom to right
                mat[y][N-1-x] = mat[N-1-x][N-1-y];

                // move values from left to bottom
                mat[N-1-x][N-1-y] = mat[N-1-y][x];

                // assign temp to left
                mat[N-1-y][x] = temp;
            }
        }
    }

    // convert image xy to pixel index
    private int xyToIndex(int x, int y, int width){
        return ( y * width ) + x;
    }
    // convert pixel index to image x
    private int indexToX(int i, int width){
        return ( i % width );
    }
    // convert pixel index to image y
    private int indexToY(int i, int width){
        return ( i / width );
    }

    public int numberColorForNodeIndex(int index){
        int row = indexToX(index,12);
        int col = indexToY(index, 12);
        return numberPImageArray[row][col];
    }

    //called by sceneManager to construct a scene of nodes
    public Node[] getNodeLayout(int xOffset, int yOffset, int zOffset)
    {
        //TODO this could be redone to use the rotation matrix

        Node[] nodeArray = new Node[144];
        int index = 0;

        //rotation normal(north)
        int xPos= 3;
        int yPos= 3;

        if(rotation == 1){//rotation (east)
            xPos= 3+(12*xSpacing);
            yPos = -3;
        }else if(rotation == 2){//rotation (south)
            xPos= 3+(13*xSpacing);
            yPos= 3+(11*ySpacing);
        }else if(rotation == 3){//rotation (west)
            xPos= 9;
            yPos= 3+(12*ySpacing);
        }

        for (int j=0; j<12; j++){
            for (int i=0; i<12; i++){

                if(rotation == 0){//rotation normal(north)
                    xPos += xSpacing;
                }else if(rotation == 1){//rotation (east)
                    yPos += ySpacing;
                }else if(rotation == 2){//rotation (south)
                    xPos -= xSpacing;
                }else if(rotation == 3){//rotation (west)
                    yPos -= ySpacing;
                }

                //Use orientation for very basic 3D rotation
                int nodeX = xPos+xOffset-3;
                int nodeY = yPos+yOffset+3;
                int nodeZ = zOffset;

                if(orientation == 1){
                    nodeY = zOffset;
                    nodeZ = yPos+yOffset+3;

                }else if(orientation == 2){
                    nodeX = zOffset;
                    nodeZ = xPos+xOffset-3;
                }

                Node node = new Node(nodeX, nodeY, nodeZ, index,this);

                tileNodeArray[i][j] = node;
                index++;

            }//end i

            //NEW "ROW"
            if(rotation == 0){//rotation normal(north)
                xPos = 3;
                yPos += ySpacing;
            }else if(rotation == 1){//rotation (east)
                xPos -= xSpacing;
                yPos = -3;
            }else if(rotation == 2){//rotation (south)
                xPos= 3+(13*xSpacing);
                yPos -= ySpacing;
            }else if(rotation == 3){//rotation (west)
                xPos += xSpacing;
                yPos = 3+(12*ySpacing);
            }

        }//end j

        //apply matrix transformations at the tile level
        if(this.flipVertical) {
            //if(parentRabbit.ip != "192.168.0.108") {
                flipNodeMatrixVertical(tileNodeArray);
                flipImageMatrixVertical(numberPImageArray);
            //}
        }

        if(this.flipHorizontal){
            flipNodeMatrixHorizontal(tileNodeArray);
            flipImageMatrixHorizontal(numberPImageArray);
        }

        for(int r=0; r<panelRotation; r++) {
            rotateNodeMatrix(tileNodeArray);
            rotateImageMatrix(numberPImageArray);
        }


        //return 2D array as 1D
        int c =0;
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                nodeArray[c++] = tileNodeArray[i][j];
            }
        }

        return nodeArray;

    }//end getNodeLayout

}
