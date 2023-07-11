package com.aubot.agv.integration.atc;

import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;

public class AtcSetupPanelInjectionModule extends PlantOverviewInjectionModule {

  @Override
  protected void configure() {
    pluggablePanelFactoryBinder().addBinding().to(AtcSetupPanelFactory.class);
  }
}
