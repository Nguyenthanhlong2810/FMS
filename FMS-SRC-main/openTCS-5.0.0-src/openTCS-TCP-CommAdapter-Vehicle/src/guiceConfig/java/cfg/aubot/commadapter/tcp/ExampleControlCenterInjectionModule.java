/**
 * Copyright (c) Fraunhofer IML
 */
package cfg.aubot.commadapter.tcp;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import cfg.aubot.commadapter.tcp.exchange.AdapterPanelComponentsFactory;
import cfg.aubot.commadapter.tcp.exchange.ExampleCommAdapterPanelFactory;
import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;

/**
 * A custom Guice module for project-specific configuration.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ExampleControlCenterInjectionModule
    extends ControlCenterInjectionModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(AdapterPanelComponentsFactory.class));

    commAdapterPanelFactoryBinder().addBinding().to(ExampleCommAdapterPanelFactory.class);


  }
}
