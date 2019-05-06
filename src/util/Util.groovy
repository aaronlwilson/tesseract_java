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
  public static getDataDir(String group = 'default') {
    // this returns '<repo dir>/data/<group>'
    "${new File(".").getAbsoluteFile().getParent()}/data/${group}"
  }

  // Get the data file path for a type
  // types: ClipControl, Playlist, Scene
  public static getDataFilePath(String type, String group = 'default') {
    "${new File(".").getAbsoluteFile().getParent()}/data/${group}/${type}.json"
  }

  // Pretty print a complex object.  doesn't work for objects w/ cyclical references, you can use obj.dump() and obj.inspect() on complex objects
  public static pp(o) {
    println new JsonBuilder(o).toPrettyString()
  }

//  public static printFn(Closure fn) {
//    def node = fn.metaClass.classNode.getDeclaredMethods("doCall")[0].code
//    def writer = new StringWriter()
//    node.visit new groovy.inspect.swingui.AstNodeToScriptVisitor(writer)
//    println writer
//  }
}
