package testUtil

import app.TesseractMain

// This is a class where we can mock any functions that are called in TesseractMain
// Typically these are actually inherited functions from Processing's PApplet class
class MockMain extends TesseractMain {
  public int color() {
    return 0
  }
}
