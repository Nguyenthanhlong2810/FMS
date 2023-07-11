package com.aubot.agv.integration.atc.commands;

public class RebootCommand extends AtcCommand {
  @Override
  public String getCommand() {
    return "reboot";
  }

  @Override
  protected boolean handleResult0(String[] lines) {
    return true;
  }
}
