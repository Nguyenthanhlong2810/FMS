package com.aubot.agv.integration.atc.commands;

import com.aubot.agv.integration.atc.AtcCommandException;

import java.util.Arrays;

public class ConnectWifiCommand extends AtcCommand {

  private int wifiIndex;

  public ConnectWifiCommand(int wifiIndex) {
    this.wifiIndex = wifiIndex;
  }

  @Override
  public String getCommand() {
    return "jbss " + wifiIndex;
  }

  @Override
  protected boolean handleResult0(String[] lines) {
    return true;
  }

  @Override
  public void handleResult(String result) throws AtcCommandException {
    if (result == null) {
      return;
    }
    String[] lines = result.split("\\r?\\n");
    int lastCmd = lines.length - 1;
    if (lines[lastCmd].contains("Ok")) {
      success = handleResult0(Arrays.copyOfRange(lines, 1, lastCmd));
    } else {
      if (lastCmd > 0) {
        if (lines[lastCmd - 1].contains("Join fail")) {
          throw new AtcCommandException(AtcCommandException.JOIN_FAIL);
        }
        error = lines[lastCmd - 1];
      }
      throw new AtcCommandException(error);
    }
  }
}
