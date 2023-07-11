package cfg.aubot.commadapter.tcp;

import com.google.inject.Singleton;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;

public class TcpCommPlantOverviewInjectionModule extends PlantOverviewInjectionModule {

  @Override
  protected void configure() {

//    bind(I18nVehicleTcpConnectionInfo.class).in(Singleton.class);
//    vehicleCommInfoValidatorBinder().addBinding().to(TcpCommAdapterValidator.class).in(Singleton.class);
    propertySuggestionsBinder().addBinding().to(TcpCommAdapterSuggestions.class).in(Singleton.class);

    vehicleCommunicationInfoPanelFactoryBinder().addBinding().to(TcpCommunicationInfoPanelFactory.class);
  }
}
