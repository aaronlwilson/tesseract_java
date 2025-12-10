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
 * - Linux x86_64
 * - Linux ARM64 (Raspberry Pi 4 64-bit)
 * - Linux ARM (Raspberry Pi 32-bit)
 */
public class JavaCVVideoClip extends AbstractClip {

    // Threshold for black level (same as VideoClip)
    private float _pThreshold;

    // Video dimensions
    private int _videoW = 640;
    private int _videoH = 360;

    // JavaCV components
    private FFmpegFrameGrabber grabber;
    private Java2DFrameConverter converter;

    // Current frame pixels (ARGB format, same as Processing)
    private int[] pixels;

    // Video playback state
    private boolean playing = false;
    private boolean looping = true;

    // Frame timing for smooth playback
    private long lastFrameTime = 0;
    private double frameInterval = 1000.0 / 30.0; // Default 30fps

    // We have to define the field here too (same as VideoClip)
    public filename;

    public JavaCVVideoClip() {
    }

    public void init() {
        clipId = "video";
        super.init();

        converter = new Java2DFrameConverter();
        pixels = new int[_videoW * _videoH];
    }

    public void setFilename(String filename) {
        if (!MediaStore.get().containsMedia("videos", filename)) {
            System.out.println("[JavaCVVideoClip] Warning: Tried to set non-existent mediafile of type 'video' and filename '" + filename + "'");
            return;
        }

        this.filename = filename;

        // Stop current video if playing
        if (grabber != null) {
            stopPlayback();
        }

        try {
            // Build path to video file (same location as VideoClip)
            String videoPath = Paths.get("data", "videos", filename).toString();

            grabber = new FFmpegFrameGrabber(videoPath);
            grabber.start();

            // Get actual video dimensions
            _videoW = grabber.getImageWidth();
            _videoH = grabber.getImageHeight();

            // Calculate frame interval based on video framerate
            double fps = grabber.getFrameRate();
            if (fps > 0) {
                frameInterval = 1000.0 / fps;
            }

            // Resize pixel buffer
            pixels = new int[_videoW * _videoH];

            playing = true;
            lastFrameTime = System.currentTimeMillis();

            System.out.println("[JavaCVVideoClip] Loaded video: " + filename + " (" + _videoW + "x" + _videoH + " @ " + fps + " fps)");

        } catch (Exception e) {
            System.err.println("[JavaCVVideoClip] Error loading video: " + filename);
            e.printStackTrace();
            grabber = null;
        }
    }

    public void run() {
        _pThreshold = p1 * 255.0f;

        if (grabber == null || !playing) {
            return;
        }

        // Check if it's time for a new frame
        long now = System.currentTimeMillis();
        if (now - lastFrameTime < frameInterval) {
            return;
        }
        lastFrameTime = now;

        try {
            Frame frame = grabber.grabImage();

            // Handle end of video
            if (frame == null) {
                if (looping) {
                    // Restart from beginning
                    grabber.setFrameNumber(0);
                    frame = grabber.grabImage();
                } else {
                    playing = false;
                    return;
                }
            }

            if (frame != null && frame.image != null) {
                // Convert frame to BufferedImage
                BufferedImage img = converter.convert(frame);

                if (img != null) {
                    // Ensure image is in ARGB format for direct pixel access
                    BufferedImage argbImage;
                    if (img.getType() == BufferedImage.TYPE_INT_ARGB ||
                        img.getType() == BufferedImage.TYPE_INT_RGB) {
                        argbImage = img;
                    } else {
                        // Convert to ARGB
                        argbImage = new BufferedImage(_videoW, _videoH, BufferedImage.TYPE_INT_ARGB);
                        argbImage.getGraphics().drawImage(img, 0, 0, null);
                    }

                    // Get pixels directly from the data buffer
                    int[] imgPixels = ((DataBufferInt) argbImage.getRaster().getDataBuffer()).getData();
                    System.arraycopy(imgPixels, 0, pixels, 0, Math.min(imgPixels.length, pixels.length));
                }
            }

        } catch (Exception e) {
            System.err.println("[JavaCVVideoClip] Error grabbing frame");
            e.printStackTrace();
        }
    }

    public int[] drawNode(Node node) {
        int[] nodestate = new int[3];

        // Map screen coordinates to video coordinates (same logic as VideoClip)
        int vidX = (int) _myMain.map(node.screenX, 0, 1400, 0, _videoW - 1);
        int vidY = (int) _myMain.map(node.screenY, 0, 800, 0, _videoH - 1);

        // Calculate pixel location in 1D array
        int loc = vidX + vidY * _videoW;

        int c = 0;
        if (pixels != null && loc >= 0 && loc < pixels.length) {
            c = pixels[loc];
        }

        // Extract RGB components
        int r = Util.getR(c);
        int g = Util.getG(c);
        int b = Util.getB(c);

        // Apply threshold (same as VideoClip)
        if (r < _pThreshold) r = 0;
        if (g < _pThreshold) g = 0;
        if (b < _pThreshold) b = 0;

        nodestate[0] = r;
        nodestate[1] = g;
        nodestate[2] = b;

        return nodestate;
    }

    private void stopPlayback() {
        playing = false;
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
                System.err.println("[JavaCVVideoClip] Error stopping playback");
                e.printStackTrace();
            }
            grabber = null;
        }
    }

    public void die() {
        stopPlayback();
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
        return this.playing;
    }
}
