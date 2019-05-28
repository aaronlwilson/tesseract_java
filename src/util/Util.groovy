package util

import groovy.json.JsonBuilder

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

  // Ensure data dir exists
  public static initDataDir() {
    String path = "${new File(".").getAbsoluteFile().getParent()}/data"
    File dirFile = new File(path)
    if (!dirFile.exists()) {
      println "Created data directory at ${path}"
      dirFile.mkdir()
    }

    initGroupDir('default')
  }

  // Ensure data dir exists
  public static initGroupDir(String group) {
    String path = Util.getDataDir(group)
    File dirFile = new File(path)
    if (!dirFile.exists()) {
      println "Created group directory at ${path}"
      dirFile.mkdir()
    }
  }

  // Returns the directory we are going to use to store json files
  // 'group' will be a way we can have different sets of files
  // ensures the directory exists
  public static getDataDir(String group = 'default') {
    // this returns '<repo dir>/data/<group>'
    String dirPath = "${new File(".").getAbsoluteFile().getParent()}/data/${group}"
    File dir = new File(dirPath)
    if (!dir.exists()) {
      dir.mkdirs()
    }

    dir
  }

  // Get the data file path for a type
  // types: ClipControl, Playlist, Scene
  public static String getDataFilePath(String type, String group = 'default') {
    "${getDataDir(group)}/${type}.json"
  }

  public static void listFilesForFolder(final File folder) {
    System.out.println("FILE LIST");

    for (final File fileEntry : folder.listFiles()) {
      if (fileEntry.isDirectory()) {
        //recursive behavior
        listFilesForFolder(fileEntry);
      } else {
        System.out.println(fileEntry.getName());
      }
    }

    //TODO return and array of strings, or JSON to be passed to the frontend to populate filename dropdown
  }


  // Pretty print a complex object.  doesn't work for objects w/ cyclical references, you can use obj.dump() and obj.inspect() on complex objects
  public static void pp(o) {
    println new JsonBuilder(o).toPrettyString()
  }

  //COLOR utility methods
  //c in this case is a processing type "color" which is really just a 32 bit integer
  public static int getR(int c) {
    return c >> 16 & 0xFF;
  }

  public static int getG(int c) {
    return c >> 8 & 0xFF;
  }

  public static int getB(int c) {
    return c & 0xFF;
  }

  public static float getPercent(int loaded, int total) {
    return ((float) loaded / total) * 100;
  }

}
