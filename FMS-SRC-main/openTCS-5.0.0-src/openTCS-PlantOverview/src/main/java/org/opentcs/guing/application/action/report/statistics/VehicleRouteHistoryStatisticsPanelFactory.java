package org.opentcs.guing.application.action.report.statistics;

import org.opentcs.access.Kernel;
import org.opentcs.components.plantoverview.PluggablePanel;
import org.opentcs.components.plantoverview.PluggablePanelFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import static java.util.Objects.requireNonNull;

public class VehicleRouteHistoryStatisticsPanelFactory implements PluggablePanelFactory {

  private final Provider<VehicleRouteHistoryStatisticsPanel> provider;

  @Inject
  public VehicleRouteHistoryStatisticsPanelFactory(Provider<VehicleRouteHistoryStatisticsPanel> provider) {
    this.provider = requireNonNull(provider, "provider");
  }

  @Override
  public boolean providesPanel(Kernel.State state) {
    return true;
  }

  @Nonnull
  @Override
  public String getPanelDescription() {
    return "Vehicle Route History Statistics";
  }

  @Nullable
  @Override
  public PluggablePanel createPanel(Kernel.State state) {
    return provider.get();
  }
}
