package com.aubot.agv.integration.rfid;

import org.opentcs.access.Kernel;
import org.opentcs.components.plantoverview.PluggablePanel;
import org.opentcs.components.plantoverview.PluggablePanelFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

public class RfidSetupPanelFactory implements PluggablePanelFactory {

  private final Provider<RfidSetupPanel> provider;

  private final ResourceBundle bundle = ResourceBundle.getBundle("i18n.com.aubot.agv.integration.rfid.Bundle");

  @Inject
  public RfidSetupPanelFactory(Provider<RfidSetupPanel> provider) {
    this.provider = requireNonNull(provider, "provider");
  }

  /**
   * Checks whether this factory produces panels that are available in the
   * passed <code>Kernel.State</code>.
   *
   * @param state The kernel state.
   * @return <code>true</code> if, and only if, this factory returns panels that
   * are available in the passed kernel state.
   */
  @Override
  public boolean providesPanel(Kernel.State state) {
    return true;
  }

  /**
   * Returns a string describing the factory/the panels provided.
   * This should be a short string that can be displayed e.g. as a menu item for
   * selecting a factory/plugin panel to be displayed.
   *
   * @return A string describing the factory/the panels provided.
   */
  @Nonnull
  @Override
  public String getPanelDescription() {
    return bundle.getString("rfidSetupPanel.title");
  }

  /**
   * Returns a newly created panel.
   * If a reference to the kernel provider has not been set, yet, or has been
   * set to <code>null</code>, this method returns <code>null</code>.
   *
   * @param state The kernel state for which to create the panel.
   * @return A newly created panel.
   */
  @Nullable
  @Override
  public PluggablePanel createPanel(Kernel.State state) {
    return provider.get();
  }
}
