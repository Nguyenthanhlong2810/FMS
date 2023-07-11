package com.aubot.agv.integration.atc.commands;

public class SetServerModeCommand extends AtcCommand {
  @Override
  public String getCommand() {
    return "setmode 0";
  }

  @Override
  protected boolean handleResult0(String[] lines) {
    return true;
  }
}
