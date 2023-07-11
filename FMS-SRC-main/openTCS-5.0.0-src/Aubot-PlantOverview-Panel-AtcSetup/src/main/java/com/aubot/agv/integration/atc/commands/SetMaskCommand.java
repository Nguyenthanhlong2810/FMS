package com.aubot.agv.integration.atc.commands;

public class SetMaskCommand extends AtcCommand {

  String mask;

  public SetMaskCommand(String mask) {
    this.mask = mask;
  }

  @Override
  public String getCommand() {
    return "setmask " + mask;
  }

  @Override
  protected boolean handleResult0(String[] lines) {
    return true;
  }
}
