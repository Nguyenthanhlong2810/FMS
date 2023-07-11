package com.aubot.agv.traffic;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

@ConfigurationPrefix(TrafficManagerConfiguration.PREFIX)
public interface TrafficManagerConfiguration {

  String PREFIX = "extension.trafficmanager";

  @ConfigurationEntry(type = "Boolean", description = "Traffic Manager enable")
  boolean enable();
}
