package sanity;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.nio.ByteBuffer;

/**
 * Standalone tech-stack sanity check: LibGDX window + JavaCV decode -> draw the video straight to
 * the screen. NO nodes, clips, websocket, UDP, MediaStore, or app state.
 *
 * KEY: decoding runs on a background thread AND we do NOT use Java2DFrameConverter. That converter
 * is AWT/Java2D based, and on macOS AWT wants the main thread's run loop — which LWJGL/GLFW already
 * owns via -XstartOnFirstThread. Using it (on any thread) deadlocks decoding. Instead we read the
 * grabber's raw BGR24 frame buffer directly and pack it into an RGBA Pixmap. No AWT anywhere.
 *
 * Run (macOS requires -XstartOnFirstThread for LWJGL):
 *   ./gradlew fatJar
 *   java -XstartOnFirstThread -cp build/libs/TesseractFatJar.jar sanity.VideoSanityCheck
 *   java -XstartOnFirstThread -cp build/libs/TesseractFatJar.jar sanity.VideoSanityCheck /path/to/other.mp4
 */
public class VideoSanityCheck implements ApplicationListener {

    private static final String DEFAULT_VIDEO =
        "/Users/aaron/Desktop/Data Backups/Google Drive clean up/draco all videos/24K_loop-nosound.mp4";

    private final String videoPath;

    private SpriteBatch batch;
    private Pixmap pixmap;
    private Texture texture;

    // Published by the decode thread, read by the render thread. RGBA8888 bytes, top-left origin.
    private volatile byte[] latestRgba = null;
    private volatile int frameSeq = 0;
    private volatile int vw = 0, vh = 0;
    private volatile boolean dimsReady = false;
    private volatile boolean decoding = true;

    private int lastUploadedSeq = -1;
    private int renderCalls = 0;
    private Thread decodeThread;

    public VideoSanityCheck(String videoPath) {
        this.videoPath = videoPath;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        decodeThread = new Thread(this::decodeLoop, "sanity-decode");
        decodeThread.setDaemon(true);
        decodeThread.start();
    }

    // Background thread: owns the grabber, decodes frames, publishes the latest one as RGBA bytes.
    private void decodeLoop() {
        FFmpegFrameGrabber grabber = null;
        try {
            System.out.println("[Sanity] Opening: " + videoPath);
            grabber = new FFmpegFrameGrabber(videoPath);
            grabber.start();                       // default image mode -> BGR24 in frame.image[0]
            vw = grabber.getImageWidth();
            vh = grabber.getImageHeight();
            double fps = grabber.getFrameRate();
            double intervalMs = fps > 0 ? 1000.0 / fps : 1000.0 / 30.0;
            dimsReady = true;
            System.out.println("[Sanity] Opened OK: " + vw + "x" + vh + " @ " + fps + " fps");

            while (decoding) {
                long t0 = System.currentTimeMillis();

                Frame frame = grabber.grabImage();
                if (frame == null) {               // loop
                    grabber.setFrameNumber(0);
                    frame = grabber.grabImage();
                }
                if (frame != null && frame.image != null && frame.image.length > 0 && frame.image[0] != null) {
                    latestRgba = frameToRgba(frame);
                    frameSeq++;
                    if (frameSeq <= 3 || frameSeq % 30 == 0) {
                        System.out.println("[Sanity] DECODED frame " + frameSeq);
                    }
                }

                long sleep = (long) intervalMs - (System.currentTimeMillis() - t0);
                if (sleep > 0) Thread.sleep(sleep);
            }
        } catch (Exception e) {
            System.err.println("[Sanity] decode error: " + e);
            e.printStackTrace();
        } finally {
            if (grabber != null) {
                try { grabber.stop(); grabber.release(); } catch (Exception e) { /* ignore */ }
            }
        }
    }

    // Convert a raw BGR24 frame buffer to RGBA8888 bytes, honoring row stride. No AWT.
    private static byte[] frameToRgba(Frame frame) {
        int w = frame.imageWidth;
        int h = frame.imageHeight;
        int ch = frame.imageChannels;              // 3 for BGR24
        int stride = frame.imageStride;            // bytes per row (may include padding)
        ByteBuffer src = (ByteBuffer) frame.image[0];

        byte[] out = new byte[w * h * 4];
        for (int y = 0; y < h; y++) {
            int srcRow = y * stride;
            int dstRow = y * w * 4;
            for (int x = 0; x < w; x++) {
                int s = srcRow + x * ch;
                byte b = src.get(s);
                byte g = src.get(s + 1);
                byte r = src.get(s + 2);
                int d = dstRow + x * 4;
                out[d]     = r;
                out[d + 1] = g;
                out[d + 2] = b;
                out[d + 3] = (byte) 0xFF;
            }
        }
        return out;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderCalls++;
        if (renderCalls <= 3 || renderCalls % 60 == 0) {
            System.out.println("[Sanity] RENDER call #" + renderCalls
                + " dimsReady=" + dimsReady + " frameSeq=" + frameSeq + " uploaded=" + lastUploadedSeq);
        }

        if (!dimsReady) return;

        if (texture == null) {
            pixmap = new Pixmap(vw, vh, Pixmap.Format.RGBA8888);
            texture = new Texture(pixmap);
        }

        int seq = frameSeq;
        byte[] rgba = latestRgba;
        if (seq != lastUploadedSeq && rgba != null) {
            ByteBuffer pb = pixmap.getPixels();
            pb.clear();
            pb.put(rgba);
            pb.position(0);
            texture.draw(pixmap, 0, 0);
            lastUploadedSeq = seq;
            if (seq % 30 == 0) {
                System.out.println("[Sanity] UPLOADED frame " + seq
                    + " (render fps=" + Gdx.graphics.getFramesPerSecond() + ")");
            }
        }

        // Flip vertically (V 1..0): our RGBA buffer is top-left origin, GDX textures bottom-left.
        batch.begin();
        batch.draw(texture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0f, 1f, 1f, 0f);
        batch.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        decoding = false;
        if (decodeThread != null) {
            try { decodeThread.join(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        if (texture != null) texture.dispose();
        if (pixmap != null) pixmap.dispose();
        if (batch != null) batch.dispose();
    }

    public static void main(String[] args) {
        String video = args.length > 0 ? args[0] : DEFAULT_VIDEO;

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Video Sanity Check");
        config.setWindowedMode(960, 540);
        config.useVsync(true);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new VideoSanityCheck(video), config);
    }
}
