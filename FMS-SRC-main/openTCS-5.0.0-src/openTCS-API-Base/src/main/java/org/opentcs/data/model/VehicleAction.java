package org.opentcs.data.model;

public enum VehicleAction {

  NONE(' '),
  STOP('S'),
  PAUSE_5S('T'),
  PAUSE_10S('U');

  private final char presentation;

  VehicleAction(char presentation) {
    this.presentation = presentation;
  }

  public char getPresentation() {
    return presentation;
  }

  public static VehicleAction getAction(char action) {
    for (VehicleAction value : values()) {
      if (value.getPresentation() == action) {
        return value;
      }
    }

    return NONE;
  }

  @Override
  public String toString() {
    if (this == NONE) {
      return "";
    }

    return this.name();
  }
}
