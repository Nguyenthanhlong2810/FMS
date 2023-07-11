package com.aubot.agv.integration.atc.commands;

public class SetServerPortCommand extends AtcCommand {

  private int port = 2020;

  public SetServerPortCommand(int port) {
    this.port = port;
  }

  @Override
  public String getCommand() {
    return "setsrvport " + port;
  }

  @Override
  protected boolean handleResult0(String[] lines) {
    return true;
  }
}
