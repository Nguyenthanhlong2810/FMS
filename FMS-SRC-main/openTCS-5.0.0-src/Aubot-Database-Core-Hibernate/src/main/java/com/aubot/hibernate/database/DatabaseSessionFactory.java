package com.aubot.hibernate.database;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.opentcs.components.Lifecycle;
import org.opentcs.hibernate.HibernateConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

public class DatabaseSessionFactory implements Lifecycle {

  private static final Logger LOG = LoggerFactory.getLogger(DatabaseSessionFactory.class);

  private SessionFactory sessionFactory;

  private final HibernateConfiguration configuration;

  private boolean initialized;

  @Inject
  public DatabaseSessionFactory(HibernateConfiguration configuration) {
    this.configuration = requireNonNull(configuration, "hibernateConfiguration");
  }

  /**
   * (Re-)Initializes this component before it is being used.
   */
  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    this.sessionFactory = createSessionFactory();
    initialized = true;
  }

  /**
   * Checks whether this component is initialized.
   *
   * @return <code>true</code> if, and only if, this component is initialized.
   */
  @Override
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * Terminates the instance and frees resources.
   */
  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    sessionFactory.close();
    sessionFactory = null;
    initialized = false;
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
    initialize();
    return sessionFactory.openSession();
  }
}
