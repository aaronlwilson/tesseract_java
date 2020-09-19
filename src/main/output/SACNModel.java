package output;


//package heronarts.lx.output;

//import heronarts.lx.LX;
//import heronarts.lx.model.LXModel;



/**
 * Streaming ACN, also referred to as E1.31, is a standardized protocol for
 * streaming DMX data over ACN protocol. It's a fairly simple UDP-based wrapper
 * on 512 bytes of data with a 16-bit universe number.
 *
 * See: https://tsp.esta.org/tsp/documents/docs/ANSI_E1-31-2018.pdf
 */
public class SACNModel extends BufferDatagram {

  protected final static int OFFSET_DMX_DATA = 126;
  protected final static int OFFSET_SEQUENCE_NUMBER = 111;
  protected final static int OFFSET_UNIVERSE_NUMBER = 113;

  public final static int MAX_DATA_LENGTH = 512;

  private final static int DEFAULT_PORT = 5568;

  private final static int DEFAULT_UNIVERSE_NUMBER = 1;

  /**
   * The universe number that this packet sends to.
   */
  private int universeNumber;

  /**
   * Creates a StreamingACNDatagram for the given model
   *
   * @param model Model of points
   */
  public StreamingACNDatagram(LXModel model) {
    this(model, DEFAULT_UNIVERSE_NUMBER);
  }

  /**
   * Constructs a StreamingACNDatagram on default universe
   *
   * @param indexBuffer Points to send on this universe
   */
  public StreamingACNDatagram(int[] indexBuffer) {
    this(indexBuffer, DEFAULT_UNIVERSE_NUMBER);
  }

  /**
   * Creates a StreamingACNDatagram for the model on given universe
   *
   * @param model Model of points
   * @param universeNumber Universe number
   */
  public StreamingACNDatagram(LXModel model, int universeNumber) {
    this(model.toIndexBuffer(), universeNumber);
  }

  /**
   * Constructs a datagram, sends the list of point indices on the given
   * universe number.
   *
   * @param indexBuffer List of point indices to encode in packet
   * @param universeNumber Universe number
   */
  public StreamingACNDatagram(int[] indexBuffer, int universeNumber) {
    this(indexBuffer, universeNumber, ByteOrder.RGB);
  }

  /**
   * Creates a StreamingACNDatagrm for given index buffer on universe and byte order
   *
   * @param indexBuffer Index buffer
   * @param universeNumber Universe number
   * @param byteOrder Byte order
   */
  public StreamingACNDatagram(int[] indexBuffer, int universeNumber, ByteOrder byteOrder) {
    this(indexBuffer, indexBuffer.length * byteOrder.getNumBytes(), universeNumber, byteOrder);
  }

  /**
   * Subclasses may override for a custom payload with fixed size, not necessarily
   * based upon an array of point indices - such as custom DMX data
   *
   * @param dataSize Fixed data payload size
   * @param universeNumber Universe number
   */
  protected StreamingACNDatagram(int dataSize, int universeNumber) {
    this(null, dataSize, universeNumber);
  }

  private StreamingACNDatagram(int[] indexBuffer, int dataSize, int universeNumber) {
    this(indexBuffer, dataSize, universeNumber, ByteOrder.RGB);
  }

  private StreamingACNDatagram(int[] indexBuffer, int dataSize, int universeNumber, ByteOrder byteOrder) {
    super(indexBuffer, OFFSET_DMX_DATA + dataSize, byteOrder);
    setPort(DEFAULT_PORT);
    setUniverseNumber(universeNumber);

    int flagLength;

    // Preamble size
    this.buffer[0] = (byte) 0x00;
    this.buffer[1] = (byte) 0x10;

    // Post-amble size
    this.buffer[0] = (byte) 0x00;
    this.buffer[1] = (byte) 0x10;

    // ACN Packet Identifier
    this.buffer[4] = (byte) 0x41;
    this.buffer[5] = (byte) 0x53;
    this.buffer[6] = (byte) 0x43;
    this.buffer[7] = (byte) 0x2d;
    this.buffer[8] = (byte) 0x45;
    this.buffer[9] = (byte) 0x31;
    this.buffer[10] = (byte) 0x2e;
    this.buffer[11] = (byte) 0x31;
    this.buffer[12] = (byte) 0x37;
    this.buffer[13] = (byte) 0x00;
    this.buffer[14] = (byte) 0x00;
    this.buffer[15] = (byte) 0x00;

    // Flags and length
    flagLength = 0x00007000 | ((this.buffer.length - 16) & 0x0fffffff);
    this.buffer[16] = (byte) ((flagLength >> 8) & 0xff);
    this.buffer[17] = (byte) (flagLength & 0xff);

    // RLP 1.31 Protocol PDU Identifier
    // VECTOR_ROOT_E131_DATA
    this.buffer[18] = (byte) 0x00;
    this.buffer[19] = (byte) 0x00;
    this.buffer[20] = (byte) 0x00;
    this.buffer[21] = (byte) 0x04;

    // Sender's CID - unique number
    for (int i = 22; i < 38; ++i) {
      this.buffer[i] = (byte) i;
    }

    // Flags and length
    flagLength = 0x00007000 | ((this.buffer.length - 38) & 0x0fffffff);
    this.buffer[38] = (byte) ((flagLength >> 8) & 0xff);
    this.buffer[39] = (byte) (flagLength & 0xff);

    // DMP Protocol PDU Identifier
    // VECTOR_E131_DATA_PACKET
    this.buffer[40] = (byte) 0x00;
    this.buffer[41] = (byte) 0x00;
    this.buffer[42] = (byte) 0x00;
    this.buffer[43] = (byte) 0x02;

    // Source name
    this.buffer[44] = 'L';
    this.buffer[45] = 'X';
    this.buffer[46] = '-';
    byte[] versionBytes = LX.VERSION.getBytes();
    System.arraycopy(versionBytes, 0, this.buffer, 47, versionBytes.length);
    for (int i = 47 + versionBytes.length; i < 108; ++i) {
      this.buffer[i] = 0;
    }

    // Priority
    this.buffer[108] = 100;

    // Reserved
    this.buffer[109] = 0x00;
    this.buffer[110] = 0x00;

    // Sequence Number
    this.buffer[111] = 0x00;

    // Options
    this.buffer[112] = 0x00;

    // Universe number
    // 113-114 are done in setUniverseNumber()

    // Flags and length
    flagLength = 0x00007000 | ((this.buffer.length - 115) & 0x0fffffff);
    this.buffer[115] = (byte) ((flagLength >> 8) & 0xff);
    this.buffer[116] = (byte) (flagLength & 0xff);

    // DMP Set Property Message PDU
    this.buffer[117] = (byte) 0x02;

    // Address Type & Data Type
    this.buffer[118] = (byte) 0xa1;

    // First Property Address
    this.buffer[119] = 0x00;
    this.buffer[120] = 0x00;

    // Address Increment
    this.buffer[121] = 0x00;
    this.buffer[122] = 0x01;

    // Property value count
    int numProperties = 1 + dataSize;
    this.buffer[123] = (byte) ((numProperties >> 8) & 0xff);
    this.buffer[124] = (byte) (numProperties & 0xff);

    // DMX Start
    this.buffer[125] = 0x00;
  }

  /**
   * Sets the universe for this datagram
   *
   * @param universeNumber DMX universe
   * @return this
   */
  public StreamingACNDatagram setUniverseNumber(int universeNumber) {
    this.universeNumber = (universeNumber &= 0x0000ffff);
    this.buffer[OFFSET_UNIVERSE_NUMBER] = (byte) ((universeNumber >> 8) & 0xff);
    this.buffer[OFFSET_UNIVERSE_NUMBER + 1] = (byte) (universeNumber & 0xff);
    return this;
  }

  /**
   * Universe number for datagram.
   *
   * @return Universe number
   */
  public int getUniverseNumber() {
    return this.universeNumber;
  }

  public void setDmxData(byte data, int channel) {
    if (channel < 0 || channel >= this.buffer.length - OFFSET_DMX_DATA) {
      throw new IndexOutOfBoundsException("Channel is greater than DMX data length");
    }
    this.buffer[OFFSET_DMX_DATA + channel] = data;
  }

  public void setDmxData(byte[] data, int channel) {
    if (channel < 0 || channel > this.buffer.length - OFFSET_DMX_DATA - data.length) {
      throw new IndexOutOfBoundsException("Channel is greater than DMX data length");
    }
    System.arraycopy(data, 0, this.buffer, OFFSET_DMX_DATA, data.length);
  }

  @Override
  protected int getColorBufferPosition() {
    return OFFSET_DMX_DATA;
  }

  @Override
  protected void updateSequenceNumber() {
    this.buffer[OFFSET_SEQUENCE_NUMBER]++;
  }

}
