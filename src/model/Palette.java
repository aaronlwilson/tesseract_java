package model;


//import java.util.ArrayList;

import processing.core.PApplet;

import static processing.core.PApplet.hex;


public class Palette {

    private PApplet p;

    //public ArrayList<int> colors;//cannot be primitive type
    public int[] colors = new int[10];


    //constructor
    public Palette(PApplet pApplet) {

        p = pApplet;

        colors[0] = p.color(0,145,35);
        colors[1] = randomColor();
        colors[2] = p.color(0,0,0);
        colors[3] = randomColor();
        colors[4] = p.color(245,35,200);

        colors[5] = p.color(0,145,35);
        colors[6] = randomColor();
        colors[7] = p.color(0,0,0);
        colors[8] = randomColor();
        colors[9] = p.color(245,35,200);

        //System.out.println(hex(colors[4]));

    }

    public int randomColor(){
        int rBlue = (int)(Math.random()*255);
        int rRed =  (int)(Math.random()*255);
        int rGreen = (int)(Math.random()*255);

        return p.color(rRed, rBlue, rGreen);
    }



}
