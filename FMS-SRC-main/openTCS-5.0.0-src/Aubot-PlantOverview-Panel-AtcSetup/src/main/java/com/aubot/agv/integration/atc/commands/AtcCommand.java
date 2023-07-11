package com.aubot.agv.integration.atc.commands;

import com.aubot.agv.integration.atc.AtcCommandException;

import java.util.Arrays;

public abstract class AtcCommand {

  protected boolean success = false;

  protected String error = "Error!";

  public boolean isSuccess() {
    return success;
  }

  public abstract String getCommand();

  public String getError() {
    return error;
  }

  public void handleResult(String result) throws AtcCommandException {
    if (result == null) {
      return;
    }
    String[] lines = result.split("\\r?\\n");
    int lastCmd = lines.length - 1;
    if (lines[lastCmd].contains("Ok")) {
      success = handleResult0(Arrays.copyOfRange(lines, 1, lastCmd));
    } else if (lines[lastCmd].contains("Error")) {
      if (lastCmd > 0) {
        error = lines[lastCmd - 1];
      }
      throw new AtcCommandException(error);
    }
  }

  protected abstract boolean handleResult0(String[] lines);
}
