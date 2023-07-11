package com.aubot.agv.traffic;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficManagerModule extends KernelInjectionModule {

  private static final Logger LOG = LoggerFactory.getLogger(TrafficManagerModule.class);

  @Override
  protected void configure() {
    TrafficManagerConfiguration configuration = getConfigBindingProvider()
            .get(TrafficManagerConfiguration.PREFIX, TrafficManagerConfiguration.class);
    bind(TrafficManagerConfiguration.class).toInstance(configuration);
    if (!configuration.enable()) {
      return;
    }

    LOG.info("Enable Traffic manager extension");
    Multibinder<IntersectionHandler> intersectionHandlerBinder = Multibinder.newSetBinder(binder(), IntersectionHandler.class);
    intersectionHandlerBinder.addBinding().to(PointIntersection.class);
    intersectionHandlerBinder.addBinding().to(BlockIntersection.class);
    extensionsBinderOperating().addBinding().to(TrafficManager.class);

  }
}
