package hardware;

import app.TesseractMain;
import com.heroicrobot.dropbit.registry.*;
import com.heroicrobot.dropbit.devices.pixelpusher.Pixel;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;
import environment.Node;
import processing.core.PApplet;

import java.util.*;

public class PixelPusher extends Controller {

    PixelPusherObserver observer;
    DeviceRegistry registry;

    public TileAPA[] tileArray;

    //constructor
    public PixelPusher(String theIp, int theId, String theMac, int theTestColor) {
        super(theIp, theId, theMac, theTestColor);

        registry = new DeviceRegistry();
        observer = new PixelPusherObserver();
        registry.addObserver(observer);

        tileArray = new TileAPA[9];
    }

    public void send() {
        // System.out.println("PIXEL PUSHER SEND");

        // scrape for the strips
        if (observer.hasStrips) {
            registry.setExtraDelay(0);
            registry.startPushing();

            //send data
            sendPixelData();
        }
    }

    private void sendPixelData() {
        List<Strip> strips = registry.getStrips();

        // for every strip:
        for (int j = 0; j < strips.size(); j++) {
            Strip strip = strips.get(j);

            // for every pixel in the physical strip
            for (int i = 0; i < strip.getLength(); i++) {
                Node node = tileArray[0].nodeArray[i];
                int c = ((node.r & 0x0ff) << 16) | ((node.g & 0x0ff) << 8) | (node.b & 0x0ff);

                strip.setPixel(c, i);
            }
        }
    }

}
