package render;

import environment.Node;

/**
 * Abstraction layer for rendering.
 * Allows swapping between LibGDX (headed) and headless implementations.
 */
public interface IRenderer {

    /**
     * Initialize the renderer. Called once at startup.
     */
    void init(int width, int height);

    /**
     * Called at the start of each frame.
     */
    void beginFrame();

    /**
     * Called at the end of each frame.
     */
    void endFrame();

    /**
     * Draw a single node (LED point) at its position with its current color.
     */
    void drawNode(Node node);

    /**
     * Draw all nodes in a batch for efficiency.
     */
    void drawNodes(Node[] nodes);

    /**
     * Draw a 3D line between two points.
     */
    void drawLine(float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b);

    /**
     * Draw a 3D bounding box (wireframe).
     */
    void drawBox(float x, float y, float z, float width, float height, float depth, int r, int g, int b);

    /**
     * Draw text on screen (2D overlay).
     */
    void drawText(String text, float x, float y, int r, int g, int b);

    /**
     * Set the camera rotation based on mouse drag.
     */
    void setCameraRotation(float xRot, float yRot);

    /**
     * Set the camera zoom (OrthographicCamera.zoom convention: &lt;1 zooms in, &gt;1 zooms out).
     */
    void setCameraZoom(float zoom);

    /**
     * Set the on-screen radius (in pixels) of each LED node dot.
     */
    void setNodeRadius(float radius);

    /**
     * Get the current frame rate.
     */
    float getFrameRate();

    /**
     * Check if mouse is currently pressed.
     */
    boolean isMousePressed();

    /**
     * Get current mouse X position.
     */
    int getMouseX();

    /**
     * Get current mouse Y position.
     */
    int getMouseY();

    /**
     * Get the screen X projection of a 3D point (for Perlin noise calculations).
     */
    float screenX(float x, float y, float z);

    /**
     * Get the screen Y projection of a 3D point (for Perlin noise calculations).
     */
    float screenY(float x, float y, float z);

    /**
     * Cleanup resources. Called on shutdown.
     */
    void dispose();

    /**
     * Check if this is a headless renderer (no actual display).
     */
    boolean isHeadless();
}
