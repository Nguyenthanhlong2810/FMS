package com.aubot.agv.integration.rfid;

import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;

public class RfidSetupPanelInjectionModule extends PlantOverviewInjectionModule {

  @Override
  protected void configure() {
    pluggablePanelFactoryBinder().addBinding().to(RfidSetupPanelFactory.class);
  }
}
