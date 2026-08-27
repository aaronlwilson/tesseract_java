package stores

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import util.Util

// Global (non-clip-scoped) per-channel brightness filter, applied to every node's final color
// every frame in TesseractApp.renderNode(). Distinct from clip 'p' knobs, which are per-scene.
class MasterBrightnessStore {
  public static MasterBrightnessStore instance

  private double r = 1.0
  private double g = 1.0
  private double b = 1.0
  // Degrees, 0-360, wraps rather than clamps. 0 (== 360) means no shift.
  private double hue = 0.0

  public MasterBrightnessStore() {
    Map data = this.loadDataFromDisk()
    if (data != null) {
      this.r = clamp(data.r)
      this.g = clamp(data.g)
      this.b = clamp(data.b)
      if (data.hue != null) {
        this.hue = wrapHue(data.hue)
      }
    }
  }

  // Singleton
  public static MasterBrightnessStore get() {
    if (instance == null) {
      instance = new MasterBrightnessStore()
    }

    instance
  }

  public double getR() { this.r }
  public double getG() { this.g }
  public double getB() { this.b }
  public double getHue() { this.hue }

  // Set one channel by name ('r', 'g', 'b', or 'hue'). This is the one place taking untrusted
  // network input (a client-sent value), unlike the internal renderNode() math, so it
  // clamps/wraps defensively rather than trusting the caller.
  public void setChannel(String channel, double value) {
    if (channel == 'r') {
      this.r = clamp(value)
    } else if (channel == 'g') {
      this.g = clamp(value)
    } else if (channel == 'b') {
      this.b = clamp(value)
    } else if (channel == 'hue') {
      // Hue wraps rather than clamps: 370 degrees is 10 degrees, not 360.
      this.hue = wrapHue(value)
    } else {
      System.err.println("[MasterBrightnessStore] Ignoring unknown channel '${channel}'")
    }
  }

  private static double clamp(value) {
    double d = value as double
    Math.max(0.0, Math.min(1.0, d))
  }

  private static double wrapHue(value) {
    double d = value as double
    ((d % 360.0) + 360.0) % 360.0
  }

  // Load the JSON data from disk and parse it. Returns null if no file exists yet (first run).
  public Map loadDataFromDisk() {
    File dataFile = new File(Util.getDataFilePath('brightness'))
    if (!dataFile.exists()) {
      return null
    }

    println "Loading Master Brightness Data from Disk".yellow()
    new JsonSlurper().parseText(dataFile.text) as Map
  }

  // Save current state to disk as JSON
  public void saveDataToDisk() {
    println "Writing Master Brightness Data to Disk".yellow()

    String filename = Util.getDataFilePath('brightness')

    new File(filename).write "${new JsonBuilder(this.asJsonObj()).toPrettyString()}\n"
    println "Wrote Master Brightness Data to Disk".green()
  }

  // Get the store data as JSON, either for persisting or sending to the front end
  public Map asJsonObj() {
    [r: this.r, g: this.g, b: this.b, hue: this.hue]
  }
}
