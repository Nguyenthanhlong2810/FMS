package com.aubot.agv.integration.atc.commands;

public class SetGatewayCommand extends AtcCommand {

  String gateway;

  public SetGatewayCommand(String gateway) {
    this.gateway = gateway;
  }

  @Override
  public String getCommand() {
    return "setgateway " + gateway;
  }

  @Override
  protected boolean handleResult0(String[] lines) {
    return true;
  }
}
