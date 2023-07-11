/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.util;

import java.util.List;
import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;
import org.opentcs.util.gui.dialog.ConnectionParamSet;

/**
 * Provides methods to configure the KernelControlCenter application.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(KernelControlCenterConfiguration.PREFIX)
public interface KernelControlCenterConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "kernelcontrolcenter";

  @ConfigurationEntry(
      type = "String",
      description = {"The kernel control center application's locale, as a BCP 47 language tag.",
                     "Examples: 'en', 'de', 'zh'"},
      orderKey = "0_init_0")
  String locale();

  @ConfigurationEntry(
      type = "Comma-separated list of <description>\\|<hostname>\\|<port>",
      description = "Kernel connection bookmarks to be used.",
      orderKey = "1_connection_0")
  List<ConnectionParamSet> connectionBookmarks();

  @ConfigurationEntry(
      type = "Boolean",
      description = {"Whether to automatically connect to the kernel on startup.",
                     "If 'true', the first connection bookmark will be used for the initial "
                     + "connection attempt.",
                     "If 'false', a dialog will be shown to enter connection parameters."},
      orderKey = "1_connection_1")
  boolean connectAutomaticallyOnStartup();

  @ConfigurationEntry(
      type = "Integer",
      description = "The maximum number of characters in the logging text area.",
      orderKey = "9_misc")
  int loggingAreaCapacity();
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
          description = {" ip for postgresql ehehetenantayo","Example: localhost"},
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
