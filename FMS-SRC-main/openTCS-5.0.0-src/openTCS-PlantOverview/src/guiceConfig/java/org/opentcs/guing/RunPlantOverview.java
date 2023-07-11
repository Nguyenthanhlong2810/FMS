/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.opentcs.configuration.ConfigurationBindingProvider;
import org.opentcs.configuration.cfg4j.Cfg4jConfigurationBindingProvider;
import org.opentcs.customizations.ConfigurableInjectionModule;
import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.opentcs.database.logging.LogManager;
import org.opentcs.database.logging.logentity.Logs;
import org.opentcs.guing.application.PlantOverviewStarter;
import org.opentcs.util.Environment;
import org.opentcs.util.logging.UncaughtExceptionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The plant overview process's default entry point.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RunPlantOverview {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RunPlantOverview.class);

  /**
   * Prevents external instantiation.
   */
  private RunPlantOverview() {
  }

  /**
   * The plant overview client's main entry point.
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    System.setSecurityManager(new SecurityManager());
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger(false));
    Environment.logSystemInfo();
    Injector injector = Guice.createInjector(customConfigurationModule());
    String path = RunPlantOverview.class.getCanonicalName() + ".main()";
    String content = "PlantOverview Started";
    Logs log = new Logs();
    log.setMethod(path);
    log.setLevel(Logs.LEVEL.INFO.name());
    log.setContent(content);
    LogManager logManager = new LogManager();
    logManager.listen();
    logManager.submit(log);
    injector.getInstance(PlantOverviewStarter.class).startPlantOverview();
  }

  /**
   * Builds and returns a Guice module containing the custom configuration for the plant overview
   * application, including additions and overrides by the user.
   *
   * @return The custom configuration module.
   */
  private static Module customConfigurationModule() {
    ConfigurationBindingProvider bindingProvider = configurationBindingProvider();
    ConfigurableInjectionModule plantOverviewInjectionModule
        = new DefaultPlantOverviewInjectionModule();
    plantOverviewInjectionModule.setConfigBindingProvider(bindingProvider);
    return Modules.override(plantOverviewInjectionModule)
        .with(findRegisteredModules(bindingProvider));
  }

  /**
   * Finds and returns all Guice modules registered via ServiceLoader.
   *
   * @return The registered/found modules.
   */
  private static List<ConfigurableInjectionModule> findRegisteredModules(
      ConfigurationBindingProvider bindingProvider) {
    List<ConfigurableInjectionModule> registeredModules = new LinkedList<>();
    for (PlantOverviewInjectionModule module
             : ServiceLoader.load(PlantOverviewInjectionModule.class)) {
      LOG.info("Integrating injection module {}", module.getClass().getName());
      module.setConfigBindingProvider(bindingProvider);
      registeredModules.add(module);
    }
    for (ControlCenterInjectionModule module
            : ServiceLoader.load(ControlCenterInjectionModule.class)) {
      LOG.info("Integrating injection module {}", module.getClass().getName());
      module.setConfigBindingProvider(bindingProvider);
      registeredModules.add(module);
    }
    return registeredModules;
  }

  private static ConfigurationBindingProvider configurationBindingProvider() {
    return new Cfg4jConfigurationBindingProvider(
        Paths.get(System.getProperty("opentcs.base", "."),
                  "config",
                  "opentcs-plantoverview-defaults-baseline.properties")
            .toAbsolutePath(),
        Paths.get(System.getProperty("opentcs.base", "."),
                  "config",
                  "opentcs-plantoverview-defaults-custom.properties")
            .toAbsolutePath(),
        Paths.get(System.getProperty("opentcs.home", "."),
                  "config",
                  "opentcs-plantoverview.properties")
            .toAbsolutePath()
    );
  }
}
