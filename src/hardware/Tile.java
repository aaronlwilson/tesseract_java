package hardware;

import environment.Node;

public class Tile extends Fixture {

    public Rabbit parentRabbit;
    //public int[][] numberPImageArray;
    //public int[][] rotatedPImageArray;

    //this tile holds references to all its nodes, and where they are mapped considering rotation
    public Node[][] tileNodeArray = new Node[12][12];


    private int xSpacing = 6;
    private int ySpacing = 6;
    private int xOff = 0;
    private int yOff = 0;

    public int snapX = -1;// -1 means its outside of the tilegrid
    public int snapY = -1;
    public int rotation = 0;

    //this is a work-around to correct one v1 tile that has swapped blue and green channels
    public boolean channelSwap;


    //CONSTRUCTOR
    public Tile(Rabbit theParentRabbit, int theId) {
        super(theId);

        parentRabbit = theParentRabbit;
        id = theId;

        /*
        String imagePath = "ui/pixel_number_"+id+".gif";
        PImage numberPImage = loadImage(imagePath);
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
        rotatedPImageArray = numberPImageArray;
         */

    }//end constructor


    //called by sceneManager to construct a scene of nodes
    public Node[] getNodeLayout(int xOffset, int yOffset, int globalIndex){
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

                //TODO map node z position
                Node node = new Node(xPos+xOffset-3, yPos+yOffset+3, 0, globalIndex,this);
                globalIndex++;

                tileNodeArray[i][j] = node;

                nodeArray[index] = node;
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

        return nodeArray;

    }//end getNodeLayout




}
