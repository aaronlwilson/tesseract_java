package clip;


import processing.core.PApplet;

public class Stripe {
    float x;       // horizontal location of stripe
    float speed;   // speed of stripe
    float w;       // width of stripe
    boolean mouse; // state of stripe (mouse is over or not?)
    PApplet p; // The parent PApplet that we will render ourselves onto

    public Stripe(PApplet pApplet) {
        p = pApplet;
        x = 0;              // All stripes start at 0
        speed = p.random(1);  // All stripes have a random positive speed
        w = p.random(10,30);
        mouse = false;
    }

    // Draw stripe
    public void display() {
        p.fill(255,100);
        p.noStroke();
        p.rect(x,0,w,p.height);
    }

    // Move stripe
    public void move() {
        x += speed;
        if (x > p.width+20) x = -20;
    }
}