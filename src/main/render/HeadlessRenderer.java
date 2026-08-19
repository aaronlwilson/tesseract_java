package render;

import environment.Node;

/**
 * No-op renderer for headless mode (Raspberry Pi, servers).
 * All rendering methods do nothing - LED output happens via UDP regardless.
 */
public class HeadlessRenderer implements IRenderer {

    private int width;
    private int height;
    private long frameCount = 0;
    private long lastFrameTime = System.nanoTime();
    private float frameRate = 30.0f;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
        System.out.println("HeadlessRenderer initialized (" + width + "x" + height + ")");
    }

    @Override
    public void beginFrame() {
        frameCount++;
    }

    @Override
    public void endFrame() {
        // Calculate frame rate
        long now = System.nanoTime();
        float deltaSeconds = (now - lastFrameTime) / 1_000_000_000.0f;
        if (deltaSeconds > 0) {
            frameRate = 1.0f / deltaSeconds;
        }
        lastFrameTime = now;
    }

    @Override
    public void drawNode(Node node) {
        // No-op in headless mode
    }

    @Override
    public void drawNodes(Node[] nodes) {
        // No-op in headless mode
    }

    @Override
    public void drawLine(float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b) {
        // No-op in headless mode
    }

    @Override
    public void drawBox(float x, float y, float z, float width, float height, float depth, int r, int g, int b) {
        // No-op in headless mode
    }

    @Override
    public void drawText(String text, float x, float y, int r, int g, int b) {
        // No-op in headless mode
    }

    @Override
    public void setCameraRotation(float xRot, float yRot) {
        // No-op in headless mode
    }

    @Override
    public void setCameraZoom(float zoom) {
        // No-op in headless mode
    }

    @Override
    public void setNodeRadius(float radius) {
        // No-op in headless mode
    }

    @Override
    public float getFrameRate() {
        return frameRate;
    }

    @Override
    public boolean isMousePressed() {
        return false;
    }

    @Override
    public int getMouseX() {
        return 0;
    }

    @Override
    public int getMouseY() {
        return 0;
    }

    @Override
    public float screenX(float x, float y, float z) {
        // Simple orthographic projection for headless mode
        // This ensures Perlin noise calculations still work
        return x + width / 2.0f;
    }

    @Override
    public float screenY(float x, float y, float z) {
        // Simple orthographic projection for headless mode
        return y + height / 2.0f;
    }

    @Override
    public void dispose() {
        System.out.println("HeadlessRenderer disposed");
    }

    @Override
    public boolean isHeadless() {
        return true;
    }
}
