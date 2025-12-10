package render;

/**
 * Processing compatibility layer.
 * Provides pure Java implementations of Processing utility functions
 * so we can remove the Processing dependency without changing clip code.
 */
public class ProcessingCompat {

    // Perlin noise state
    private static final int PERLIN_SIZE = 4096;
    private static final float[] perlin = new float[PERLIN_SIZE + 1];
    private static int perlin_octaves = 4;
    private static float perlin_amp_falloff = 0.5f;
    private static boolean perlinInitialized = false;

    // Random for noise initialization
    private static java.util.Random perlinRandom = new java.util.Random();

    // Constants
    public static final float PI = (float) Math.PI;
    public static final float TWO_PI = PI * 2;
    public static final float HALF_PI = PI / 2;

    // Color mode constants
    public static final int RGB = 1;
    public static final int HSB = 3;

    /**
     * Initialize Perlin noise array.
     */
    private static void initPerlin() {
        if (perlinInitialized) return;
        for (int i = 0; i < PERLIN_SIZE + 1; i++) {
            perlin[i] = perlinRandom.nextFloat();
        }
        perlinInitialized = true;
    }

    /**
     * Set Perlin noise parameters (matches Processing's noiseDetail).
     */
    public static void noiseDetail(int lod, float falloff) {
        if (lod > 0) perlin_octaves = lod;
        if (falloff > 0) perlin_amp_falloff = falloff;
    }

    /**
     * Perlin noise implementation matching Processing's noise() function.
     * Single dimension.
     */
    public static float noise(float x) {
        return noise(x, 0, 0);
    }

    /**
     * Perlin noise implementation matching Processing's noise() function.
     * Two dimensions.
     */
    public static float noise(float x, float y) {
        return noise(x, y, 0);
    }

    /**
     * Perlin noise implementation matching Processing's noise() function.
     * Three dimensions.
     *
     * This is a faithful port of Processing's Perlin noise implementation.
     */
    public static float noise(float x, float y, float z) {
        initPerlin();

        if (x < 0) x = -x;
        if (y < 0) y = -y;
        if (z < 0) z = -z;

        int xi = (int) x;
        int yi = (int) y;
        int zi = (int) z;
        float xf = x - xi;
        float yf = y - yi;
        float zf = z - zi;
        float rxf, ryf;

        float r = 0;
        float ampl = 0.5f;

        for (int i = 0; i < perlin_octaves; i++) {
            int of = xi + (yi << 4) + (zi << 8);

            rxf = noise_fsc(xf);
            ryf = noise_fsc(yf);

            float n1 = perlin[of & PERLIN_SIZE];
            n1 += rxf * (perlin[(of + 1) & PERLIN_SIZE] - n1);
            float n2 = perlin[(of + 16) & PERLIN_SIZE];
            n2 += rxf * (perlin[(of + 16 + 1) & PERLIN_SIZE] - n2);
            n1 += ryf * (n2 - n1);

            of += 256;
            n2 = perlin[of & PERLIN_SIZE];
            n2 += rxf * (perlin[(of + 1) & PERLIN_SIZE] - n2);
            float n3 = perlin[(of + 16) & PERLIN_SIZE];
            n3 += rxf * (perlin[(of + 16 + 1) & PERLIN_SIZE] - n3);
            n2 += ryf * (n3 - n2);

            n1 += noise_fsc(zf) * (n2 - n1);

            r += n1 * ampl;
            ampl *= perlin_amp_falloff;
            xi <<= 1;
            xf *= 2;
            yi <<= 1;
            yf *= 2;
            zi <<= 1;
            zf *= 2;

            if (xf >= 1.0f) {
                xi++;
                xf--;
            }
            if (yf >= 1.0f) {
                yi++;
                yf--;
            }
            if (zf >= 1.0f) {
                zi++;
                zf--;
            }
        }
        return r;
    }

    private static float noise_fsc(float i) {
        return 0.5f * (1.0f - (float) Math.cos(i * Math.PI));
    }

    /**
     * Set noise seed (matches Processing's noiseSeed).
     */
    public static void noiseSeed(long seed) {
        perlinRandom = new java.util.Random(seed);
        perlinInitialized = false;
    }

    /**
     * Map a value from one range to another (matches Processing's map).
     */
    public static float map(float value, float start1, float stop1, float start2, float stop2) {
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }

    /**
     * Constrain a value between min and max (matches Processing's constrain).
     */
    public static float constrain(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Constrain an int value between min and max.
     */
    public static int constrain(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Linear interpolation (matches Processing's lerp).
     */
    public static float lerp(float start, float stop, float amt) {
        return start + (stop - start) * amt;
    }

    /**
     * Create a color integer from RGB values (matches Processing's color).
     * Returns ARGB packed integer.
     */
    public static int color(int r, int g, int b) {
        return color(r, g, b, 255);
    }

    /**
     * Create a color integer from RGBA values (matches Processing's color).
     * Returns ARGB packed integer.
     */
    public static int color(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    /**
     * Create a grayscale color (matches Processing's color(gray)).
     */
    public static int color(int gray) {
        return color(gray, gray, gray, 255);
    }

    /**
     * Create a color from HSB values (hue, saturation, brightness).
     * All values scaled 0-max.
     */
    public static int colorHSB(float h, float s, float b, float max) {
        // Normalize to 0-1
        h = (h % max) / max;
        s = s / max;
        b = b / max;

        int r, g, bl;
        if (s == 0) {
            r = g = bl = (int) (b * 255);
        } else {
            float hue = h * 6;
            int i = (int) hue;
            float f = hue - i;
            float p = b * (1 - s);
            float q = b * (1 - s * f);
            float t = b * (1 - s * (1 - f));

            switch (i % 6) {
                case 0: r = (int)(b*255); g = (int)(t*255); bl = (int)(p*255); break;
                case 1: r = (int)(q*255); g = (int)(b*255); bl = (int)(p*255); break;
                case 2: r = (int)(p*255); g = (int)(b*255); bl = (int)(t*255); break;
                case 3: r = (int)(p*255); g = (int)(q*255); bl = (int)(b*255); break;
                case 4: r = (int)(t*255); g = (int)(p*255); bl = (int)(b*255); break;
                default: r = (int)(b*255); g = (int)(p*255); bl = (int)(q*255); break;
            }
        }
        return color(r, g, bl);
    }

    /**
     * Extract red component from color (matches Processing's red).
     */
    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Extract green component from color (matches Processing's green).
     */
    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Extract blue component from color (matches Processing's blue).
     */
    public static int blue(int color) {
        return color & 0xFF;
    }

    /**
     * Extract alpha component from color (matches Processing's alpha).
     */
    public static int alpha(int color) {
        return (color >> 24) & 0xFF;
    }

    /**
     * Convert degrees to radians (matches Processing's radians).
     */
    public static float radians(float degrees) {
        return degrees * PI / 180.0f;
    }

    /**
     * Convert radians to degrees (matches Processing's degrees).
     */
    public static float degrees(float radians) {
        return radians * 180.0f / PI;
    }

    /**
     * Calculate distance between two 2D points (matches Processing's dist).
     */
    public static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    /**
     * Calculate distance between two 3D points (matches Processing's dist).
     */
    public static float dist(float x1, float y1, float z1, float x2, float y2, float z2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));
    }

    /**
     * Calculate magnitude of a 2D vector (matches Processing's mag).
     */
    public static float mag(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate magnitude of a 3D vector (matches Processing's mag).
     */
    public static float mag(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Floor function returning int (matches Processing's floor).
     */
    public static int floor(float value) {
        return (int) Math.floor(value);
    }

    /**
     * Ceiling function returning int (matches Processing's ceil).
     */
    public static int ceil(float value) {
        return (int) Math.ceil(value);
    }

    /**
     * Round function returning int (matches Processing's round).
     */
    public static int round(float value) {
        return Math.round(value);
    }

    /**
     * Absolute value for float (convenience).
     */
    public static float abs(float value) {
        return Math.abs(value);
    }

    /**
     * Absolute value for int (convenience).
     */
    public static int abs(int value) {
        return Math.abs(value);
    }

    /**
     * Square root (convenience wrapper).
     */
    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    /**
     * Power function (convenience wrapper).
     */
    public static float pow(float base, float exponent) {
        return (float) Math.pow(base, exponent);
    }

    /**
     * Sin function (convenience wrapper).
     */
    public static float sin(float angle) {
        return (float) Math.sin(angle);
    }

    /**
     * Cos function (convenience wrapper).
     */
    public static float cos(float angle) {
        return (float) Math.cos(angle);
    }

    /**
     * Tan function (convenience wrapper).
     */
    public static float tan(float angle) {
        return (float) Math.tan(angle);
    }

    /**
     * Atan2 function (convenience wrapper).
     */
    public static float atan2(float y, float x) {
        return (float) Math.atan2(y, x);
    }

    /**
     * Random float between 0 and high (matches Processing's random).
     */
    public static float random(float high) {
        return (float) (Math.random() * high);
    }

    /**
     * Random float between low and high (matches Processing's random).
     */
    public static float random(float low, float high) {
        return low + (float) (Math.random() * (high - low));
    }
}
