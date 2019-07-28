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
  private Map<String, Object> configOptions = [
      initialPlaylist : [
          defaultValue     : 'Color Cube',
          validateFn       : { PlaylistStore.get().find('displayName', it) != null },
          validateFailMsgFn: { "Playlist '${it}' does not exist" }
      ],
      initialPlayState: [
          defaultValue     : 'LOOP_SCENE',
          transformValueFn : { it.toUpperCase() },
          validateFn       : { String playStateStr ->
            try {
              playStateStr as Playlist.PlayState
            } catch (IllegalArgumentException e) {
              return false
            }
          },
          validateFailMsgFn: { "PlayState '${it}' is invalid.  Must be one of 'PLAYING', 'LOOP_SCENE', or 'STOPPED'" },
      ],
      stageType       : [
          defaultValue     : 'CUBOTRON',
          transformValueFn : { it.toUpperCase() },
          validateFn       : { ['CUBOTRON', 'TESSERACT', 'DRACO'].contains(it) },
          validateFailMsgFn: { "stageType '${it}' is invalid.  Must be one of 'CUBOTRON', 'TESSERACT', or 'DRACO'" },
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

  private Map loadConfigFile() {
    // read in configuration file.  defaults to config/tesseract-config.yml
    String configPath = System.getProperty("configPath") ?: 'config/tesseract-config.yml'

    File configFile = new File(configPath)

    // The config options we've loaded from the file
    Map<String, Object> fileConfigValues = [:]

    // Final storage place for the config options
    this.configData = [:]

    if (configFile.exists()) {
      println "Reading config file from ${configFile.getCanonicalPath().cyan()}".yellow()
      fileConfigValues = new Yaml().load(configFile.text)
    } else {
      println("WARNING: No configuration file found at '${configFile.getCanonicalPath()}'")
    }

    // Verify all of the options are valid/recognized and print warnings if they are not
    fileConfigValues.each { String k, v ->
      if (!this.configOptions.containsKey(k)) {
        println "WARNING: Unrecognized configuration option '${k}'.  This value will be ignored".yellow()
      }
    }

    fileConfigValues
  }

  public String getEnvVarNameForConfigOption(String optionKey) {
    "TESSERACT_${optionKey.replaceAll(/(\B[A-Z])/, '_$1')}".toUpperCase()
  }

  // Loads the configuration data, transforms the values, validates the values, then populates the maps we use to retrieve the values
  private void loadConfigData() {
    Map fileConfigValues = loadConfigFile()

    this.configOptions.each { String optionKey, optionData ->
      // First check to see if the configuration option is defined in a system property
      def value = System.getProperty(optionKey)

      // If that is null, check for an environment variable
      value = value ?: System.getenv(getEnvVarNameForConfigOption(optionKey))

      // If both the system property and environment variable are null, check the configuration file
      value = value ?: fileConfigValues[optionKey]

      // If none of system property, env var, and config file contain a value for this key, use the default from this file
      value = value ?: optionData.defaultValue

      // Transform values if the transformValueFn function is defined
      if (optionData.transformValueFn != null) {
        value = optionData.transformValueFn(value)
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
  // We will look in 3 places for configuration options: system properties, environment variables, and the configuration file
  // That is also the order of precedence, meaning system properties will override environment variables, which will override options in the configuration file
  private getOption(Class type, String key) {
    def value = this.configData[key]
    if (!(type.isInstance(value))) {
      Util.throwException("ERROR: config option with key '${key}' is not type '${type.toString()}'")
    }

    // Validate the options with the validateFn function, print errors with the validateFailMsgFn
    if (this.configOptions[key].validateFn != null && !this.configOptions[key].validateFn(value)) {
      Util.throwException("ERROR: Failed validation of option '${key}': ${this.configOptions[key].validateFailMsgFn(value)}")
    }

    value
  }
}
