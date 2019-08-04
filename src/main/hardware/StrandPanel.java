package hardware;


import environment.Node;
import processing.core.PApplet;
import hardware.*;

public class StrandPanel extends Fixture{

    private PApplet p;

    private int _scale = 10;

    public int pinNum;
    public Teensy teensy;

    public Node[] strandNodeArray;



    //Arrays for strand mapping Draco panels
    private int panelOneMap[][] = {{1,10,10}, {2,10,9}, {3,9,9}, {4,9,8}, {5,8,7}, {6,8,8}, {7,8,9}, {8,7,9}, {9,7,8}, {10,7,7}, {11,7,6}, {12,7,5}, {13,6,5}, {14,6,6}, {15,6,7}, {16,6,8}, {17,5,8}, {18,5,7}, {19,5,6}, {20,5,5}, {21,4,5}, {22,4,6}, {23,4,7}, {24,4,8}, {25,3,7}, {26,3,6}, {27,3,5}, {28,2,5}, {29,2,6}, {30,1,5}, {31,1,4}, {32,2,4}, {33,3,4}, {34,4,4}, {35,5,4}, {36,6,4}, {37,6,3}, {38,6,2}, {39,5,2}, {40,4,2}, {41,3,2}, {42,2,2}, {43,1,2}, {44,1,1}, {45,2,1}, {46,3,1}, {47,4,1}, {48,5,1}, {49,6,1}, {50,7,1}};
    private int panelTwoMap[][] = {{1,10,21}, {2,10,20}, {3,10,19}, {4,10,18}, {5,10,17}, {6,10,16}, {7,10,15}, {8,10,14}, {9,10,13}, {10,10,12}, {11,10,11}, {12,10,10}, {13,10,9}, {14,10,8}, {15,10,7}, {16,10,6}, {17,10,5}, {18,10,4}, {19,10,3}, {20,10,2}, {21,10,1}, {22,9,2}, {23,9,3}, {24,9,4}, {25,9,5}, {26,9,6}, {27,9,7}, {28,9,8}, {29,9,9}, {30,9,10}, {31,9,11}, {32,9,12}, {33,9,13}, {34,9,14}, {35,9,15}, {36,9,16}, {37,9,17}, {38,9,18}, {39,9,19}, {40,9,20}, {41,9,21}, {42,8,20}, {43,7,20}, {44,6,20}, {45,6,19}, {46,7,19}, {47,8,19}, {48,8,18}, {49,7,18}, {50,7,17}, {51,8,17}, {52,8,16}, {53,8,15}, {54,8,14}, {55,8,13}, {56,8,12}, {57,8,11}, {58,8,10}, {59,8,9}, {60,8,8}, {61,8,7}, {62,8,6}, {63,8,5}, {64,8,4}, {65,8,3}, {66,8,2}, {67,7,2}, {68,6,2}, {69,6,3}, {70,7,3}, {71,7,4}, {72,6,4}, {73,7,5}, {74,7,6}, {75,7,7}, {76,7,8}, {77,7,9}, {78,6,8}, {79,5,7}, {80,4,6}, {81,3,5}, {82,2,4}, {83,1,4}, {84,2,5}, {85,3,6}, {86,4,7}, {87,5,8}, {88,6,9}, {89,7,10}, {90,7,11}, {91,7,12}, {92,7,13}, {93,7,14}, {94,7,15}, {95,6,15}, {96,6,14}, {97,6,13}, {98,6,12}, {99,6,11}, {100,5,12}, {101,5,13}, {102,5,14}, {103,5,15}, {104,4,15}, {105,4,14}, {106,4,13}, {107,3,14}, {108,3,15}};


    public StrandPanel(PApplet pApplet) {
        super(0);
        strandNodeArray = new Node[0];
        p = pApplet;
    }


    public Node[] buildPanel(Teensy theTeensy, int thePinNum, int panelSpecies, int globalIndex, int startX, int startY, int startZ, int rotation){

        teensy = theTeensy;
        pinNum = thePinNum;

        int panelMap[][] = panelOneMap;

        if(panelSpecies == 2){
            panelMap = panelTwoMap;
        }

        int l = panelMap.length;
        Node[] planeNodes = new Node[l];

        for(int i=0; i<l; i++) {

            int x = panelMap[i][1] * _scale;
            int y = panelMap[i][2] * _scale;

            Node node = new Node(startX+x, startY+y, startZ, globalIndex,null);
            globalIndex++;

            planeNodes[i] = node;
        }

        strandNodeArray = planeNodes;

        return planeNodes;
    }


}
