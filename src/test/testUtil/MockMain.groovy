package testUtil

import app.TesseractApp

// This is a class where we can mock any functions that are called in TesseractApp
// (previously mocked TesseractMain which extended Processing's PApplet)
class MockMain extends TesseractApp {
  MockMain() {
    super(true, 100, 100) // headless mode, 100x100 window
  }

  @Override
  public int color(int r, int g, int b) {
    return 0
  }
}
