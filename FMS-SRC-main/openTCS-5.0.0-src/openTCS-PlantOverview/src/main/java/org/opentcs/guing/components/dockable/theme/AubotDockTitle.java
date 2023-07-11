package org.opentcs.guing.components.dockable.theme;

import bibliothek.extension.gui.dock.theme.smooth.SmoothDefaultTitle;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.title.DockTitleVersion;
import java.awt.*;

public class AubotDockTitle extends SmoothDefaultTitle {

  private static final Insets DEFAULT_INSETS_HORIZONTAL = new Insets(3, 2, 3, 2);
  private static final Insets DEFAULT_INSETS_VERTICAL = new Insets(2, 3, 2, 3);

  public AubotDockTitle(Dockable dockable, DockTitleVersion origin) {
    super(dockable, origin);
  }

  protected Insets getInnerInsets() {
    return this.getOrientation().isHorizontal() ? DEFAULT_INSETS_HORIZONTAL : DEFAULT_INSETS_VERTICAL;
  }
}
