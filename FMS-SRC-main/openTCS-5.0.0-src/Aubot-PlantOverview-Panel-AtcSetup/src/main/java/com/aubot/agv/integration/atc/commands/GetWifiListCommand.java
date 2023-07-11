package com.aubot.agv.integration.atc.commands;

import java.util.ArrayList;

public class GetWifiListCommand extends AtcCommand {

  private ArrayList<String> wifiNames = new ArrayList<>();

  @Override
  public String getCommand() {
    return "sisrvy";
  }

  @Override
  public boolean handleResult0(String[] lines) {
    int limit = -1;
    for (String line : lines) {
      if (limit < 0) {
        limit = line.lastIndexOf("TYPE ");
        continue;
      }

      wifiNames.add(line.substring(24, limit).trim());
    }
    return limit >= 0;
  }

  public ArrayList<String> getWifiNames() {
    return wifiNames;
  }
}
