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
 *   java -jar TesseractFatJar.jar           # headed mode (default)
 *   java -jar TesseractFatJar.jar --headless  # headless mode for Pi/servers
 */
public class TesseractLauncher {

    public static void main(String[] args) {
        boolean headless = false;
        int width = 1400;
        int height = 800;

        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--headless") || arg.equals("-h")) {
                headless = true;
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
            } else if (arg.equals("--help")) {
                printHelp();
                return;
            }
        }

        // Auto-detect headless mode if no display available
        if (!headless && System.getenv("DISPLAY") == null && !isMacOS()) {
            System.out.println("No display detected, running in headless mode");
            headless = true;
        }

        // Create and launch application
        TesseractApp app = new TesseractApp(headless, width, height);

        if (headless) {
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
        System.out.println("  --headless, -h     Run without display (for Raspberry Pi/servers)");
        System.out.println("  --width, -w NUM    Window width (default: 1400)");
        System.out.println("  --height, -H NUM   Window height (default: 800)");
        System.out.println("  --version, -v      Print version and exit");
        System.out.println("  --help             Show this help message");
        System.out.println();
        System.out.println("Keyboard controls (headed mode):");
        System.out.println("  s                  Toggle UDP sending");
        System.out.println("  d                  Toggle visualization drawing");
        System.out.println("  arrow keys         Mapping tool: UP/DOWN controller, LEFT/RIGHT pin");
    }
}
