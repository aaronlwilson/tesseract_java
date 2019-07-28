package util

// This class will control fetching settings values for configuring the application
// Right now they come from environment variables, but we could have them come from configuration files or other sources as well

public class AppSettings {

  // Default values for the settings
  private static defaults = [
      STAGE_TYPE: 'CUBOTRON',
      NUM_TEENSIES: '0',
      NUM_RABBITS: '0',
  ]

  public static String get(String key) {
    String settingValue = System.getenv(key) ?: defaults[key]

    if (settingValue == null) {
      throw new RuntimeException("ERROR: Setting value not defined: '${key}'")
    }

    settingValue
  }
}
