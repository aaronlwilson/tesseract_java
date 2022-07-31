package hardware;

import com.heroicrobot.dropbit.registry.*;
import com.heroicrobot.dropbit.devices.pixelpusher.Pixel;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;
import java.util.*;

public class PixelPusher extends Controller {

  PixelPusherObserver observer;
  DeviceRegistry registry;

  //constructor
  public PixelPusher(String theIp, int theId, String theMac) {
    super(theIp, theId, theMac);

    registry = new DeviceRegistry();
    observer = new PixelPusherObserver();
    registry.addObserver(observer);
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
    for(int j = 0; j < strips.size(); j++){
      Strip strip = strips.get(j);

      // for every pixel in the physical strip
      for (int i = 0; i < strip.getLength(); i++) {

        int c = 0xff00ff;

        strip.setPixel(c, i);
      }
    }
  }

}
