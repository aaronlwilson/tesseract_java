import processing.core.*;

public class TesseractMain extends PApplet {

    public static void main(String [] args) {
        PApplet.main("TesseractMain", args);
    }

    @Override
    public void settings() {
        size(500, 500);
    }

    @Override
    public void setup() {
        clear();
    }

    @Override
    public void draw() {
        clear();
        fill(255, 0, 0);
        rect(50, 50, 100, 100);
    }
}
