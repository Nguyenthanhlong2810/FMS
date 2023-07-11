package org.opentcs.virtualvehicle;

import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;

public class LoopbackCommunicationModule extends PlantOverviewInjectionModule {
    @Override
    protected void configure() {
        vehicleCommunicationInfoPanelFactoryBinder().addBinding().to(LoopbackCommunicationInfoPanelFactory.class);
    }
}
