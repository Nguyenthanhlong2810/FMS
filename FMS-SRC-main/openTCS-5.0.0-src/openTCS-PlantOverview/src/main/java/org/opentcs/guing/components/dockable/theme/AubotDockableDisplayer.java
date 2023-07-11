package org.opentcs.guing.components.dockable.theme;

import bibliothek.extension.gui.dock.theme.FlatTheme;
import bibliothek.extension.gui.dock.theme.flat.FlatBorder;
import bibliothek.gui.DockController;
import bibliothek.gui.DockStation;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.station.DockableDisplayer;
import bibliothek.gui.dock.themes.basic.BasicDockableDisplayer;
import bibliothek.gui.dock.themes.basic.BasicDockableDisplayerDecorator;
import bibliothek.gui.dock.themes.basic.TabDecorator;
import bibliothek.gui.dock.title.DockTitle;
import java.awt.*;

public class AubotDockableDisplayer extends BasicDockableDisplayer {

  private Border border = new Border(this);

  public AubotDockableDisplayer(DockStation station, Dockable dockable, DockTitle title, Location location) {
    super(station, dockable, title, location);
    this.setDefaultBorderHint(true);
    this.setRespectBorderHint(true);
    this.setSingleTabShowInnerBorder(false);
    this.setSingleTabShowOuterBorder(false);
  }

  public void setController(DockController controller) {
    super.setController(controller);
    this.border.connect(controller);
  }

  protected Border getDefaultBorder() {
    return this.border;
  }

  protected BasicDockableDisplayerDecorator createTabDecorator() {
    return new TabDecorator(this.getStation(), FlatTheme.ACTION_DISTRIBUTOR);
  }

  protected BasicDockableDisplayerDecorator createStackedDecorator() {
    return this.createStackedDecorator(FlatTheme.ACTION_DISTRIBUTOR);
  }

  private static class Border extends FlatBorder {

    public Border(DockableDisplayer owner) {
      super(owner);
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(0, 1, 1, 1);
    }
  }
}
