package clip;

import render.Vec3;
import render.ProcessingCompat;

import java.util.Random;

public class Particle
{
    //variable definition
    int lifespan = 200;

    public int color;

    public Vec3 position;
    Vec3 velocity;
    Vec3 acceleration;

    public float size = 100.0f;
    public float ramp = 100.0f;
    public float fade = 1.0f;



    //constructor
    public Particle(Vec3 theL, int theC, float theSize, float theRamp, Vec3 theSpeed, Vec3 theAccel) {

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
        fade = ProcessingCompat.map(lifespan, 0, 200, 0.0f, 1.0f);

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
