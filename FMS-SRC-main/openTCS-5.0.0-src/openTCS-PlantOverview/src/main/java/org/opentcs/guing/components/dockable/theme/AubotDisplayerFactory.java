package org.opentcs.guing.components.dockable.theme;

import bibliothek.gui.DockStation;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.displayer.DisplayerRequest;
import bibliothek.gui.dock.station.DisplayerFactory;
import bibliothek.gui.dock.station.DockableDisplayer;
import bibliothek.gui.dock.title.DockTitle;

public class AubotDisplayerFactory implements DisplayerFactory {

  @Override
  public void request(DisplayerRequest request) {
    Dockable dockable = request.getTarget();
    DockStation station = request.getParent();
    DockTitle title = request.getTitle();
    DockableDisplayer.Location location;
    if (dockable.asDockStation() != null) {
      location = DockableDisplayer.Location.LEFT;
    } else {
      location = DockableDisplayer.Location.TOP;
    }

    request.answer(new AubotDockableDisplayer(station, dockable, title, location));
  }
}
