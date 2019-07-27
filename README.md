# tesseract_java
Processing-based control system for interactive Kinetic LED and Flame FX sculptures

This project was created for IntelliJ Idea IDE
Install and run Idea.
File --> Open --> root directory of this repo.

Processing core.jar is included in the lib directory, but can be externally linked to wherever Processing 3+ is installed on your computer.

MUST USE JAVA SDK 1.8! Anything newer is not supported by Processing.

## Local Development (IntelliJ)

You should be able to directly open the project via `idea .` in the repo directory, or by going to File -> Open and choosing the repo directory in IntelliJ.

### Creating the IntelliJ project from scratch

This project uses Gradle.  Gradle provides dependency resolution and allows us to easily get the project set up in IntelliJ.

To configure the project from scratch in IntelliJ, follow these steps:
- First install the IntelliJ command line launcher if you haven't already
  - In IntelliJ, go to the 'Tools' menu at the top of the screen and click 'Create Command Line Launcher...'
- Then, delete any existing IntelliJ project files
  - Close IntelliJ
  - Run `rm -rf *.iml .idea` in the repo directory
- Next, run `idea build.gradle`
  - IntelliJ will open and show you a dialog box
  - You can accept the default settings
  - You can check 'Enable auto-import' if you want IntelliJ to automatically watch your project configuration for changes and update itself.  This can be very helpful, or annoying depending on the situation.
  - Note: you must have a JVM installed on your machine.  Make sure the Gradle JVM box is not empty

Once you click OK, the project will be configured.  IntelliJ will start downloading and indexing dependencies.

You can run `idea .` in the repo directory to open the project again (you can also open it again in various other ways).

Gradle allows us to easily define and resolve dependencies w/o storing them in the repo, and it will allow us to easily write tests if we ever get around to that.  :)
It also allows us to transparently integrate code written in other JVM languages, in addition to a bunch of other potentially useful stuff.  Check out some of the features here: https://gradle.org/features/

#### Creating the Launcher configuration to launch the application

If you've created the project from scratch, you can create the 'run configuration' easily.  This allows you to click the green 'play' button near the top right of the screen to launch the application.

- Open the project
- Open the file TesseractMain.java
- Find the 'main' method
- Click the 'Play' symbol next to the method (the green left-facing triangle)
- The application will launch
- Close the application
- In the Run Configurations dropdown (to the left of the play/debug buttons at the top of the screen), choose 'Save TesseractMain'

Now every time you open the project, you can run it easily by pressing the 'play' button at the top of the screen.

## Building a fat jar

A fat jar file is a jar (compiled java application) that contains all necessary dependencies to be run independently.

To build a far jar, run the command `./gradlew unzipProcessingVideoLibrary unzipProcessingUdpLibrary untarProcessingCoreLibrary downloadJoglJar fatJar` in the repo directory.

The unzip/untar commands add a bit to the application startup time when running in IntelliJ, so they don't automatically run by default and must be run manually.

The jar will be created in the `./build/libs` directory.

To run the resulting jar: `java -jar ./build/libs TesseractFatJar`.

### macos

There are helper scripts in the `bin` directory to enable you to easily build and run the application.  

Use `./bin/build_macos.sh` to build the jar and `./bin/start_macos.sh` to run the application.

Running `./bin/start_macos.sh` will automatically build the jar if it doesn't exist.

### linux

The process is largely the same for Linux.  The same scripts may even work, but they are untested on Linux.

## Configuration

Tesseract Java is configured via a YAML configuration file named 'tesseract-config.yml'.

By default, it will look in the location `./config/tesseract-config.yml`.  If it doesn't exist, the app will print a warning and use default values.

You can pass in a path to your configuration file via the system property `configPath` (e.g., `java -DconfigPath=/path/to/config/tesseract-config.yml`).

The repo contains a configuration file in `<repo>/config/tesseract-config.yml` that you can use as an example.  This is the file that will be used when running the application via your IDE.

### Configuration options

#### initialPlaylist

This option controls which playlist will load by default when the application starts.  Specify the 'displayName' property of the playlist.  If the playlist doesn't exist, the application will throw an error and exit.  These values are case-sensitive.

#### initialPlayState

This option controls the initial 'playState' of the application.  Applicable values are `loop_scene`, `playing`, or `stopped`.  These values are case-insensitive and will be converted to all caps internally.

`loop_scene` will simply continue playing the first scene continuously forever.

`playing` will advance scenes in the playlist according to the specified duration.

`stopped` will not play anything (the LEDs will be dark)
