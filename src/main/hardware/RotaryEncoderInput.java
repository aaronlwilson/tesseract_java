package hardware;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

/**
 * Reads the TESSERACT rotary encoder's position over USB serial.
 * <p>
 * Wiring: the encoder is read on the Arduino Mega's GPIO pins; the Mega streams the running
 * pulse count to Java over USB serial as newline-terminated ASCII integers (0-2047 per
 * revolution). This mirrors the wire protocol of the old Processing-based TesseractMain, which
 * used Processing's {@code Serial} library - only the port library changed.
 */
public class RotaryEncoderInput {

    private static final int BAUD_RATE = 115200;
    private static final int PULSES_PER_REVOLUTION = 2048;

    private SerialPort port;
    private volatile double angleRadians = 0;

    // Accumulates bytes between newlines. Only touched from the jSerialComm listener thread.
    private final StringBuilder lineBuffer = new StringBuilder();

    public RotaryEncoderInput(String portName) {
        if (portName == null || portName.isBlank()) {
            System.out.println("[RotaryEncoderInput] No rotaryEncoderPort configured, skipping.");
            return;
        }

        port = SerialPort.getCommPort(portName);
        port.setBaudRate(BAUD_RATE);

        if (!port.openPort()) {
            System.err.println("[RotaryEncoderInput] Failed to open serial port '" + portName + "'");
            port = null;
            return;
        }

        System.out.println("[RotaryEncoderInput] Listening on " + portName + " @ " + BAUD_RATE + " baud");

        // Read raw bytes directly off the port in the event callback rather than layering a
        // blocking InputStream/BufferedReader on top - mixing those with the event listener is a
        // known-shaky jSerialComm combination that throws spurious "read operation timed out"
        // errors under TIMEOUT_NONBLOCKING.
        port.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                byte[] buffer = new byte[port.bytesAvailable()];
                int numRead = port.readBytes(buffer, buffer.length);
                for (int i = 0; i < numRead; i++) {
                    char c = (char) buffer[i];
                    if (c == '\n') {
                        handleLine(lineBuffer.toString());
                        lineBuffer.setLength(0);
                    } else if (c != '\r') {
                        lineBuffer.append(c);
                    }
                }
            }
        });
    }

    private void handleLine(String line) {
        line = line.trim();
        if (line.isEmpty()) return;
        try {
            int pulses = Integer.parseInt(line);
            angleRadians = pulses * (2 * Math.PI / PULSES_PER_REVOLUTION);
        } catch (NumberFormatException e) {
            // Ignore malformed/partial lines (e.g. right after opening the port mid-stream)
        }
    }

    /**
     * Latest encoder angle in degrees. Safe to poll every frame from the render thread - updated
     * asynchronously off the serial listener thread.
     */
    public float getAngleDegrees() {
        return (float) Math.toDegrees(angleRadians);
    }

    public boolean isConnected() {
        return port != null && port.isOpen();
    }

    public void dispose() {
        if (port != null && port.isOpen()) {
            port.closePort();
        }
    }
}
