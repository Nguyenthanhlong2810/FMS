package com.aubot.agv.integration.atc.commands;

public class SetIpCommand extends AtcCommand {

  String ip;

  public SetIpCommand(String ip) {
    this.ip = ip;
  }

  @Override
  public String getCommand() {
    return "setip " + ip;
  }

  @Override
  protected boolean handleResult0(String[] lines) {
    return true;
  }
}
