package com.aubot.agv.integration.rfid;

import com.fazecast.jSerialComm.SerialPort;

import java.nio.ByteBuffer;

public class RfidReadWrite {

  private SerialPort port;

  public RfidReadWrite() {
  }

  public boolean connect(SerialPort port) {
    port.setBaudRate(57600);
    port.setNumDataBits(8);
    port.setNumStopBits(SerialPort.ONE_STOP_BIT);
    port.setParity(SerialPort.NO_PARITY);
    port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
            3000, 1200);

    if (port.openPort()) {
      this.port = port;
      byte[] open = { 0x02, 0x02, 0x01, 0x01 };
      byte[] opened = readReceive(open);
      if (opened.length != 2) {
        port.closePort();
        return false;
      }
      if (opened[0] != 0x00 || opened[1] != 0x00) {
        port.closePort();
        return false;
      }

      return true;
    }

    return false;
  }

  public boolean isConnected() {
    return port != null && port.isOpen();
  }

  public void disconnect() {
    if (port != null) {
      if (port.isOpen()) {
        port.closePort();
      }
      port = null;
    }
  }

  public RfidReadResult read() {
    byte[] block0 = readRfidBlock((byte) 0);
    byte[] block1 = readRfidBlock((byte) 1);
    byte[] block2 = readRfidBlock((byte) 2);
    if ((block0.length & block1.length & block2.length) == 0) {
      return null;
    }

    return RfidReadResult.read(block0, block1, block2);
  }

  private String checkBlock(byte block) {
    for (int i = 0; i < 3; i++) {
      byte[] data = readRfidBlock(block);
      if (data.length > 0) {
        return new String(data);
      }
    }

    return null;
  }

  private byte[] readRfidBlock(byte block) {
    byte[] data = { 0x04, 0x03, 0x02, 0x20, block };
    byte[] result = readReceive(data);
    if (Byte.toUnsignedInt(result[0]) == 0x80 && Byte.toUnsignedInt(result[1]) == 0x08) {
      return new byte[] { result[3], result[4], result[5], result[6] };
    }

    return new byte[0];
  }

  public boolean write(Rfid rfid) {
    ByteBuffer buffer = ByteBuffer.allocate(5);
    byte[] names = rfid.getName().substring(0, 4).getBytes();
    buffer.put((byte) 0x00);
    buffer.put(names);
    if (!writeBlock(buffer.array())) {
      return false;
    }

    int r = names[0] + names[1] + names[2] + names[3];
    ByteBuffer buffCrc = ByteBuffer.allocate(8);
    buffCrc.put(names);
    buffCrc.put(new byte[] {(byte) (r >> 8), (byte) (r), 0, 0});
    int crc = CrcUtils.createCrc(buffCrc.array(), buffCrc.limit());
    if (!writeBlock(new byte[] {0x01, (byte) (r >> 8), (byte) r, (byte) (crc >> 8), (byte) crc})) {
      return false;
    }
    byte[] intersection;
    if (rfid.isBeginIntersection()) {
      byte in = (byte) rfid.getIntersectionNo();
      byte irn = (byte) rfid.getIntersectionRoadNo();
      crc = CrcUtils.crcCheck8(new byte[] {0x28, in, irn}, 3);
      intersection = new byte[] {0x02, (byte) crc, 0x28, in, irn};
    } else if (rfid.isEndIntersection()) {
      intersection = new byte[] {0x02, (byte) 0xDD, (byte) 0x82, 0x00, 0x00};
    } else {
      intersection = new byte[] {0x02, 0, 0, 0, 0};
    }

    return writeBlock(intersection);
  }

  public byte[] readReceive(byte[] data) {
    if (!isConnected()) {
      return new byte[0];
    }

    port.writeBytes(data, data.length);

    byte[] header = new byte[2];
    port.readBytes(header, 2);
    int len = header[1];
    if (len == 0) {
      return header;
    }
    byte[] result = new byte[len];
    int read = port.readBytes(result, len);
    if (read < len) {
      return new byte[0];
    }

    ByteBuffer buffer = ByteBuffer.allocate(len + 2);
    buffer.put(header);
    buffer.put(result);

    return buffer.array();
  }

  private boolean writeBlock(byte[] write) {
    ByteBuffer buffer = ByteBuffer.allocate(write.length + 4);
    buffer.put(new byte[]{0x04, 0x07, 0x42, 0x21});
    buffer.put(write);
    readReceive(buffer.array());

    return checkBlock(write[0]) != null;
  }
}
