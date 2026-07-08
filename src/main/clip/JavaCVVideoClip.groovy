package clip;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import environment.Node;
import stores.MediaStore;
import util.Util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.file.Paths;

/**
 * JavaCV-based video clip that replaces the Processing Video library.
 * Uses FFmpeg via JavaCV for cross-platform video playback including:
 * - macOS ARM64 (Apple Silicon)
 * - macOS x86_64 (Intel)
 * - Linux x86_64 / ARM64 (Raspberry Pi 4 64-bit) / ARM (Pi 32-bit)
 *
 * Decoding runs on a dedicated background thread so slow FFmpeg grab + software colorspace
 * conversion never stalls the render thread, and so the (non-thread-safe) grabber is only ever
 * touched by one thread.
 *
 * The decode thread is driven by run(): the channel only calls run() on the *active* clip, so we
 * start decoding the first time run() is called and let the thread self-terminate once run() stops
 * being called (scene switched away). This keeps exactly one video decoding at a time, even though
 * every video Scene constructs its own clip and sets a filename up front.
 */
public class JavaCVVideoClip extends AbstractClip {

    // Threshold for black level (same as the original VideoClip)
    private float _pThreshold;

    // Stop decoding this many ms after run() was last called (i.e. after this clip goes inactive).
    private static final long INACTIVE_TIMEOUT_MS = 500;

    // An immutable snapshot of the most recently decoded frame, published atomically by the decode
    // thread (single volatile write) and read by the render thread. Bundling pixels + dimensions
    // avoids any size/buffer mismatch across a resolution change.
    private static final class VideoFrame {
        final int[] px;
        final int w;
        final int h;
        VideoFrame(int[] px, int w, int h) { this.px = px; this.w = w; this.h = h; }
    }

    private volatile VideoFrame currentFrame = null;

    // Decode thread state
    private volatile boolean decoding = false;
    private volatile long lastRunMs = 0;        // updated by run(); the decode thread's "is this clip still active?" heartbeat
    private volatile String decodingFilename = null; // the file the current decode thread is playing
    private Thread decodeThread = null;

    private boolean looping = true;

    // NOTE: the desired filename lives in the inherited AbstractClip.filename field. We deliberately
    // do NOT redeclare it here — a shadowing field would be set by setFilename() but leave the
    // parent field null, so getClipControlValues() (which reads via getFilename()) would report the
    // default. run() (re)starts the decode thread to match this.filename.

    public JavaCVVideoClip() {
    }

    public void init() {
        clipId = "video";
        super.init();
    }

    // Called from scene construction (for every video scene) and from the live picker. We only
    // record the desired file here — run() actually (re)starts decoding, so inactive scenes whose
    // run() is never called don't spin up a decoder.
    public void setFilename(String filename) {
        if (!MediaStore.get().containsMedia("videos", filename)) {
            System.out.println("[JavaCVVideoClip] Warning: Tried to set non-existent mediafile of type 'video' and filename '" + filename + "'");
            return;
        }
        // Set the inherited AbstractClip.filename via the parent (Java) setter. Assigning
        // this.filename directly in Groovy would resolve to this overridden setter and recurse.
        super.setFilename(filename);
    }

    public void run() {
        // Only cheap work on the render thread — decoding happens on the decode thread.
        _pThreshold = p1 * 255.0f;

        // This clip is the active one (the channel called run()); keep the heartbeat fresh and make
        // sure the decode thread is running the currently-desired file.
        lastRunMs = System.currentTimeMillis();
        ensureDecoding();
    }

    private synchronized void ensureDecoding() {
        if (this.filename == null) {
            return;
        }
        boolean alive = decodeThread != null && decodeThread.isAlive();
        if (!alive || !this.filename.equals(decodingFilename)) {
            startDecoding((String) this.filename);
        }
    }

    private synchronized void startDecoding(final String name) {
        stopDecoding();
        currentFrame = null;
        decodingFilename = name;
        decoding = true;
        decodeThread = new Thread(new Runnable() {
            public void run() { decodeLoop(name); }
        });
        decodeThread.setName("video-decode");
        decodeThread.setDaemon(true);
        decodeThread.start();
    }

    private synchronized void stopDecoding() {
        decoding = false;
        if (decodeThread != null) {
            try {
                decodeThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            decodeThread = null;
        }
    }

    // Runs on the background decode thread; the grabber is created, used, and released here only.
    private void decodeLoop(String name) {
        FFmpegFrameGrabber grabber = null;
        Java2DFrameConverter converter = new Java2DFrameConverter();
        try {
            String videoPath = Paths.get("data", "videos", name).toString();
            grabber = new FFmpegFrameGrabber(videoPath);
            grabber.start();

            int w = grabber.getImageWidth();
            int h = grabber.getImageHeight();
            double fps = grabber.getFrameRate();
            double frameInterval = fps > 0 ? 1000.0 / fps : 1000.0 / 30.0;

            System.out.println("[JavaCVVideoClip] Loaded video: " + name + " (" + w + "x" + h + " @ " + fps + " fps)");

            while (decoding) {
                // Self-terminate once this clip is no longer the active one (run() stopped ticking).
                if (System.currentTimeMillis() - lastRunMs > INACTIVE_TIMEOUT_MS) {
                    break;
                }

                long startMs = System.currentTimeMillis();

                Frame frame = grabber.grabImage();

                // Handle end of video
                if (frame == null) {
                    if (looping) {
                        grabber.setFrameNumber(0);
                        frame = grabber.grabImage();
                    } else {
                        break;
                    }
                }

                if (frame != null && frame.image != null) {
                    BufferedImage img = converter.convert(frame);
                    if (img != null) {
                        currentFrame = new VideoFrame(toArgb(img, w, h), w, h);
                    }
                }

                // Pace to the video framerate without spinning the CPU
                long sleepMs = (long) frameInterval - (System.currentTimeMillis() - startMs);
                if (sleepMs > 0) {
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[JavaCVVideoClip] Decode error for '" + name + "': " + e);
            e.printStackTrace();
        } finally {
            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.release();
                } catch (Exception e) {
                    // ignore — we're tearing down
                }
            }
            // Allow a restart if this clip becomes active again.
            if (name.equals(decodingFilename)) {
                decodingFilename = null;
            }
        }
    }

    // Convert a decoded frame to a fresh ARGB int[] (same pixel semantics as Processing's pixels[])
    private static int[] toArgb(BufferedImage img, int w, int h) {
        BufferedImage argbImage;
        if (img.getType() == BufferedImage.TYPE_INT_ARGB || img.getType() == BufferedImage.TYPE_INT_RGB) {
            argbImage = img;
        } else {
            argbImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            argbImage.getGraphics().drawImage(img, 0, 0, null);
        }
        int[] src = ((DataBufferInt) argbImage.getRaster().getDataBuffer()).getData();
        int[] out = new int[w * h];
        System.arraycopy(src, 0, out, 0, Math.min(src.length, out.length));
        return out;
    }

    public int[] drawNode(Node node) {
        int[] nodestate = new int[3];

        VideoFrame f = currentFrame;
        if (f == null) {
            return nodestate; // black until the first frame is decoded
        }

        // Project the node's screen-space position onto the video frame. screenX/screenY are the
        // node's orthographic 2D projection, populated by the renderer.
        int vidX = (int) _myMain.map(node.screenX, 0, 1400, 0, f.w - 1);
        int vidY = (int) _myMain.map(node.screenY, 0, 800, 0, f.h - 1);

        int loc = vidX + vidY * f.w;

        int c = 0;
        if (loc >= 0 && loc < f.px.length) {
            c = f.px[loc];
        }

        int r = Util.getR(c);
        int g = Util.getG(c);
        int b = Util.getB(c);

        // Apply threshold (same as the original VideoClip)
        if (r < _pThreshold) r = 0;
        if (g < _pThreshold) g = 0;
        if (b < _pThreshold) b = 0;

        nodestate[0] = r;
        nodestate[1] = g;
        nodestate[2] = b;

        return nodestate;
    }

    public void die() {
        stopDecoding();
        currentFrame = null;
        super.die();
    }

    // Allow external control of looping
    public void setLooping(boolean loop) {
        this.looping = loop;
    }

    public boolean isLooping() {
        return this.looping;
    }

    public boolean isPlaying() {
        return this.decoding;
    }
}
