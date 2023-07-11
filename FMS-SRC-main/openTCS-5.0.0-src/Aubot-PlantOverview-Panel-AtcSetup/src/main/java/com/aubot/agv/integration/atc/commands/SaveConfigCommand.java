package com.aubot.agv.integration.atc.commands;

public class SaveConfigCommand extends AtcCommand {
  @Override
  public String getCommand() {
    return "saveconfig";
  }

  @Override
  protected boolean handleResult0(String[] lines) {
    return true;
  }
}
