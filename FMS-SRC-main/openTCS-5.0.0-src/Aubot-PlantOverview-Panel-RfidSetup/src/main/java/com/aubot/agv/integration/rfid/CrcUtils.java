package com.aubot.agv.integration.rfid;

public class CrcUtils {

  public static int createCrc(byte[] buffer, int len) {
    int i;
    char crcReg = (char) 0xFFFF;
    for (i = 1; i < len - 2; i++)
    {
      crcReg = calculateCrc8(crcReg, (char) 0x1021, buffer[i]);
    }

    return crcReg;
  }

  public static boolean checkCrcData(byte[] data)
  {
    char cardDataCrc = (char)(data[0] + data[1] + data[2] + data[3]);
    if (cardDataCrc == (data[4] & 0xFF) * 256 + (data[5] & 0xFF)) {
      return checkCrc(data, 8);
    } else {
      return false;
    }
  }

  public static boolean checkCrc(byte[] buffer, int len)
  {
    int i;
    char crc_reg = 0xFFFF;
    for (i = 1; i < len - 2; i++) {
      crc_reg = calculateCrc8(crc_reg, (char) 0x1021, buffer[i]);
    }

    return (buffer[len - 2] == (byte) (crc_reg >> 8)) && (buffer[len - 1] == (byte) (crc_reg & 255));
  }

  public static byte crcCheck8(byte[] bytes, int le)
  {
    byte generator = 0x07;
    byte crc = 0x00; /* start with 0 so first byte can be 'xored' in */

    for (int m = 0; m < le; m++) {
      crc ^= bytes[m]; /* XOR-in the next input byte */

      for (int i = 0; i < 8; i++) {
        if ((crc & 0x80) != 0) {
          crc = (byte)((crc << 1) ^ generator);
        } else {
          crc <<= 1;
        }
      }
    }

    return crc;
  }

  public static char calculateCrc8(char crcReg, char poly, byte u8Data) {
    char i;
    char h;
    char xorFlag;
    char bit;
    char dcdBitMask = 0x80;
    for (i = 0; i < 8; i++) {
      // Get the carry bit. This determines if the polynomial should be
      // xor'd with the CRC register.
      xorFlag = (char) (crcReg & 0x8000);
      // Shift the bits over by one.
      crcReg <<= 1;
      // Shift in the next bit in the data byte
      if ((u8Data & dcdBitMask) == dcdBitMask) {
        bit = 1;
      } else {
        bit = 0;
      }
      crcReg |= bit;
      // XOR the polynomial
      if (xorFlag>0) {
        crcReg = (char) (crcReg ^ poly);
      }
      // Shift over the dcd mask
      dcdBitMask >>= 1;
    }
    h = crcReg;

    return h;
  }
}
