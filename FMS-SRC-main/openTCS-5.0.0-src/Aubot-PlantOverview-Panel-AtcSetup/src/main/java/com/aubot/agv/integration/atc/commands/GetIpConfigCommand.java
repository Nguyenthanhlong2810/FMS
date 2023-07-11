package com.aubot.agv.integration.atc.commands;

public class GetIpConfigCommand extends AtcCommand {

  private String ip;
  private String mask;
  private String gateway;

  public String[] getAddresses() {
    return new String[] {
        getIp(),
        getMask(),
        getGateway()
    };
  }

  public String getIp() {
    return ip;
  }

  public String getMask() {
    return mask;
  }

  public String getGateway() {
    return gateway;
  }

  @Override
  public String getCommand() {
    return "ipconfig";
  }

  @Override
  protected boolean handleResult0(String[] lines) {
    ip = lines[0].split(": ")[1];
    mask = lines[1].split(": ")[1];
    gateway = lines[2].split(": ")[1];

    return true;
  }
}
