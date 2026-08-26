package app;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

import environment.Node;
import environment.Stage;
import model.Channel;
import output.UDPModel;
import render.IRenderer;
import render.LibGDXRenderer;
import render.HeadlessRenderer;
import render.ProcessingCompat;
import show.Playlist;
import show.PlaylistManager;
import stores.ConfigStore;
import stores.PlaylistStore;
import stores.SceneStore;
import util.Util;
import websocket.WebsocketInterface;
import clip.Particle;
import render.Vec3;

import java.util.List;

/**
 * Main Tesseract application using LibGDX.
 * Replaces TesseractMain (which extended Processing's PApplet).
 */
public class TesseractApp implements ApplicationListener, InputProcessor {

    // Singleton instance
    private static TesseractApp instance;

    // Rendering
    private IRenderer renderer;
    private boolean headless;
    private int width;
    private int height;

    // Particles (public for clip access - matches original TesseractMain)
    public Particle particleX;
    public Particle particleY;
    public Particle particleZ;
    public Particle particleSpin;

    // Mapping-tool selection (used by StrandMapTest; adjusted via arrow keys)
    public int mapController = 1;
    public int mapPort = 1;

    // Clip class constants (matches original TesseractMain)
    public static final int NODESCAN = 0;
    public static final int SOLID = 1;
    public static final int COLORWASH = 2;
    public static final int VIDEO = 3;
    public static final int PARTICLE = 4;
    public static final int PERLINNOISE = 5;
    public static final int LINESCLIP = 6;
    public static final int TILESTEST = 7;
    public static final int STRANDMAPTEST = 8;

    // Core components
    public UDPModel udpModel;
    public Stage stage;
    public Channel channel1;
    public Boolean setupComplete = false;
    public Boolean drawing = true;
    public Boolean sending = true;

    // Camera rotation state (for mouse-controlled viewing)
    private float xRot = 0;
    private float yRot = 0;
    private float newXrot = 0;
    private float newYrot = 0;
    private float xStart = 0;
    private float yStart = 0;
    private float xDelta = 0;
    private float yDelta = 0;
    private float xMove = 0;
    private float yMove = 0;
    private boolean mousePressed = false;

    // Camera zoom state (space + drag to zoom, shift+space to reset). Same start/committed/eased
    // pattern as rotation above, but driven off vertical drag while spaceHeld is true instead of
    // always-on horizontal/vertical drag.
    private static final float ZOOM_SENSITIVITY = 0.005f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 4f;
    private boolean spaceHeld = false;
    private float zoom = 1f;
    private float newZoom = 1f;
    private float zoomStartY = 0;
    private float zoomMove = 1f;

    // Node dot size (Q/A keys) - a depth-perception aid: bigger dots make it easier to tell which
    // face is front vs. back when zoomed into an orthographic projection.
    private static final float NODE_RADIUS_MIN = 0.5f;
    private static final float NODE_RADIUS_MAX = 12f;
    private static final float NODE_RADIUS_STEP = 0.5f;
    private float nodeRadius = 2f;

    public TesseractApp(boolean headless, int width, int height) {
        this.headless = headless;
        this.width = width;
        this.height = height;
        instance = this;
    }

    public static TesseractApp get() {
        return instance;
    }

    // For backwards compatibility with code expecting TesseractMain
    public static TesseractApp getMain() {
        return instance;
    }

    @Override
    public void create() {
        // Initialize renderer
        if (headless) {
            renderer = new HeadlessRenderer();
        } else {
            renderer = new LibGDXRenderer();
            // Set up input processing
            Gdx.input.setInputProcessor(this);
        }
        renderer.init(width, height);
        renderer.setNodeRadius(nodeRadius);

        // Initialize particles with default values
        particleX = new Particle(new Vec3(0, 0, 0), 0xFF0000, 100, 100, new Vec3(0, 0, 0), new Vec3(0, 0, 0));
        particleY = new Particle(new Vec3(0, 0, 0), 0x00FF00, 100, 100, new Vec3(0, 0, 0), new Vec3(0, 0, 0));
        particleZ = new Particle(new Vec3(0, 0, 0), 0x0000FF, 100, 100, new Vec3(0, 0, 0), new Vec3(0, 0, 0));
        particleSpin = new Particle(new Vec3(0, 0, 0), 0xFF00FF, 100, 100, new Vec3(0, 0, 0), new Vec3(0, 0, 0));

        Util.enableColorization();

        // Start listening for UDP messages
        udpModel = new UDPModel();

        // The stage is the LED mapping
        stage = new Stage();

        // Create channel
        channel1 = new Channel(1);

        // Complete configuration in separate thread (matches original)
        new Thread(this::completeConfiguration).start();
    }

    private void completeConfiguration() {
        // Configure Data and Stores
        Util.createBuiltInScenes();
        Util.createBuiltInPlaylists();

        // Save the default data
        SceneStore.get().saveDataToDisk();
        PlaylistStore.get().saveDataToDisk();

        // Load configuration from file
        ConfigStore.get();

        // Initialize websocket connection
        WebsocketInterface.get();

        // Initialize the StateManager now so its inbound handlers (requestInitialState,
        // stateUpdate) are registered before any client connects — not lazily on first play/stop.
        state.StateManager.get();

        // Get the configured stage value
        String stageType = ConfigStore.get().getString("stageType");

        // Build the stage
        stage.buildStage(stageType);

        // Tell the PlaylistManager which channel to play playlists in
        PlaylistManager.get().setChannel(this.channel1);

        // Get initial playlist & playState from config. 'initialPlaylist' is validated against the
        // live PlaylistStore by displayName, so a playlist renamed via the UI since this config was
        // last set makes the configured name stale — don't let that crash this thread and leave the
        // app stuck with setupComplete=false (render() is a no-op until it's true) forever.
        Playlist initialPlaylist;
        try {
            initialPlaylist = PlaylistStore.get().find("displayName", ConfigStore.get().getString("initialPlaylist"));
        } catch (RuntimeException e) {
            List<Playlist> allPlaylists = PlaylistStore.get().getItems();
            initialPlaylist = allPlaylists.isEmpty() ? null : allPlaylists.get(0);
            System.err.println("[TesseractApp] WARNING: " + e.getMessage() + ". Falling back to "
                    + (initialPlaylist != null ? "'" + initialPlaylist.getDisplayName() + "'" : "no playlist (none exist)") + ".");
        }
        Playlist.PlayState initialPlayState = Util.getPlayState(ConfigStore.get().getString("initialPlayState"));

        // Play the playlist with the playState defined in our configuration
        if (initialPlaylist != null) {
            PlaylistManager.get().play(initialPlaylist.getId(), null, initialPlayState);
        }

        // Create shutdown hook
        createShutdownHook();

        setupComplete = true;
    }

    @Override
    public void render() {
        // Wait for configuration to complete
        if (!setupComplete) return;

        // Run clips
        channel1.run();

        // Get the full list of hardware nodes
        int l = stage.nodes.length;
        Node[] nextNodes = stage.nodes;
        stage.prevNodes = stage.nodes;

        for (int i = 0; i < l; i++) {
            Node n = nextNodes[i];
            int[] rgb = renderNode(n);

            // Store color on the node for UDP output
            n.r = rgb[0];
            n.g = rgb[1];
            n.b = rgb[2];

            nextNodes[i] = n;
        }

        stage.nodes = nextNodes;

        // Draw visualization
        if (drawing && !renderer.isHeadless()) {
            drawVisualization();
        }

        // Send UDP data to LEDs
        if (sending) {
            // Diagnostic timing: udpModel.send() is called synchronously on this (render) thread every
            // frame. If it's slow on a real network (vs. the quiet dev LAN this was last profiled on),
            // it eats into the same frame budget the video decode thread needs to stay warmed up, even
            // if the FPS counter (paced by vsync/foregroundFPS) doesn't visibly drop. Logged periodically,
            // not every frame, so it's cheap to leave in.
            long udpSendStartNs = System.nanoTime();
            udpModel.send();
            long udpSendNs = System.nanoTime() - udpSendStartNs;
            udpSendTimeAccumNs += udpSendNs;
            udpSendCountAccum++;
            long nowMs = System.currentTimeMillis();
            if (nowMs - lastUdpTimingLogMs > 2000) {
                double avgMs = (udpSendTimeAccumNs / (double) udpSendCountAccum) / 1e6;
               // System.out.println("[UDP] send() avg " + String.format("%.2f", avgMs) + "ms/frame over " + udpSendCountAccum + " frames");
                udpSendTimeAccumNs = 0;
                udpSendCountAccum = 0;
                lastUdpTimingLogMs = nowMs;
            }
        }
    }

    // See the diagnostic-timing block in render() above.
    private long udpSendTimeAccumNs = 0;
    private int udpSendCountAccum = 0;
    private long lastUdpTimingLogMs = 0;

    private void drawVisualization() {
        renderer.beginFrame();

        // Draw framerate
        renderer.drawText("FPS " + Math.floor(renderer.getFrameRate()), width - 60, 20, 160, 160, 160);

        if (sending) {
            renderer.drawText("SENDING", width - 110, 40, 160, 160, 160);
        } else {
            renderer.drawText("NOT SENDING", width - 110, 40, 160, 160, 160);
        }

        if (!drawing) {
            renderer.drawText("NOT DRAWING", width - 110, 70, 160, 160, 160);
            renderer.endFrame();
            return;
        }

        // Update camera rotation/zoom with mouse
        updateCameraRotation();
        updateCameraZoom();
        renderer.setCameraRotation(xRot, yRot);
        renderer.setCameraZoom(zoom);

        // TESSERACT rests spinning on one corner rather than sitting face-on; tilt the base pose
        // to match. (stage.stageType is set async in buildStage(), hence the null-safe check.)
        if ("TESSERACT".equals(stage.stageType)) {
            renderer.setCornerTilt(45f, 0f, 35.3f);
        }

        // Draw axes
        drawAxes(600);

        // Draw particles
        renderer.drawParticle(createNodeFromParticle(particleX));
        renderer.drawParticle(createNodeFromParticle(particleY));
        renderer.drawParticle(createNodeFromParticle(particleZ));

        // Draw bounding box
        float boxX = stage.minX + (stage.maxW / 2);
        float boxY = stage.minY + (stage.maxH / 2);
        float boxZ = stage.minZ + (stage.maxD / 2);
        renderer.drawBox(boxX, boxY, boxZ, stage.maxW, stage.maxH, stage.maxD, 60, 60, 60);

        // Draw all nodes
        renderer.drawNodes(stage.nodes);

        renderer.endFrame();
    }

    private Node createNodeFromParticle(Particle p) {
        Node n = new Node();
        n.x = p.position.x;
        n.y = p.position.y;
        n.z = p.position.z;
        n.r = (p.color >> 16) & 0xFF;
        n.g = (p.color >> 8) & 0xFF;
        n.b = p.color & 0xFF;
        return n;
    }

    private void updateCameraRotation() {
        // While spaceHeld, drags drive zoom (see updateCameraZoom) instead of rotation.
        if (mousePressed && !spaceHeld) {
            xDelta = xStart - renderer.getMouseX();
            yDelta = yStart - renderer.getMouseY();
        } else {
            xDelta = 0;
            yDelta = 0;
        }

        newXrot = xMove - xDelta;
        newYrot = yMove - yDelta;

        // Easing
        float diff = xRot - newXrot;
        if (Math.abs(diff) > 0.01) {
            xRot -= diff / 6.0;
        }

        diff = yRot - newYrot;
        if (Math.abs(diff) > 0.01) {
            yRot -= diff / 6.0;
        }
    }

    private void updateCameraZoom() {
        if (mousePressed && spaceHeld) {
            // Drag up (screen Y decreases) zooms in; drag down zooms out.
            float dragDeltaY = renderer.getMouseY() - zoomStartY;
            newZoom = zoomMove + dragDeltaY * ZOOM_SENSITIVITY;
            newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));
        }

        // Easing, same pattern as rotation
        float diff = zoom - newZoom;
        if (Math.abs(diff) > 0.001) {
            zoom -= diff / 6.0;
        }
    }

    // Called when a space+drag zoom gesture starts (space pressed while already dragging, or a
    // new drag starting while space is already held) so the drag baseline is captured fresh.
    private void beginZoomDrag() {
        zoomStartY = renderer.getMouseY();
        zoomMove = newZoom;
    }

    private void resetZoom() {
        zoom = 1f;
        newZoom = 1f;
        zoomMove = 1f;
        zoomStartY = renderer.getMouseY();
        System.out.println("Zoom reset to default");
    }

    private void drawAxes(float size) {
        // X - red
        renderer.drawLine(0, 0, 0, size, 0, 0, 220, 0, 0);
        // Y - green
        renderer.drawLine(0, 0, 0, 0, size, 0, 0, 220, 0);
        // Z - blue
        renderer.drawLine(0, 0, 0, 0, 0, size, 0, 0, 220);
    }

    private int[] renderNode(Node node) {
        int[] rgb1 = channel1.drawNode(node);

        // Apply channel brightness
        rgb1[0] = (int) Math.round(rgb1[0] * 0.9);
        rgb1[1] = (int) Math.round(rgb1[1] * 0.9);
        rgb1[2] = (int) Math.round(rgb1[2] * 0.9);

        return rgb1;
    }

    private void createShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Only non-GL cleanup here. This runs on the shutdown-hook thread, and disposing the
            // renderer touches OpenGL (glDelete* via SpriteBatch/ShapeRenderer). On macOS the GL
            // context lives on the -XstartOnFirstThread main thread, so a GL call from any other
            // thread aborts the process (SIGABRT) — which is exactly the crash report on quit.
            // GL resources are freed by LibGDX on the main thread in dispose() during a normal
            // window close, and reclaimed by the OS on a signal-driven exit.
            WebsocketInterface.get().shutdownServer();
        }));
    }

    // ===== Processing compatibility methods =====
    // These allow existing code to work without modification

    public int color(int r, int g, int b) {
        return ProcessingCompat.color(r, g, b);
    }

    public float noise(float x, float y, float z) {
        return ProcessingCompat.noise(x, y, z);
    }

    public void noiseDetail(int lod, float falloff) {
        ProcessingCompat.noiseDetail(lod, falloff);
    }

    public float map(float value, float start1, float stop1, float start2, float stop2) {
        return ProcessingCompat.map(value, start1, stop1, start2, stop2);
    }

    public float screenX(float x, float y, float z) {
        return renderer.screenX(x, y, z);
    }

    public float screenY(float x, float y, float z) {
        return renderer.screenY(x, y, z);
    }

    // ===== LibGDX ApplicationListener methods =====

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        if (renderer != null) {
            renderer.dispose();
        }
    }

    // ===== InputProcessor methods =====

    @Override
    public boolean keyDown(int keycode) {
        // Space held while dragging zooms the viewport instead of rotating it; shift+space
        // (either press order) resets zoom to default. See updateCameraZoom()/touchDown().
        if (keycode == Input.Keys.SPACE) {
            boolean shiftHeld = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
            if (shiftHeld) {
                resetZoom();
            } else if (!spaceHeld) {
                spaceHeld = true;
                if (mousePressed) {
                    // Switch an in-progress rotate-drag into a zoom-drag without jumping.
                    xMove = xMove - xDelta;
                    yMove = yMove - yDelta;
                    beginZoomDrag();
                }
            }
            return true;
        } else if (keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT) {
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                resetZoom();
                return true;
            }
            return false;
        } else if (keycode == Input.Keys.Q) {
            nodeRadius = Math.min(NODE_RADIUS_MAX, nodeRadius + NODE_RADIUS_STEP);
            renderer.setNodeRadius(nodeRadius);
            return true;
        } else if (keycode == Input.Keys.A) {
            nodeRadius = Math.max(NODE_RADIUS_MIN, nodeRadius - NODE_RADIUS_STEP);
            renderer.setNodeRadius(nodeRadius);
            return true;
        }

        // Arrow keys drive the mapping-tool selection (see StrandMapTest).
        // UP/DOWN cycle the controller (1..4), LEFT/RIGHT cycle the pin (1..8).
        if (keycode == Input.Keys.UP) {
            mapPort = 1;
            mapController++;
            if (mapController > 4) mapController = 1;
            System.out.println("mapController:" + mapController);
        } else if (keycode == Input.Keys.DOWN) {
            mapPort = 1;
            mapController--;
            if (mapController < 1) mapController = 4;
            System.out.println("mapController:" + mapController);
        } else if (keycode == Input.Keys.RIGHT) {
            mapPort++;
            if (mapPort > 8) mapPort = 1;
            System.out.println("mapPort:" + mapPort);
        } else if (keycode == Input.Keys.LEFT) {
            mapPort--;
            if (mapPort < 1) mapPort = 8;
            System.out.println("mapPort:" + mapPort);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.SPACE) {
            spaceHeld = false;
            if (mousePressed) {
                // Switch back to a rotate-drag from the current mouse position, no jump.
                zoomMove = newZoom;
                xStart = renderer.getMouseX();
                yStart = renderer.getMouseY();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        System.out.println(character);
        if (character == 's') {
            sending = !sending;
        }
        if (character == 'd') {
            drawing = !drawing;
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        mousePressed = true;
        if (spaceHeld) {
            beginZoomDrag();
        } else {
            xStart = screenX;
            yStart = screenY;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        mousePressed = false;
        if (spaceHeld) {
            zoomMove = newZoom;
        } else {
            xMove = xMove - xDelta;
            yMove = yMove - yDelta;
        }
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
