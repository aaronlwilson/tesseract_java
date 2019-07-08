package clip;

import processing.core.PApplet;
import processing.core.PVector;

public class Particle
{
    //variable definition
    int index;
    int lifespan = 200;

    int c;
    float radius = 0.0f;

    PVector position;
    PVector velocity;
    PVector acceleration;

    float size = 0.0f;
    float ramp = 0.0f;
    float fade = 1.0f;



    //constructor
    public Particle(PVector theL, int theC) {

        acceleration = new PVector(0, 0, 0);

        //float lowVel = -10.0f;
        //float highVel = 10.0f;


        //velocity = new PVector(PApplet.random(lowVel,highVel), PApplet.random(lowVel,highVel), PApplet.random(lowVel,highVel));
        velocity = new PVector(0, 0, 0);

        position = theL.copy();

        //index = particles.size();

        //each drop is a little different
        //c = color( int(random(100, 255)), int(random(100, 255)), int(random(100, 255)));
        //println(hex(theC));

        c = theC;
    }

    // Method to update position
    public void run() {
        velocity.add(acceleration);
        position.add(velocity);

        lifespan -= 1;
        fade = PApplet.map(lifespan, 0, 200, 0.0f, 1.0f);

        if(size <100){
            size += 10;
        }

        if(ramp <100) {
            ramp += 5;
        }
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
