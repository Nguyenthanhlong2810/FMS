package org.opentcs.guing.components.dockable.theme;

import bibliothek.extension.gui.dock.theme.bubble.*;
import bibliothek.extension.gui.dock.theme.flat.FlatStationPaint;
import bibliothek.gui.DockController;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.action.*;
import bibliothek.gui.dock.action.actions.SeparatorAction;
import bibliothek.gui.dock.action.view.ActionViewConverter;
import bibliothek.gui.dock.action.view.ViewGenerator;
import bibliothek.gui.dock.action.view.ViewTarget;
import bibliothek.gui.dock.station.stack.tab.layouting.TabPlacement;
import bibliothek.gui.dock.themes.BasicTheme;
import bibliothek.gui.dock.themes.ColorScheme;
import bibliothek.gui.dock.themes.basic.NoSpanFactory;
import bibliothek.gui.dock.themes.basic.action.*;
import bibliothek.gui.dock.util.DockProperties;
import bibliothek.gui.dock.util.Priority;
import bibliothek.gui.dock.util.PropertyKey;
import bibliothek.gui.dock.util.property.DynamicPropertyFactory;

import javax.swing.*;

public class AubotTheme extends BasicTheme {

  public static final PropertyKey<ColorScheme> COLOR_SCHEME = new PropertyKey<>("dock.ui.AubotTheme.ColorScheme", new DynamicPropertyFactory<ColorScheme>() {
    public ColorScheme getDefault(PropertyKey<ColorScheme> key, DockProperties properties) {
      return new AubotColorScheme();
    }
  }, true);

  public AubotTheme() {
    this.setColorSchemeKey(COLOR_SCHEME);
    this.setDisplayerFactory(new AubotDisplayerFactory(), Priority.DEFAULT);
    this.setTitleFactory(new AubotDockTitleFactory(), Priority.DEFAULT);
    this.setPaint(new FlatStationPaint(), Priority.DEFAULT);
//    this.setMovingImageFactory(new BubbleMovingImageFactory(), Priority.DEFAULT);
    this.setTabPlacement(TabPlacement.BOTTOM_OF_DOCKABLE, Priority.DEFAULT);
    this.setSpanFactory(new NoSpanFactory());
  }

  @Override
  public void install(DockController controller) {
    super.install(controller);
//    controller.getDockTitleManager().registerTheme("flap button", new SmoothDefaultButtonTitleFactory());
//    controller.getProperties().set(TabPane.LAYOUT_MANAGER, new MenuLineLayout(), Priority.THEME);
    ActionViewConverter converter = controller.getActionViewConverter();
    converter.putTheme(ActionType.BUTTON, ViewTarget.TITLE, new ButtonGenerator());
    converter.putTheme(ActionType.CHECK, ViewTarget.TITLE, new CheckGenerator());
    converter.putTheme(ActionType.RADIO, ViewTarget.TITLE, new RadioGenerator());
    converter.putTheme(ActionType.DROP_DOWN, ViewTarget.TITLE, new DropDownGenerator());
    converter.putTheme(ActionType.MENU, ViewTarget.TITLE, new MenuGenerator());
    converter.putTheme(ActionType.SEPARATOR, ViewTarget.TITLE, new SeparatorGenerator());
  }

  @Override
  public void uninstall(DockController controller) {
    super.uninstall(controller);
//    controller.getProperties().unset(TabPane.LAYOUT_MANAGER, Priority.THEME);
    controller.getDockTitleManager().clearThemeFactories();
    controller.getIcons().setScheme(Priority.THEME, null);
    ActionViewConverter converter = controller.getActionViewConverter();
    converter.putTheme(ActionType.BUTTON, ViewTarget.TITLE, null);
    converter.putTheme(ActionType.CHECK, ViewTarget.TITLE, null);
    converter.putTheme(ActionType.RADIO, ViewTarget.TITLE, null);
    converter.putTheme(ActionType.DROP_DOWN, ViewTarget.TITLE, null);
    converter.putTheme(ActionType.MENU, ViewTarget.TITLE, null);
    converter.putTheme(ActionType.SEPARATOR, ViewTarget.TITLE, null);
  }

  private static class SeparatorGenerator implements ViewGenerator<SeparatorAction, BasicTitleViewItem<JComponent>> {
    private SeparatorGenerator() {
    }

    public BasicTitleViewItem<JComponent> create(ActionViewConverter converter, SeparatorAction action, Dockable dockable) {
      return action.shouldDisplay(ViewTarget.TITLE) ? new BubbleSeparator(action) : null;
    }
  }

  private static class MenuGenerator implements ViewGenerator<MenuDockAction, BasicTitleViewItem<JComponent>> {
    private MenuGenerator() {
    }

    public BasicTitleViewItem<JComponent> create(ActionViewConverter converter, MenuDockAction action, Dockable dockable) {
      BasicMenuHandler handler = new BasicMenuHandler(action, dockable);
      RoundButton button = new RoundButton(handler, handler);
      handler.setModel(button.getModel());
      return handler;
    }
  }

  private static class DropDownGenerator implements ViewGenerator<DropDownAction, BasicTitleViewItem<JComponent>> {
    private DropDownGenerator() {
    }

    public BasicTitleViewItem<JComponent> create(ActionViewConverter converter, DropDownAction action, Dockable dockable) {
      BasicDropDownButtonHandler handler = new BasicDropDownButtonHandler(action, dockable);
      RoundDropDownButton button = new RoundDropDownButton(handler, dockable, action);
      handler.setModel(button.getModel());
      return new RoundButtonViewItem(dockable, handler, button);
    }
  }

  private static class RadioGenerator implements ViewGenerator<SelectableDockAction, BasicTitleViewItem<JComponent>> {
    private RadioGenerator() {
    }

    public BasicTitleViewItem<JComponent> create(ActionViewConverter converter, SelectableDockAction action, Dockable dockable) {
      BasicSelectableHandler.Radio handler = new BasicSelectableHandler.Radio(action, dockable);
      RoundButton button = new RoundButton(handler, handler);
      handler.setModel(button.getModel());
      return handler;
    }
  }

  private static class CheckGenerator implements ViewGenerator<SelectableDockAction, BasicTitleViewItem<JComponent>> {
    private CheckGenerator() {
    }

    public BasicTitleViewItem<JComponent> create(ActionViewConverter converter, SelectableDockAction action, Dockable dockable) {
      BasicSelectableHandler.Check handler = new BasicSelectableHandler.Check(action, dockable);
      RoundButton button = new RoundButton(handler, handler);
      handler.setModel(button.getModel());
      return handler;
    }
  }

  private static class ButtonGenerator implements ViewGenerator<ButtonDockAction, BasicTitleViewItem<JComponent>> {
    private ButtonGenerator() {
    }

    public BasicTitleViewItem<JComponent> create(ActionViewConverter converter, ButtonDockAction action, Dockable dockable) {
      BasicButtonHandler handler = new BasicButtonHandler(action, dockable);
      RoundButton button = new RoundButton(handler, handler);
      handler.setModel(button.getModel());
      return handler;
    }
  }
}
