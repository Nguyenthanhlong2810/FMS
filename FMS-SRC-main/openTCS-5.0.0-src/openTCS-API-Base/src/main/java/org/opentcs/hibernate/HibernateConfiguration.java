package org.opentcs.hibernate;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

@ConfigurationPrefix(HibernateConfiguration.PREFIX)
public interface HibernateConfiguration {

  String PREFIX = "hibernate";

  @ConfigurationEntry(type = "String", description = "Hibernate connection url hostname")
  String hostname();

  @ConfigurationEntry(type = "Integer", description = "Hibernate connection url port")
  int port();

  @ConfigurationEntry(type = "String", description = "Hibernate connection url username")
  String username();

  @ConfigurationEntry(type = "String", description = "Hibernate connection url password")
  String password();

  @ConfigurationEntry(type = "String", description = "Hibernate connection url datasource")
  String datasource();
}
