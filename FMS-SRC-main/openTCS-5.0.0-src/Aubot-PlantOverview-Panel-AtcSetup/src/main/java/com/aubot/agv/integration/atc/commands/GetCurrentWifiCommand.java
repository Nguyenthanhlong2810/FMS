package com.aubot.agv.integration.atc.commands;

public class GetCurrentWifiCommand extends AtcCommand {

  private String wifiName;

  public String getWifiName() {
    return wifiName;
  }

  @Override
  public String getCommand() {
    return "wificonfig";
  }

  @Override
  protected boolean handleResult0(String[] lines) {
    for (String line : lines) {
      if (line.contains("SSID")) {
        wifiName = line.split(": ")[1];
        return true;
      }
    }

    wifiName = "";
    return false;
  }
}
