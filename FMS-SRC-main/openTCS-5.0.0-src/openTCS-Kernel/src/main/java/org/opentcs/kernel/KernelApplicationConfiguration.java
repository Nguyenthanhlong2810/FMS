/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides common kernel configuration entries.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(KernelApplicationConfiguration.PREFIX)
public interface KernelApplicationConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "kernelapp";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to automatically enable drivers on startup.",
      orderKey = "1_startup_0")
  boolean autoEnableDriversOnStartup();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to implicitly save the model when leaving modelling state.",
      orderKey = "2_autosave")
  boolean saveModelOnTerminateModelling();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to implicitly save the model when leaving operating state.",
      orderKey = "2_autosave")
  boolean saveModelOnTerminateOperating();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to implicitly update the router's topology when a path is (un)locked.",
      orderKey = "3_topologyUpdate")
  boolean updateRoutingTopologyOnPathLockChange();

  @ConfigurationEntry(
          type = "String",
          description = {"Server name - customer name or company name", "Example: aubot.itteam.nvkhoi"},
          orderKey = "0_name_1"
  )
  String name();
  //port
  @ConfigurationEntry(
          type = "String",
          description = {" Port for postgresql ehehetenantayo","Example: 2021"},
          orderKey = "0_init_1"
  )
  String port();
  //username
  @ConfigurationEntry(
          type = "String",
          description = {" username for postgresql ","Example: postgres"},
          orderKey = "0_init_2"
  )
  String username();
  //password
  @ConfigurationEntry(
          type = "String",
          description = {" password for postgresql ","Example: dobaduy99"},
          orderKey = "0_init_3"
  )
  String password();
  //ip
  @ConfigurationEntry(
          type = "String",
          description = {" ipaddress for postgresql ehehetenantayo","Example: localhost"},
          orderKey = "0_init_4"
  )
  String ipaddress();
  //database's name
  @ConfigurationEntry(
          type = "String",
          description = {" database name of postgresql ehehetenantayo","Example: aubot"},
          orderKey = "0_init_5"
  )
  String dbname();
}
