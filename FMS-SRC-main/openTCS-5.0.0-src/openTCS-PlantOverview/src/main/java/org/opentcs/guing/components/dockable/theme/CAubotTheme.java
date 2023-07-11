package org.opentcs.guing.components.dockable.theme;

import bibliothek.gui.DockController;
import bibliothek.gui.dock.action.view.ActionViewConverter;
import bibliothek.gui.dock.action.view.ViewTarget;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.action.CPanelPopup;
import bibliothek.gui.dock.common.intern.action.panel.BasicPanelPopupGenerator;
import bibliothek.gui.dock.common.intern.action.panel.PanelDropDownGenerator;
import bibliothek.gui.dock.common.intern.action.panel.PanelMenuGenerator;
import bibliothek.gui.dock.common.theme.CDockTheme;
import bibliothek.gui.dock.themes.NoStackTheme;
import bibliothek.gui.dock.util.DockUtilities;
import bibliothek.gui.dock.util.IconManager;
import bibliothek.gui.dock.util.Priority;

import javax.swing.*;
import java.util.Map;

public class CAubotTheme extends CDockTheme<AubotTheme> {

  public CAubotTheme(CControl control, AubotTheme theme) {
    super(theme);
    init(control);
  }

  /**
   * Creates a new theme. This theme can be used directly with a
   * {@link CControl}.
   *
   * @param control the controller for which this theme will be used.
   */
  public CAubotTheme(CControl control) {
    this(new AubotTheme());
    init(control);
  }

  /**
   * Creates a new theme.
   *
   * @param theme the delegate which will do most of the work
   */
  private CAubotTheme(AubotTheme theme) {
    super(theme, new NoStackTheme(theme));
  }

  /**
   * Initializes the properties of this theme.
   *
   * @param control the controller for which this theme will be used
   */
  private void init(final CControl control) {
//    putColorBridgeFactory(TabColor.KIND_TAB_COLOR, new ColorBridgeFactory() {
//      public ColorBridge create(ColorManager manager) {
//        BasicTabTransmitter transmitter = new BasicTabTransmitter(manager);
//        transmitter.setControl(control);
//        return transmitter;
//      }
//    });
//    putColorBridgeFactory(TitleColor.KIND_TITLE_COLOR, new ColorBridgeFactory() {
//      public ColorBridge create(ColorManager manager) {
//        BubbleTitleTransmitter transmitter = new BubbleTitleTransmitter(manager);
//        transmitter.setControl(control);
//        return transmitter;
//      }
//    });
//    putColorBridgeFactory( DisplayerColor.KIND_DISPLAYER_COLOR, new ColorBridgeFactory(){
//      public ColorBridge create( ColorManager manager ) {
//        BubbleDisplayerTransmitter transmitter = new BubbleDisplayerTransmitter( manager );
//        transmitter.setControl( control );
//        return transmitter;
//      }
//    });
//    putColorBridgeFactory( TitleColor.KIND_FLAP_BUTTON_COLOR, new ColorBridgeFactory(){
//      public ColorBridge create( ColorManager manager ) {
//        BubbleButtonTitleTransmitter transmitter = new BubbleButtonTitleTransmitter( manager );
//        transmitter.setControl( control );
//        return transmitter;
//      }
//    });
    initDefaultFontBridges(control);
  }

  @Override
  public void install(DockController controller) {
    super.install(controller);
    IconManager manager = controller.getIcons();
    Map<String, Icon> icons = DockUtilities.loadIcons(
            "org/opentcs/guing/res/symbols/dock/icons.ini", null, CAubotTheme.class.getClassLoader() );
    for( Map.Entry<String, Icon> entry : icons.entrySet() ){
      manager.setIconTheme( entry.getKey(), entry.getValue() );
    }
    ActionViewConverter converter = controller.getActionViewConverter();
    converter.putTheme(CPanelPopup.PANEL_POPUP, ViewTarget.TITLE, new BasicPanelPopupGenerator());
    converter.putTheme(CPanelPopup.PANEL_POPUP, ViewTarget.MENU, new PanelMenuGenerator());
    converter.putTheme(CPanelPopup.PANEL_POPUP, ViewTarget.DROP_DOWN, new PanelDropDownGenerator());
  }

  @Override
  public void uninstall(DockController controller) {
    ActionViewConverter converter = controller.getActionViewConverter();
    controller.getIcons().clear(Priority.THEME);
    converter.putTheme(CPanelPopup.PANEL_POPUP, ViewTarget.TITLE, null);
    converter.putTheme(CPanelPopup.PANEL_POPUP, ViewTarget.MENU, null);
    converter.putTheme(CPanelPopup.PANEL_POPUP, ViewTarget.DROP_DOWN, null);
    super.uninstall(controller);
  }
}
