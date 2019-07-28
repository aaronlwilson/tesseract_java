package stores

import show.Playlist
import util.Util
import org.yaml.snakeyaml.Yaml

class ConfigStore extends BaseStore {
  public static ConfigStore instance

  // this is the data read from the configuration file
  private Map configData

  // Map of data/functions for the config options
  // defaultValue - the value that will be used if the field isn't defined in the config file, or there is no config file loaded
  // transformValue - a function that will transform the value when it is loaded.  for example, we require the playState to be ALL_CAPS, this allows us to transform it when we load the data
  // validateFn - a function to validate the config option.  returns true if its valid, otherwise false
  // validateFailMsgFn - a function that returns the error message to print when validation fails.  The function is passed the value that caused the validation to fail.  This is REQUIRED if validateFn is defined
  // EVERY option must have an entry in this map, or we won't load it!
  private Map configOptions = [
      initialPlaylist : [
          defaultValue     : 'Color Cube',
          validateFn       : { PlaylistStore.get().find('displayName', it) != null },
          validateFailMsgFn: { "Playlist '${it}' does not exist" }
      ],
      initialPlayState: [
          defaultValue     : 'loop_scene',
          transformValueFn : { it.toUpperCase() },
          validateFn       : { String playStateStr ->
            try {
              playStateStr as Playlist.PlayState
            } catch (IllegalArgumentException e) {
              return false
            }
          },
          validateFailMsgFn: { "PlayState '${it}' is invalid.  Must be one of 'playing', 'loop_scene', or 'stopped'" },
      ],
  ]

  // Singleton
  public static ConfigStore get() {
    if (instance == null) {
      instance = new ConfigStore()
    }

    instance
  }

  public ConfigStore() {
    this.loadConfigData()
  }

  // Loads the configuration data, transforms the values, validates the values, then populates the maps we use to retrieve the values
  private loadConfigData() {
    // read in configuration file.  defaults to config/tesseract-config.yml
    String configPath = System.getProperty("configPath") ?: 'config/tesseract-config.yml'

    File configFile = new File(configPath)

    // The config options we've loaded from the file
    Map loadedConfigData = [:]

    // Final storage place for the config options
    this.configData = [:]

    if (configFile.exists()) {
      println "Reading config file from ${configFile.getCanonicalPath().cyan()}".yellow()
      loadedConfigData = new Yaml().load(configFile.text)
    } else {
      println("WARNING: No configuration file found at '${configFile.getCanonicalPath()}'.  Using default values")
    }

    this.configOptions.each { optionKey, optionData ->
      def value = loadedConfigData[optionKey] ?: optionData.defaultValue

      // Transform values if the transformValueFn function is defined
      if (optionData.transformValueFn != null) {
        value = optionData.transformValueFn(value)
      }

      // Validate the options with the validateFn function, print errors with the validateFailMsgFn
      if (optionData.validateFn != null && !optionData.validateFn(value)) {
        Util.throwException("ERROR: Failed validation of option '${optionKey}': ${optionData.validateFailMsgFn(value)}")
      }

      this.configData[optionKey] = value
    }
  }

  // Get a Boolean config value
  public Boolean getBool(String key) {
    this.getOption(Boolean, key)
  }

  // Get a String config value
  public String getString(String key) {
    this.getOption(String, key)
  }

  // Internal method that does some type checking
  private getOption(Class type, String key) {
    def value = this.configData[key] ?: this.defaultConfigValues[key]
    if (!(type.isInstance(value))) {
      Util.throwException("ERROR: config option with key '${key}' is not type '${type.toString()}'")
    }
    value
  }
}
