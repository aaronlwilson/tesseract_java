package render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import environment.Node;

import java.util.Arrays;

/**
 * LibGDX-based renderer for headed mode (desktop with display).
 * Replaces Processing's P3D renderer and JOGL dependency.
 */
public class LibGDXRenderer implements IRenderer {

    // On-screen radius (in pixels) of each LED dot. Nodes are billboarded, so
    // this is a constant screen size regardless of 3D depth (matches Processing's point()).
    // Adjustable at runtime (Q/A keys, see TesseractApp) as a depth-perception aid when zoomed in.
    private float nodeRadius = 2f;

    private int width;
    private int height;

    // LibGDX rendering components
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private OrthographicCamera camera;

    // Screen-space (2D) projection used to draw billboarded node dots at their
    // projected positions. Origin bottom-left, matching camera.project() output.
    private Matrix4 screenMatrix;

    // Camera rotation state (for mouse-controlled viewing)
    private float xRot = 0;
    private float yRot = 0;

    // For 3D to 2D projection
    private Matrix4 projectionMatrix;
    private Matrix4 transformMatrix;
    private Vector3 tempVec = new Vector3();

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        // Initialize LibGDX rendering components
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont(); // Default font

        // Setup orthographic camera (matches Processing's ortho())
        camera = new OrthographicCamera(width, height);
        camera.position.set(width / 2f, height / 2f, 0);
        // Wide near/far so the rotated 3D scene (axes, bounding box) is never
        // clipped. The default 0..100 range slices through the rotated geometry.
        camera.near = -10000;
        camera.far = 10000;
        camera.update();

        // Initialize matrices for 3D projection
        projectionMatrix = new Matrix4();
        transformMatrix = new Matrix4();

        // 2D screen-space projection for billboarded node dots
        screenMatrix = new Matrix4();
        screenMatrix.setToOrtho2D(0, 0, width, height);

        System.out.println("LibGDXRenderer initialized (" + width + "x" + height + ")");
    }

    @Override
    public void beginFrame() {
        // Clear screen with dark background (matches Processing's background(20))
        Gdx.gl.glClearColor(20/255f, 20/255f, 20/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Enable depth testing for 3D
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        // Update camera
        camera.update();

        // Setup transformation matrix for 3D rotation
        transformMatrix.idt();
        transformMatrix.translate(width / 2f, height / 2f, 0);

        // Apply camera rotation from mouse control
        // Map rotation similar to Processing's approach
        float xRotAngle = map(yRot, 0, height, 180, -180) + 180;
        float yRotAngle = map(xRot, 0, width, -180, 180) + 180;

        transformMatrix.rotate(1, 0, 0, xRotAngle);
        transformMatrix.rotate(0, 1, 0, yRotAngle);

        // Combine with projection
        projectionMatrix.set(camera.combined);
        projectionMatrix.mul(transformMatrix);
    }

    @Override
    public void endFrame() {
        // Disable depth testing for 2D overlay
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }

    @Override
    public void drawNode(Node node) {
        // Billboard pass: 2D dots at projected positions, painter's order.
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        shapeRenderer.setProjectionMatrix(screenMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawNodeBillboard(node);
        shapeRenderer.end();
    }

    @Override
    public void drawNodes(Node[] nodes) {
        if (nodes == null) return;

        // Billboard pass: project each node's true 3D (x,y,z) to screen space and draw a
        // constant-size 2D circle there. This reproduces Processing's point(): a flat sprite at
        // the correct 3D position, so all X/Y/Z layers are visible.
        //
        // GL_DEPTH_TEST stays off because these are flat screen-space circles, not real 3D
        // geometry - the z-buffer can't sort them for us. So instead we sort back-to-front
        // ourselves (painter's algorithm) using each node's projected depth, and draw in that
        // order. Without this, nodes just painted in whatever order `nodes` happens to be in
        // (e.g. cube-face build order), so a nearer face could get painted UNDER a farther one -
        // visible as a "hollow"/Escher-looking cube once nodes are big/zoomed enough to notice.
        int n = nodes.length;
        ensureDepthBuffers(n);

        for (int i = 0; i < n; i++) {
            Node node = nodes[i];
            tempVec.set(node.x, node.y, node.z);
            tempVec.mul(transformMatrix);
            camera.project(tempVec); // -> window coords, origin bottom-left, z = depth (0 near, 1 far)

            screenXBuf[i] = tempVec.x;
            screenYBuf[i] = tempVec.y;
            depthBuf[i] = tempVec.z;

            // Record projected position for clips (e.g. Perlin) in Processing's top-left convention.
            node.screenX = tempVec.x;
            node.screenY = height - tempVec.y;
        }

        // Farthest (largest depth) first, nearest (smallest depth) last so it ends up on top.
        Arrays.sort(drawIndices, 0, n, (a, b) -> Float.compare(depthBuf[b], depthBuf[a]));

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        shapeRenderer.setProjectionMatrix(screenMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < n; i++) {
            int idx = drawIndices[i];
            Node node = nodes[idx];
            shapeRenderer.setColor(node.r / 255f, node.g / 255f, node.b / 255f, 1);
            shapeRenderer.circle(screenXBuf[idx], screenYBuf[idx], nodeRadius);
        }

        shapeRenderer.end();
    }

    @Override
    public void drawParticle(Node node) {
        // Real 3D cube (via the full rotation/zoom projection, like drawBox), so it reads as a
        // solid marker distinct from the flat, constant-size node dot billboards.
        float half = PARTICLE_CUBE_SIZE / 2f;

        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(node.r / 255f, node.g / 255f, node.b / 255f, 1);
        shapeRenderer.box(node.x - half, node.y - half, node.z + half, PARTICLE_CUBE_SIZE, PARTICLE_CUBE_SIZE, PARTICLE_CUBE_SIZE);
        shapeRenderer.end();
    }

    // Cube edge length, in world units, for drawParticle() markers. Fixed rather than tied to
    // nodeRadius: this cube is real 3D geometry (goes through the zoom/rotation projection), so
    // it already grows/shrinks with zoom on its own, unlike the constant-screen-size node dots.
    private static final float PARTICLE_CUBE_SIZE = 8f;

    // Depth-sort scratch buffers for drawNodes(), reused/grown across frames to avoid per-frame
    // allocation. drawIndices holds boxed 0..n-1 once; only their ORDER changes each frame (a
    // re-sort of existing references), so steady-state draws allocate nothing here.
    private Integer[] drawIndices = new Integer[0];
    private float[] depthBuf = new float[0];
    private float[] screenXBuf = new float[0];
    private float[] screenYBuf = new float[0];

    private void ensureDepthBuffers(int n) {
        if (drawIndices.length < n) {
            int old = drawIndices.length;
            drawIndices = Arrays.copyOf(drawIndices, n);
            for (int i = old; i < n; i++) drawIndices[i] = i;
            depthBuf = new float[n];
            screenXBuf = new float[n];
            screenYBuf = new float[n];
        }
    }

    // Projects a node to screen space and draws its dot. Must be called between
    // shapeRenderer.begin()/end() with screenMatrix as the projection.
    private void drawNodeBillboard(Node node) {
        // Project true 3D position (x, y, z) through the rotation + camera.
        tempVec.set(node.x, node.y, node.z);
        tempVec.mul(transformMatrix);
        camera.project(tempVec); // -> window coords, origin bottom-left, z = depth

        shapeRenderer.setColor(node.r / 255f, node.g / 255f, node.b / 255f, 1);
        shapeRenderer.circle(tempVec.x, tempVec.y, nodeRadius);

        // Record projected position for clips (e.g. Perlin) in Processing's
        // top-left coordinate convention.
        node.screenX = tempVec.x;
        node.screenY = height - tempVec.y;
    }

    @Override
    public void drawLine(float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b) {
        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(r / 255f, g / 255f, b / 255f, 1);
        shapeRenderer.line(x1, y1, z1, x2, y2, z2);
        shapeRenderer.end();
    }

    @Override
    public void drawBox(float x, float y, float z, float boxWidth, float boxHeight, float boxDepth, int r, int g, int b) {
        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(r / 255f, g / 255f, b / 255f, 1);

        float hw = boxWidth / 2;
        float hh = boxHeight / 2;
        float hd = boxDepth / 2;

        // Front face
        shapeRenderer.line(x - hw, y - hh, z + hd, x + hw, y - hh, z + hd);
        shapeRenderer.line(x + hw, y - hh, z + hd, x + hw, y + hh, z + hd);
        shapeRenderer.line(x + hw, y + hh, z + hd, x - hw, y + hh, z + hd);
        shapeRenderer.line(x - hw, y + hh, z + hd, x - hw, y - hh, z + hd);

        // Back face
        shapeRenderer.line(x - hw, y - hh, z - hd, x + hw, y - hh, z - hd);
        shapeRenderer.line(x + hw, y - hh, z - hd, x + hw, y + hh, z - hd);
        shapeRenderer.line(x + hw, y + hh, z - hd, x - hw, y + hh, z - hd);
        shapeRenderer.line(x - hw, y + hh, z - hd, x - hw, y - hh, z - hd);

        // Connecting edges
        shapeRenderer.line(x - hw, y - hh, z - hd, x - hw, y - hh, z + hd);
        shapeRenderer.line(x + hw, y - hh, z - hd, x + hw, y - hh, z + hd);
        shapeRenderer.line(x + hw, y + hh, z - hd, x + hw, y + hh, z + hd);
        shapeRenderer.line(x - hw, y + hh, z - hd, x - hw, y + hh, z + hd);

        shapeRenderer.end();
    }

    @Override
    public void drawText(String text, float x, float y, int r, int g, int b) {
        // Fixed screen-space overlay: x/y are already literal pixel coordinates (e.g. "width -
        // 60, 20" for the FPS readout), so this must NOT go through the 3D camera (camera.combined
        // bakes in zoom/pan and made HUD text fly off-screen when zoomed). Use the same fixed
        // 2D ortho projection the node billboards use instead.
        spriteBatch.setProjectionMatrix(screenMatrix);
        spriteBatch.begin();
        font.setColor(r / 255f, g / 255f, b / 255f, 1);
        // Flip Y coordinate (LibGDX has Y=0 at bottom, Processing has Y=0 at top)
        font.draw(spriteBatch, text, x, height - y);
        spriteBatch.end();
    }

    @Override
    public void setCameraRotation(float xRot, float yRot) {
        this.xRot = xRot;
        this.yRot = yRot;
    }

    @Override
    public void setCameraZoom(float zoom) {
        camera.zoom = zoom;
    }

    @Override
    public void setNodeRadius(float radius) {
        this.nodeRadius = radius;
    }

    @Override
    public float getFrameRate() {
        return Gdx.graphics.getFramesPerSecond();
    }

    @Override
    public boolean isMousePressed() {
        return Gdx.input.isTouched();
    }

    @Override
    public int getMouseX() {
        return Gdx.input.getX();
    }

    @Override
    public int getMouseY() {
        return Gdx.input.getY();
    }

    @Override
    public float screenX(float x, float y, float z) {
        // Project 3D point to 2D screen coordinates
        tempVec.set(x, y, z);
        tempVec.mul(transformMatrix);
        camera.project(tempVec);
        return tempVec.x;
    }

    @Override
    public float screenY(float x, float y, float z) {
        // Project 3D point to 2D screen coordinates
        tempVec.set(x, y, z);
        tempVec.mul(transformMatrix);
        camera.project(tempVec);
        // Flip Y to match Processing's coordinate system
        return height - tempVec.y;
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (spriteBatch != null) spriteBatch.dispose();
        if (font != null) font.dispose();
        System.out.println("LibGDXRenderer disposed");
    }

    @Override
    public boolean isHeadless() {
        return false;
    }

    // Utility method matching Processing's map()
    private float map(float value, float start1, float stop1, float start2, float stop2) {
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }
}
