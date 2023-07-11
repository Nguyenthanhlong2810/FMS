package org.opentcs.kernel.extensions.database;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.opentcs.hibernate.HibernateConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

public class DatabaseSessionFactory {

  private static final Logger LOG = LoggerFactory.getLogger(DatabaseSessionFactory.class);

  private SessionFactory sessionFactory;

  private final HibernateConfiguration configuration;

  @Inject
  public DatabaseSessionFactory(HibernateConfiguration configuration) {
    this.configuration = requireNonNull(configuration, "hibernateConfiguration");

    this.sessionFactory = createSessionFactory();
  }

  private SessionFactory createSessionFactory() {
    LOG.info("Creating session factory...");
    Configuration cfg = new Configuration();
    cfg.configure();
    String connectionString = String.format("jdbc:postgresql://%s:%s/%s",
            configuration.hostname(),
            configuration.port(),
            configuration.datasource());
    cfg.setProperty("hibernate.connection.url", connectionString);
    cfg.setProperty("hibernate.connection.username", configuration.username());
    cfg.setProperty("hibernate.connection.password", configuration.password());

    return cfg.buildSessionFactory();
  }

  public Session getSession() {
    return sessionFactory.openSession();
  }
}
