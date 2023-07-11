package org.opentcs.guing.components.dockable.theme;

import bibliothek.gui.dock.title.DockTitleFactory;
import bibliothek.gui.dock.title.DockTitleRequest;

public class AubotDockTitleFactory implements DockTitleFactory {
  @Override
  public void install(DockTitleRequest dockTitleRequest) {

  }

  @Override
  public void request(DockTitleRequest dockTitleRequest) {
    dockTitleRequest.answer(new AubotDockTitle(dockTitleRequest.getTarget(), dockTitleRequest.getVersion()));
  }

  @Override
  public void uninstall(DockTitleRequest dockTitleRequest) {

  }
}
