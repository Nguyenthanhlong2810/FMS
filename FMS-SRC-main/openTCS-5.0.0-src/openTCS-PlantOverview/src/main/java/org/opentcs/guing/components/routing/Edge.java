package org.opentcs.guing.components.routing;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Data
@NoArgsConstructor
public class Edge {

  public static final char STRAIGHT = 'S';
  public static final char LEFT = 'L';
  public static final char RIGHT = 'R';

  private String start;
  private String end;
  private char direction = STRAIGHT;

  public Edge(String start, String end) {
    this.start = start;
    this.end = end;
  }

  public Edge(String start, String end, char direction) {
    this.start = start;
    this.end = end;
    this.direction = direction;
  }

  protected Edge clone() {
    return new Edge(start, end, direction);
  }

  public byte[] toByteArray() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] name = this.getStart().getBytes(StandardCharsets.UTF_8);
    byte direct = (byte) this.getDirection();
    try {
      outputStream.write(name);
      outputStream.write(direct);
    } catch (IOException ignored) { }

    return outputStream.toByteArray();
  }

}