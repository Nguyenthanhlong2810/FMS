package com.aubot.agv.integration.rfid;

import java.nio.ByteBuffer;

public class RfidReadResult {

  private Rfid rifd;

  private boolean crcOk;

  private RfidReadResult() {

  }

  public Rfid getRifd() {
    return rifd;
  }

  public boolean isCrcOk() {
    return crcOk;
  }

  public static RfidReadResult read(byte[] block0, byte[] block1, byte[] block2) {
    Rfid rfid = new Rfid(new String(block0));
    if (block2[1] == 0x28) {
      rfid.setBeginIntersection(true);
      rfid.setIntersectionNo(block2[2]);
      rfid.setIntersectionRoadNo(block2[3]);
    } else if (block2[1] == 0x82) {
      rfid.setEndIntersection(true);
    }

    RfidReadResult result = new RfidReadResult();
    result.rifd = rfid;

    ByteBuffer crcData = ByteBuffer.allocate(8);
    crcData.put(block0);
    crcData.put(block1);
    ByteBuffer crcCheck = ByteBuffer.allocate(4);
    crcCheck.put(block2[1]);
    crcCheck.put(block2[2]);
    crcCheck.put(block2[3]);
    crcCheck.put(block2[0]);
    result.crcOk = CrcUtils.checkCrcData(crcData.array()) && CrcUtils.crcCheck8(crcCheck.array(), crcCheck.limit()) == 0x00;

    return result;
  }
}
