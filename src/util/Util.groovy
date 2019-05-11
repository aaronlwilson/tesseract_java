package util

public class Util {

  // This function will add several functions to the String class, allowing us to print messages w/ colors
  public static enableColorization() {
    String.metaClass.color_code = { code -> "${(char)27}[${code}m" }
    String.metaClass.colorize   = { code -> "${delegate.color_code(code)}${delegate.replace(delegate.color_code(0), delegate.color_code(code))}${delegate.color_code(0)}" }
    String.metaClass.red        = { -> delegate.colorize(31) }
    String.metaClass.green      = { -> delegate.colorize(32) }
    String.metaClass.yellow     = { -> delegate.colorize(33) }
    String.metaClass.blue       = { -> delegate.colorize(34) }
    String.metaClass.magenta    = { -> delegate.colorize(35) }
    String.metaClass.cyan       = { -> delegate.colorize(36) }
  }

  // Returns the directory we are going to use to store json files
  public static getDataDir() {
    // this returns the repo directory + '/data'
    "${new File(".").getAbsoluteFile().getParent()}/data"
  }
}
