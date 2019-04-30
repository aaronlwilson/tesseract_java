
import processing.core.*;

import clip.*;
import output.*;
//import environment.*;


public class TesseractMain extends PApplet {

    //single global instance
    public static PApplet processing;

    //	An array of stripes
    //Stripe[] stripes = new Stripe[50];

    UDPModel udpModel;
    OnScreen onScreen;


    public static void main(String[] args) {
        PApplet.main("TesseractMain", args);
    }

    @Override
    public void settings() {

        size(1100, 800, P3D);

        //pixelDensity(displayDensity());
        pixelDensity(2);
    }

    @Override
    public void setup() {
        processing = this;

        clear();

        udpModel = new UDPModel(this);

        onScreen = new OnScreen(this);

        /*
        // Initialize all "stripes"
        for (int i = 0; i < stripes.length; i++) {
            stripes[i] = new Stripe(this);
        }
        */


    }

    @Override
    public void draw() {
        clear();

        onScreen.draw();



        /*
        // Move and display all "stripes"
        for (int i = 0; i < stripes.length; i++) {
            stripes[i].move();
            stripes[i].display();
        }
        */


    }
}
