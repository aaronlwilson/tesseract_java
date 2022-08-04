package hardware;

import environment.Node;
import processing.core.PImage;

public class Tile extends Fixture {
  public static int numLEDperTile = 144;

  public int[][] numberPImageArray;
  public PImage numberPImage;

  //this tile holds references to all its nodes, and where they are mapped considering rotation
  public Node[][] tileNodeArray = new Node[12][12];

  int xSpacing = 6;
  int ySpacing = 6;

  public int rotation = 0;
  public int panelRotation = 0;
  public int orientation = 0;

  public boolean flipHorizontal;
  public boolean flipVertical;

  //CONSTRUCTOR
  public Tile(int theId) {
    super(theId);
    id = theId;

    String imagePath = "tiles/pixel_number_" + id + ".gif";
    numberPImage = app.TesseractMain.getMain().loadImage(imagePath);

    numberPImageArray = new int[12][12];
    //convert the image to a 2d array so that it can be rotated
    int loc = 0;
    for (int j = 0; j < 12; j++) {
      for (int i = 0; i < 12; i++) {
        int c = numberPImage.pixels[loc];
        numberPImageArray[i][j] = c;
        loc++;
      }
    }//end convert
  }//end constructor


  static void flipNodeMatrixHorizontal(Node mat[][]) {
    int N = mat.length;

    for (int i = 0; i < N / 2; i++) {
      for (int j = 0; j < mat[i].length; j++) {
        Node temp = mat[i][j];
        mat[i][j] = mat[N - 1 - i][j];
        mat[N - 1 - i][j] = temp;
      }
    }
  }

  static void flipImageMatrixHorizontal(int mat[][]) {
    int N = mat.length;

    for (int i = 0; i < N / 2; i++) {
      for (int j = 0; j < mat[i].length; j++) {
        int temp = mat[i][j];
        mat[i][j] = mat[N - 1 - i][j];
        mat[N - 1 - i][j] = temp;
      }
    }
  }

  static void flipNodeMatrixVertical(Node mat[][]) {
    int N = mat.length;

    for (int i = 0; i < N; i++) {
      for (int j = 0; j < mat[i].length / 2; j++) {
        Node temp = mat[i][j];
        mat[i][j] = mat[i][mat[i].length - 1 - j];
        mat[i][mat[i].length - 1 - j] = temp;
      }
    }
  }

  static void flipImageMatrixVertical(int mat[][]) {
    int N = mat.length;

    for (int i = 0; i < N; i++) {
      for (int j = 0; j < mat[i].length / 2; j++) {
        int temp = mat[i][j];
        mat[i][j] = mat[i][mat[i].length - 1 - j];
        mat[i][mat[i].length - 1 - j] = temp;
      }
    }
  }

  // An Inplace function to rotate a N x N matrix
  // by 90 degrees in anti-clockwise direction
  static void rotateNodeMatrix(Node mat[][]) {
    int N = mat.length;

    // Consider all squares one by one
    for (int x = 0; x < N / 2; x++) {
      // Consider elements in group of 4 in
      // current square
      for (int y = x; y < N - x - 1; y++) {
        // store current cell in temp variable
        Node temp = mat[x][y];

        // move values from right to top
        mat[x][y] = mat[y][N - 1 - x];

        // move values from bottom to right
        mat[y][N - 1 - x] = mat[N - 1 - x][N - 1 - y];

        // move values from left to bottom
        mat[N - 1 - x][N - 1 - y] = mat[N - 1 - y][x];

        // assign temp to left
        mat[N - 1 - y][x] = temp;
      }
    }
  }

  //clockwise 90
  static void rotateImageMatrix(int mat[][]) {
    int N = mat.length;

    // Consider all squares one by one
    for (int x = 0; x < N / 2; x++) {
      // Consider elements in group of 4 in
      // current square
      for (int y = x; y < N - x - 1; y++) {
        // store current cell in temp variable
        int temp = mat[x][y];

        // move values from right to top
        mat[x][y] = mat[y][N - 1 - x];

        // move values from bottom to right
        mat[y][N - 1 - x] = mat[N - 1 - x][N - 1 - y];

        // move values from left to bottom
        mat[N - 1 - x][N - 1 - y] = mat[N - 1 - y][x];

        // assign temp to left
        mat[N - 1 - y][x] = temp;
      }
    }
  }


  // convert image xy to pixel index
  int xyToIndex(int x, int y, int width) {
    return (y * width) + x;
  }

  // convert pixel index to image x
  int indexToX(int i, int width) {
    return (i % width);
  }

  // convert pixel index to image y
  int indexToY(int i, int height) {
    return (i / height);
  }

  public int numberColorForNodeIndex(int index) {
    int row = indexToX(index, 12);
    int col = indexToY(index, 12);
    return numberPImageArray[row][col];
  }
}
