package org.opentcs.guing.components.dockable.theme;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.util.LoggingFacade;

import java.io.IOException;

public class AubotIJTheme extends IntelliJTheme.ThemeLaf {

  static IntelliJTheme loadTheme() {
    try {
      return new IntelliJTheme(AubotIJTheme.class.getResourceAsStream(
              "/aubot-theme.theme.json"));
    } catch (IOException ex) {
      String msg = "FlatLaf: Failed to load theme, return default theme";
      LoggingFacade.INSTANCE.logSevere(msg, ex);
      throw new RuntimeException(msg, ex);
    }
  }

  public AubotIJTheme() {
    super(loadTheme());
  }
}
