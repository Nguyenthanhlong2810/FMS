package org.opentcs.example.telegrams;

public abstract class WorkingRouteRequest extends Request {

  protected int mapId;
  /**
   * Creates a new instance.
   *
   * @param telegramLength The request's length.
   */
  public WorkingRouteRequest(int telegramLength) {
    super(telegramLength);
  }

  public int getMapId() {
    return mapId;
  }
}
