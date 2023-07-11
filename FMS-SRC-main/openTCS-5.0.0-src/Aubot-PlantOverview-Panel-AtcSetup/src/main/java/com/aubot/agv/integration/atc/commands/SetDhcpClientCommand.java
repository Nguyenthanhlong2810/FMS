package com.aubot.agv.integration.atc.commands;

public class SetDhcpClientCommand extends AtcCommand {

  boolean dhcp = true;

  public SetDhcpClientCommand(boolean dhcp) {
    this.dhcp = dhcp;
  }

  @Override
  public String getCommand() {
    return "dhcpclient " + (dhcp ? 1 : 0);
  }

  @Override
  protected boolean handleResult0(String[] lines) {
    return true;
  }
}
