package app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

/**
 * Launcher for Tesseract application.
 * Supports both headed (desktop with display) and headless (Raspberry Pi) modes.
 *
 * Usage:
 *   java -jar TesseractFatJar.jar             # auto: headed if a display is available
 *   java -jar TesseractFatJar.jar --headless  # headless mode for Pi/servers
 *   java -jar TesseractFatJar.jar --headed    # force headed, skipping auto-detection
 */
public class TesseractLauncher {

    public static void main(String[] args) {
        // null means "not stated on the command line", which is what enables the display
        // auto-detection below. --headless and --headed both pin it, and a pinned value is
        // never second-guessed.
        Boolean headless = null;
        int width = 1400;
        int height = 800;

        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--headless")) {
                headless = true;
            } else if (arg.equals("--headed")) {
                headless = false;
            } else if (arg.equals("--width") || arg.equals("-w")) {
                if (i + 1 < args.length) {
                    width = Integer.parseInt(args[++i]);
                }
            } else if (arg.equals("--height") || arg.equals("-H")) {
                if (i + 1 < args.length) {
                    height = Integer.parseInt(args[++i]);
                }
            } else if (arg.equals("--version") || arg.equals("-v")) {
                System.out.println("Tesseract Desktop " + getVersion());
                return;
            } else if (arg.equals("--help") || arg.equals("-h")) {
                printHelp();
                return;
            } else {
                // Rejecting unknown arguments matters more than it looks: a typo'd --headed
                // would otherwise fall through to auto-detection and start headless, which
                // is the exact silent-wrong-mode failure this flag exists to prevent.
                System.err.println("Unknown argument: " + arg);
                System.err.println("Run with --help to see available options.");
                System.exit(2);
            }
        }

        boolean runHeadless;
        if (headless != null) {
            runHeadless = headless;
            if (!runHeadless && !isMacOS() && System.getenv("DISPLAY") == null) {
                System.out.println("WARNING: --headed requested but DISPLAY is not set.");
                System.out.println("         Over SSH, export DISPLAY=:0 to render on the machine's own screen.");
            }
        } else {
            // Only guess when the mode wasn't stated. Note this cannot override --headed:
            // DISPLAY is unset in every plain SSH session, so letting it win would make
            // headed mode unreachable on exactly the remote hosts it's most useful for.
            runHeadless = !isMacOS() && System.getenv("DISPLAY") == null;
            if (runHeadless) {
                System.out.println("No display detected, running in headless mode (pass --headed to override)");
            }
        }

        // Create and launch application
        TesseractApp app = new TesseractApp(runHeadless, width, height);

        if (runHeadless) {
            launchHeadless(app);
        } else {
            launchDesktop(app, width, height);
        }
    }

    private static void launchDesktop(TesseractApp app, int width, int height) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Tesseract");
        config.setWindowedMode(width, height);
        config.useVsync(true);
        config.setForegroundFPS(30);

        // Enable OpenGL debugging in development
        // config.enableGLDebugOutput(true, System.err);

        System.out.println("Starting Tesseract in headed mode (" + width + "x" + height + ")");
        new Lwjgl3Application(app, config);
    }

    private static void launchHeadless(TesseractApp app) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30; // Match original 30 FPS

        System.out.println("Starting Tesseract in headless mode");
        new HeadlessApplication(app, config);

        // Keep main thread alive (HeadlessApplication runs on separate thread)
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Tesseract shutting down");
        }
    }

    private static boolean isMacOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("mac");
    }

    /** Reads the version stamped into the JAR manifest (Implementation-Version); "dev" when run from classes. */
    private static String getVersion() {
        String v = TesseractLauncher.class.getPackage().getImplementationVersion();
        return v != null ? v : "dev";
    }

    private static void printHelp() {
        System.out.println("Tesseract LED Controller");
        System.out.println();
        System.out.println("Usage: java -jar TesseractFatJar.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --headless         Run without a window (Raspberry Pi/servers)");
        System.out.println("  --headed           Force the 3D visualization, skipping display auto-detection");
        System.out.println("  --width, -w NUM    Window width (default: 1400)");
        System.out.println("  --height, -H NUM   Window height (default: 800)");
        System.out.println("  --version, -v      Print version and exit");
        System.out.println("  --help, -h         Show this help message");
        System.out.println();
        System.out.println("With neither --headless nor --headed, the mode is chosen by whether");
        System.out.println("DISPLAY is set (always headed on macOS). Over SSH DISPLAY is unset, so");
        System.out.println("pass --headed along with `export DISPLAY=:0` to render on the Pi's screen.");
        System.out.println();
        System.out.println("Keyboard controls (headed mode):");
        System.out.println("  s                  Toggle UDP sending");
        System.out.println("  d                  Toggle visualization drawing");
        System.out.println("  arrow keys         Mapping tool: UP/DOWN controller, LEFT/RIGHT pin");
    }
}
