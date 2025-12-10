package model;

import static render.ProcessingCompat.color;


public class Palette {

    //public ArrayList<int> colors;//cannot be primitive type
    public int[] colors = new int[10];


    //constructor
    public Palette(Object unused) {
        // Parameter kept for API compatibility but no longer used

        colors[0] = color(0,145,35);
        colors[1] = randomColor();
        colors[2] = randomColor();
        colors[3] = randomColor();
        colors[4] = color(10,35,25);

        colors[5] = color(0,145,35);
        colors[6] = randomColor();
        colors[7] = randomColor();
        colors[8] = randomColor();
        colors[9] = color(16,35,24);

    }

    public int randomColor(){
        int rBlue = (int)(Math.random()*255);
        int rRed =  (int)(Math.random()*255);
        int rGreen = (int)(Math.random()*255);

        return color(rRed, rBlue, rGreen);
    }



}
