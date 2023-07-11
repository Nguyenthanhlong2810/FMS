package com.aubot.agv.integration.atc.commands;

public class SetWifiPasswordCommand extends AtcCommand {

  private String password;

  public SetWifiPasswordCommand(String password) {
    this.password = password;
  }


  @Override
  public String getCommand() {
    return "setwp " + password;
  }

  @Override
  protected boolean handleResult0(String[] lines) {
    return true;
  }
}
