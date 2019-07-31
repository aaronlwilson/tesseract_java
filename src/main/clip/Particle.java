package clip;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.Random;

public class Particle
{
    //variable definition
    int lifespan = 200;

    public int color;

    PVector position;
    PVector velocity;
    PVector acceleration;

    public float size = 100.0f;
    public float ramp = 100.0f;
    public float fade = 1.0f;



    //constructor
    public Particle(PVector theL, int theC, float theSize, float theRamp, PVector theSpeed, PVector theAccel) {

        position = theL.copy();

        color = theC;

        size = theSize;

        ramp = theRamp;

        velocity = theSpeed.copy();

        acceleration = theAccel;
    }



    // Method to update position
    public void run() {
        velocity.add(acceleration);
        position.add(velocity);

        lifespan -= 1;
        fade = PApplet.map(lifespan, 0, 200, 0.0f, 1.0f);

        /*
        if(size <100){
            size += 10;
        }

        if(ramp <100) {
            ramp += 5;
        }
        */
    }

    // Is the particle still useful?
    boolean isDead() {
        if (lifespan < 0.0) {
            return true;
        } else {
            return false;
        }
    }

}//end class Particle
