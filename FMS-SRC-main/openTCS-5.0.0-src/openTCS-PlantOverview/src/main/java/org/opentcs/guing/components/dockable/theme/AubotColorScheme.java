package org.opentcs.guing.components.dockable.theme;

import bibliothek.gui.DockUI;
import bibliothek.gui.dock.themes.color.DefaultColorScheme;
import bibliothek.util.Colors;

import java.awt.*;

public class AubotColorScheme extends DefaultColorScheme {

  public AubotColorScheme() {
    this.setColor("title.active.left", DockUI.getColor("dock.title.selection.background"));
    this.setColor("title.inactive.left", DockUI.getColor("dock.title.background"));
    this.setColor("title.active.right", Colors.brighter(DockUI.getColor("dock.title.selection.background")));
    this.setColor("title.inactive.right", Colors.brighter(DockUI.getColor("dock.title.background")));
    this.setColor("title.active.text", DockUI.getColor("dock.title.selection.foreground"));
    this.setColor("title.inactive.text", DockUI.getColor("dock.title.selection.foreground"));
    this.setColor("title.flap.active", DockUI.getColor("dock.title.selection.background"));
    this.setColor("title.flap.active.text", DockUI.getColor("dock.title.selection.foreground"));
    this.setColor("title.flap.active.knob.highlight", Colors.brighter(DockUI.getColor("dock.title.selection.background")));
    this.setColor("title.flap.active.knob.shadow", Colors.darker(DockUI.getColor("dock.title.selection.background")));
    this.setColor("title.flap.inactive", DockUI.getColor("dock.title.background"));
    this.setColor("title.flap.inactive.text", DockUI.getColor("dock.title.foreground"));
    this.setColor("title.flap.inactive.knob.highlight", Colors.brighter(DockUI.getColor("dock.title.background")));
    this.setColor("title.flap.inactive.knob.shadow", Colors.darker(DockUI.getColor("dock.title.background")));
    this.setColor("title.flap.selected", DockUI.getColor("dock.title.background"));
    this.setColor("title.flap.selected.text", DockUI.getColor("dock.title.foreground"));
    this.setColor("title.flap.selected.knob.highlight", Colors.brighter(DockUI.getColor("dock.title.background")));
    this.setColor("title.flap.selected.knob.shadow", Colors.darker(DockUI.getColor("dock.title.background")));
    this.setColor("paint", DockUI.getColor("dock.title.selection.background"));
    this.setColor("paint.insertion.area", DockUI.getColor("dock.title.selection.background"));
    this.setColor("paint.removal", Color.lightGray);
    Color border = DockUI.getColor("dock.background");
    this.setColor("stack.tab.border.center.selected", Colors.brighter(border));
    this.setColor("stack.tab.border.center.focused", Colors.brighter(border));
    this.setColor("stack.tab.border.center.disabled", Colors.brighter(border));
    this.setColor("stack.tab.border.center", Colors.darker(border));
    this.setColor("stack.tab.border", border);
    this.setColor("stack.tab.background.top.selected", Colors.diffMirror(border, 0.2D));
    this.setColor("stack.tab.background.top.focused", Colors.diffMirror(border, 0.2D));
    this.setColor("stack.tab.background.top.disabled", Colors.diffMirror(border, 0.1D));
    this.setColor("stack.tab.background.top", border);
    this.setColor("stack.tab.background.bottom.selected", Colors.diffMirror(border, 0.1D));
    this.setColor("stack.tab.background.bottom.focused", Colors.diffMirror(border, 0.1D));
    this.setColor("stack.tab.background.bottom.disabled", Colors.diffMirror(border, 0.1D));
    this.setColor("stack.tab.background.bottom", border);
    this.setColor("stack.tab.foreground", DockUI.getColor("dock.foreground"));
  }
}
