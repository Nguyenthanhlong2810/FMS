/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action;

import java.awt.Rectangle;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.MENU_PATH;

import javax.inject.Inject;
import javax.swing.*;

import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.event.ToolAdapter;
import org.jhotdraw.draw.event.ToolEvent;
import org.jhotdraw.draw.event.ToolListener;
import org.jhotdraw.draw.tool.CreationTool;
import org.jhotdraw.draw.tool.Tool;
import org.jhotdraw.gui.JPopupButton;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.action.actions.CreateBlockAction;
import org.opentcs.guing.application.action.actions.CreateGroupAction;
import org.opentcs.guing.application.action.actions.CreateLocationTypeAction;
import org.opentcs.guing.application.action.actions.CreateTransportOrderAction;
import org.opentcs.guing.application.action.actions.CreateVehicleAction;
import org.opentcs.guing.application.action.course.VehicleRoutesAction;
import org.opentcs.guing.application.action.draw.DefaultPathSelectedAction;
import org.opentcs.guing.application.action.draw.DefaultPointSelectedAction;
import org.opentcs.guing.application.action.draw.SelectSameAction;
import org.opentcs.guing.application.action.view.FindVehicleAction;
import org.opentcs.guing.application.action.view.EnableAllVehiclesAction;
import org.opentcs.guing.application.action.view.DisableAllVehiclesAction;
import org.opentcs.guing.application.toolbar.DragTool;
import org.opentcs.guing.application.toolbar.MultipleSelectionTool;
import org.opentcs.guing.application.toolbar.OpenTCSConnectionTool;
import org.opentcs.guing.application.toolbar.SelectionToolFactory;
import org.opentcs.guing.communication.CreateVehicleInOperatingAction;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.LinkConnection;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.event.ResetInteractionToolCommand;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.util.CourseObjectFactory;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.event.EventHandler;

/**
 * Sets up and manages a list of tool bars in the graphical user interface.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ToolBarManager
    implements EventHandler {

  /**
   * A factory for selectiont tools.
   */
  private final SelectionToolFactory selectionToolFactory;
  /**
   * A list of all toolbars.
   */
  private final List<JToolBar> toolBarList
      = Collections.synchronizedList(new LinkedList<JToolBar>());
  /**
   * A list of all separators.
   */
  private final List<JToolBar.Separator> separatorList = new ArrayList<>();
  /**
   * A tool bar for actions creating new items.
   */
  private final JToolBar toolBarCreation = new JToolBar();
  /**
   * A tool bar for actions regarding alignment.
   */
  private final JToolBar toolBarAlignment = new JToolBar();
  /**
   * A toggle button for the selection tool.
   */
  private final JToggleButton selectionToolButton;
  /**
   * A toggle button for the drag tool.
   */
  private final JToggleButton dragToolButton;
  /**
   * The actual drag tool.
   */
  private DragTool dragTool;
  /**
   * A button for creating points.
   * Available in modelling mode only.
   */
  private final JPopupButton buttonCreatePoint;
  /**
   * A button for creating locations.
   * Available in modelling mode only.
   */
  private final JToggleButton buttonCreateLocation;
  /**
   * A button for creating paths.
   * Available in modelling mode only.
   */
  private final JPopupButton buttonCreatePath;
  /**
   * A button for creating location links.
   * Available in modelling mode only.
   */
  private final JToggleButton buttonCreateLink;
  /**
   * A button for creating location types.
   * Available in modelling mode only.
   */
  private final JButton buttonCreateLocationType;
  /**
   * A button for creating vehicles.
   * Available in modelling mode only.
   */
  private final JButton buttonCreateVehicle;
  /**
   * A button for creating blocks.
   * Available in modelling mode only.
   */
  private final JButton buttonCreateBlock;
  /**
   * A button for creating groups.
   * Available in modelling mode.
   */
  private final JButton buttonCreateGroup;
  /**
   * A button for creating transport orders.
   * Available in operating mode.
   */
  private final JButton buttonCreateOrder;
  /**
   * A button for finding vehicles.
   * Available in operating mode.
   */
  private final JButton buttonFindVehicle;
  /**
   * A button for stop all vehicles.
   * Available in operating mode.
   */
  private final JButton buttonDisableAllVehicles;
  /**
   * A button for move all vehicles.
   * Available in operating mode.
   */
  private final JButton buttonEnableAllVehicles;
  /**
   * A button for vehicle routes table.
   * Available in operating mode.
   */
  private final JButton buttonVehicleRoutes;

  private final JButton buttonCreateVehicleInOperating;

  private final JButton buttonLogin;

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);
  /**
   * Creates a new instance.
   *
   * @param actionMap The action map to be used
   * @param crsObjFactory A factory for course objects
   * @param editor The drawing editor
   * @param selectionToolFactory The selection tool factory
   */
  @Inject
  public ToolBarManager(ViewActionMap actionMap,
                        CourseObjectFactory crsObjFactory,
                        OpenTCSDrawingEditor editor,
                        SelectionToolFactory selectionToolFactory,
                        OpenTCSView openTCSView,
                        ApplicationState applicationState) {
    requireNonNull(actionMap, "actionMap");
    requireNonNull(crsObjFactory, "crsObjFactory");
    requireNonNull(editor, "editor");
    requireNonNull(openTCSView, "view");
    requireNonNull(applicationState,"applicationState");
    this.selectionToolFactory = requireNonNull(selectionToolFactory,
                                               "selectionToolFactory");

    ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverview.TOOLBAR_PATH);

    // --- 1. ToolBar: Creation ---
    // Selection, Drag | Create Point, Location, Path, Link | 
    // Create Location Type, Vehicle, Block, Static Route | 
    // Create Transport Order | Find, Show Vehicles

    toolBarCreation.setActionMap(actionMap);
    // --- Selection Tool ---
    selectionToolButton = addSelectionToolButton(toolBarCreation, editor);
    // --- Drag Tool ---
    dragToolButton = addDragToolButton(toolBarCreation, editor);

    toolBarCreation.addSeparator();

    // --- Create Point Figure (only in Modelling mode) ---
    LabeledPointFigure lpf = crsObjFactory.createPointFigure();
    CreationTool creationTool = new CreationTool(lpf);
    buttonCreatePoint = pointToolButton(toolBarCreation, editor, creationTool);
    creationTool.setToolDoneAfterCreation(false);

    // --- Create Location Figure (only in Modelling mode) ---
    LabeledLocationFigure llf = crsObjFactory.createLocationFigure();
    creationTool = new CreationTool(llf);
    buttonCreateLocation = addToolButton(toolBarCreation,
                                         editor,
                                         creationTool,
                                         labels.getString("toolBarManager.button_createLocation.tooltipText"),
                                         ImageDirectory.getImageIcon("/toolbar/location.22.png"));
    creationTool.setToolDoneAfterCreation(false);

    // --- Create Path Figure (only in Modelling mode) ---
    PathConnection pc = crsObjFactory.createPathConnection();
    OpenTCSConnectionTool connectionTool = new OpenTCSConnectionTool(pc);
    buttonCreatePath = pathToolButton(toolBarCreation, editor, connectionTool);
    connectionTool.setToolDoneAfterCreation(false);

    // --- Create Link (only in Modelling mode) ---
    LinkConnection lc = crsObjFactory.createLinkConnection();
    connectionTool = new OpenTCSConnectionTool(lc);
    buttonCreateLink = addToolButton(toolBarCreation,
                                     editor,
                                     connectionTool,
                                     labels.getString("toolBarManager.button_createLink.tooltipText"),
                                     ImageDirectory.getImageIcon("/toolbar/link.22.png"));
    connectionTool.setToolDoneAfterCreation(false);

    JToolBar.Separator sep = new JToolBar.Separator();
    separatorList.add(sep);
    toolBarCreation.add(sep);

    // --- Location Type: No Figure, just creates a tree entry (only in Modelling mode) ---
    buttonCreateLocationType = new JButton(actionMap.get(CreateLocationTypeAction.ID));
    buttonCreateLocationType.setText(null);
    //toolBarCreation.add(buttonCreateLocationType);

    // --- Create Vehicle Figure (only in Modelling mode) ---
    buttonCreateVehicle = new JButton(actionMap.get(CreateVehicleAction.ID));
    buttonCreateVehicle.setText(null);
//    toolBarCreation.add(buttonCreateVehicle);

    // --- Create Block (only in Modelling mode) ---
    buttonCreateBlock = new JButton(actionMap.get(CreateBlockAction.ID));
    buttonCreateBlock.setText(null);
    toolBarCreation.add(buttonCreateBlock);

    // --- Create Group (both modes) ---
    buttonCreateGroup = new JButton(actionMap.get(CreateGroupAction.ID));
    buttonCreateGroup.setText(null);
    toolBarCreation.add(buttonCreateGroup);

    sep = new JToolBar.Separator();
    separatorList.add(sep);
    toolBarCreation.add(sep);

    // --- Create Transport Order (only in Operating mode) ---

    buttonCreateVehicleInOperating = new JButton(actionMap.get(CreateVehicleInOperatingAction.ID));
    toolBarCreation.add(buttonCreateVehicleInOperating);

    buttonCreateOrder = new JButton(actionMap.get(CreateTransportOrderAction.ID));
    buttonCreateOrder.setText(null);
//    toolBarCreation.add(buttonCreateOrder);

//    toolBarCreation.addSeparator();

    // --- Find Vehicle (only in Operating mode) ---
    buttonFindVehicle = new JButton(actionMap.get(FindVehicleAction.ID));
    buttonFindVehicle.setText(null);
//    toolBarCreation.add(buttonFindVehicle);

//    toolBarCreation.addSeparator();

    // --- Move and Stop All Vehicles (only in Operating mode) ---
    buttonEnableAllVehicles = new JButton(actionMap.get(EnableAllVehiclesAction.ID));
    buttonEnableAllVehicles.setText(null);
    toolBarCreation.add(buttonEnableAllVehicles);

    buttonDisableAllVehicles = new JButton(actionMap.get(DisableAllVehiclesAction.ID));
    buttonDisableAllVehicles.setText(null);
    toolBarCreation.add(buttonDisableAllVehicles);

    toolBarCreation.addSeparator();

    buttonVehicleRoutes = new JButton(actionMap.get(VehicleRoutesAction.ID));
    buttonVehicleRoutes.setText(null);
    toolBarCreation.add(buttonVehicleRoutes);
//    buttonSwitchMode.setBorder(BorderFactory.createCompoundBorder(buttonSwitchMode.getBorder(),
//            BorderFactory.createEmptyBorder(4, 4, 4, 4)));
//    toolBarCreation.add(buttonSwitchMode);
    toolBarCreation.setName(labels.getString("toolBarManager.toolbar_drawing.title"));
    toolBarList.add(toolBarCreation);

    buttonLogin = new JButton("Login");
    buttonLogin.setToolTipText("Login");
    buttonLogin.addActionListener(e->{
      if(applicationState.getOperationMode() == OperationMode.OPERATING){
        //openTCSView.switchPlantOverviewState(OperationMode.MODELLING);
        buttonLogin.setToolTipText("Login");
      }
    });
    // --- 2. ToolBar: Attributes ---
    // TODO: Diesen Toolbar "später" wieder einfügen, sobald es freie Grafikelemente im Modell gibt
    // Pick, Apply
    // Color: Stroke, Fill, Text
    // Stroke: Decoration, Width, Dashes, Type, Placement, Cap, Join
    // Font: Font; Bold, Italic, Underline
//    JToolBar toolBarAttributes = new JToolBar();
//    ButtonFactory.addAttributesButtonsTo(toolBarAttributes, editor);
//    toolBarAttributes.setName(labels.getString("toolBarManager.toolbar_attributes.title"));
//    toolBarList.add(toolBarAttributes);
    // --- 3. ToolBar: Alignment (nur im Modelling Mode) ---
    // Align: West, East, Horizontal; North, South, Vertical
    // Move: West, East, North, South
    // Bring to front, Send to back
    ButtonFactory.addAlignmentButtonsTo(toolBarAlignment, editor);
    toolBarAlignment.setName(labels.getString("toolBarManager.toolbar_alignment.title"));
    toolBarList.add(toolBarAlignment);
  }

  public List<JToolBar> getToolBars() {
    return toolBarList;
  }

  public JToolBar getToolBarCreation() {
    return toolBarCreation;
  }

  public JToggleButton getSelectionToolButton() {
    return selectionToolButton;
  }

  public JToggleButton getDragToolButton() {
    return dragToolButton;
  }

  public JPopupButton getButtonCreatePath() {
    return buttonCreatePath;
  }

  public JToggleButton getButtonCreateLink() {
    return buttonCreateLink;
  }

  public JButton getButtonLogin(){
    return buttonLogin;
  }
  @Override
  public void onEvent(Object event) {
    if (event instanceof OperationModeChangeEvent) {
      handleModeChange((OperationModeChangeEvent) event);
    }
    if (event instanceof ResetInteractionToolCommand) {
      handleToolReset((ResetInteractionToolCommand) event);
    }
  }

  private void handleModeChange(OperationModeChangeEvent evt) {
    setOperationMode(evt.getNewMode());
  }

  private void handleToolReset(ResetInteractionToolCommand evt) {
    selectionToolButton.setSelected(true);
  }

  public void setOperationMode(OperationMode mode) {
//    toolBarAttributes.setVisible(mode == OperationMode.MODELLING);
    toolBarAlignment.setVisible(mode == OperationMode.MODELLING);
    // Items in Toolbar "Create"
    buttonCreatePoint.setVisible(mode == OperationMode.MODELLING);
    buttonCreateLocation.setVisible(mode == OperationMode.MODELLING);
    buttonCreatePath.setVisible(mode == OperationMode.MODELLING);
    buttonCreateLink.setVisible(mode == OperationMode.MODELLING);
    buttonCreateLocationType.setVisible(mode == OperationMode.MODELLING);
    buttonCreateVehicle.setVisible(mode == OperationMode.MODELLING);
    buttonCreateBlock.setVisible(mode == OperationMode.MODELLING);
    buttonCreateGroup.setVisible(mode == OperationMode.MODELLING);
    buttonCreateVehicleInOperating.setVisible(mode == OperationMode.OPERATING);

    for (JToolBar.Separator sep : separatorList) {
      sep.setVisible(mode == OperationMode.MODELLING);
    }

    buttonCreateOrder.setEnabled(mode == OperationMode.OPERATING);
    buttonFindVehicle.setEnabled(mode == OperationMode.OPERATING);
    buttonEnableAllVehicles.setVisible(mode == OperationMode.OPERATING);
    buttonDisableAllVehicles.setVisible(mode == OperationMode.OPERATING);
    buttonVehicleRoutes.setVisible(mode == OperationMode.OPERATING);
  }

  /**
   * Adds the selection tool to the given toolbar.
   *
   * @param toolBar The toolbar to add to.
   * @param editor The DrawingEditor.
   */
  private JToggleButton addSelectionToolButton(JToolBar toolBar,
                                               DrawingEditor editor) {
    LinkedList<Action> drawingActions = new LinkedList<>();
    // Drawing Actions
    drawingActions.add(new SelectSameAction(editor));

    MultipleSelectionTool selectionTool
        = selectionToolFactory.createMultipleSelectionTool(drawingActions, new LinkedList<>());

    ButtonGroup buttonGroup;

    if (toolBar.getClientProperty("toolButtonGroup") instanceof ButtonGroup) {
      buttonGroup = (ButtonGroup) toolBar.getClientProperty("toolButtonGroup");
    }
    else {
      buttonGroup = new ButtonGroup();
      toolBar.putClientProperty("toolButtonGroup", buttonGroup);
    }

    // Selection tool
    editor.setTool(selectionTool);
    final JToggleButton toggleButton = new JToggleButton();

    if (!(toolBar.getClientProperty("toolHandler") instanceof ToolListener)) {
      ToolListener toolHandler = new ToolAdapter() {
        @Override
        public void toolDone(ToolEvent event) {
          toggleButton.setSelected(true);
        }
      };

      toolBar.putClientProperty("toolHandler", toolHandler);
    }

    toggleButton.setIcon(ImageDirectory.getImageIcon("/toolbar/cursor_24px.png"));
    toggleButton.setText(null);
    toggleButton.setToolTipText(ResourceBundleUtil.getBundle(I18nPlantOverview.TOOLBAR_PATH)
        .getString("toolBarManager.button_selectionTool.tooltipText"));

    toggleButton.setSelected(true);
    toggleButton.addItemListener(new ToolButtonListener(selectionTool, editor));
//    toggleButton.setFocusable(false);
    buttonGroup.add(toggleButton);
    toolBar.add(toggleButton);

    return toggleButton;
  }

  /**
   *
   * @param toolBar
   * @param editor
   */
  private JToggleButton addDragToolButton(JToolBar toolBar, DrawingEditor editor) {
    final JToggleButton button = new JToggleButton();
    dragTool = new DragTool();
    editor.setTool(dragTool);

    if (!(toolBar.getClientProperty("toolHandler") instanceof ToolListener)) {
      ToolListener toolHandler = new ToolAdapter() {
        @Override
        public void toolDone(ToolEvent event) {
          button.setSelected(true);
        }
      };
      toolBar.putClientProperty("toolHandler", toolHandler);
    }

    URL url = getClass().getResource(ImageDirectory.DIR + "/toolbar/hand_24px.png");
    button.setIcon(new ImageIcon(url));
    button.setText(null);
    button.setToolTipText(ResourceBundleUtil.getBundle(I18nPlantOverview.TOOLBAR_PATH)
        .getString("toolBarManager.button_dragTool.tooltipText"));

    button.setSelected(false);
    button.addItemListener(new ToolButtonListener(dragTool, editor));
//    button.setFocusable(false);

    ButtonGroup group = (ButtonGroup) toolBar.getClientProperty("toolButtonGroup");
    group.add(button);
    toolBar.add(button);
    return button;
  }

  /**
   * Configures a JPopupButton with all available Point types.
   *
   * @param toolBar
   * @param editor OpenTCSDrawingEditor
   * @param tool CreationTool
   * @return
   */
  private JPopupButton pointToolButton(JToolBar toolBar,
                                       DrawingEditor editor,
                                       Tool tool) {
    JPopupButton popupButton = new JPopupButton();
    ButtonGroup group = (ButtonGroup) toolBar.getClientProperty("toolButtonGroup");
    popupButton.setAction(new DefaultPointSelectedAction(editor, tool, popupButton, group),
                          new Rectangle(0, 0, 16, 16));
    ToolListener toolHandler = (ToolListener) toolBar.getClientProperty("toolHandler");
    tool.addToolListener(toolHandler);

//    for (PointModel.Type type : PointModel.Type.values()) {
//      DefaultPointSelectedAction action
//          = new DefaultPointSelectedAction(editor, tool, type, popupButton, group);
//      popupButton.add(action);
//      action.setEnabled(true);
//    }
    DefaultPointSelectedAction action
            = new DefaultPointSelectedAction(editor, tool, PointModel.Type.HALT, popupButton, group);
    popupButton.add(action);
    action.setEnabled(true);

    popupButton.setText(null);
    popupButton.setToolTipText(PointModel.Type.values()[0].getHelptext());
    popupButton.setIcon(ImageDirectory.getImageIcon("/toolbar/point.png"));
    popupButton.setFocusable(true);

    group.add(popupButton);
    toolBar.add(popupButton);

    return popupButton;
  }

  /**
   * Method addSelectionToolButton must have been invoked prior to this on the
   * JToolBar.
   *
   * @param toolBar
   * @param editor
   * @param tool
   * @param toolTipText
   * @return
   */
  private JToggleButton addToolButton(JToolBar toolBar,
                                      DrawingEditor editor,
                                      Tool tool,
                                      String toolTipText,
                                      ImageIcon iconBase) {
    JToggleButton toggleButton = new JToggleButton();

    toggleButton.setIcon(iconBase);
    toggleButton.setText(null);
    toggleButton.setToolTipText(toolTipText);
    toggleButton.addItemListener(new ToolButtonListener(tool, editor));
//    toggleButton.setFocusable(false);

    ToolListener toolHandler = (ToolListener) toolBar.getClientProperty("toolHandler");
    tool.addToolListener(toolHandler);

    ButtonGroup group = (ButtonGroup) toolBar.getClientProperty("toolButtonGroup");
    group.add(toggleButton);
    toolBar.add(toggleButton);

    return toggleButton;
  }

  /**
   * Configures a JPopupButton with all available path types.
   *
   * @param toolBar
   * @param editor
   * @param tool
   * @return
   */
  private JPopupButton pathToolButton(JToolBar toolBar,
                                      DrawingEditor editor,
                                      Tool tool) {
    JPopupButton popupButton = new JPopupButton();
    ButtonGroup group = (ButtonGroup) toolBar.getClientProperty("toolButtonGroup");
    popupButton.setAction(new DefaultPathSelectedAction(editor, tool, popupButton, group),
                          new Rectangle(0, 0, 16, 16));
    ToolListener toolHandler = (ToolListener) toolBar.getClientProperty("toolHandler");
    tool.addToolListener(toolHandler);

//    for (PathModel.Type type : PathModel.Type.values()) {
//      DefaultPathSelectedAction action
//          = new DefaultPathSelectedAction(editor, tool, type, popupButton, group);
//      popupButton.add(action);
//      action.setEnabled(true);
//    }
    DefaultPathSelectedAction action = new DefaultPathSelectedAction(editor, tool, PathModel.Type.DIRECT, popupButton, group);
    popupButton.add(action);
    action.setEnabled(true);

    action = new DefaultPathSelectedAction(editor, tool, PathModel.Type.BEZIER, popupButton, group);
    popupButton.add(action);
    action.setEnabled(true);

    action = new DefaultPathSelectedAction(editor, tool, PathModel.Type.BEZIER_3, popupButton, group);
    popupButton.add(action);
    action.setEnabled(true);

    popupButton.setText(null);
    popupButton.setToolTipText(PathModel.Type.values()[0].getHelptext());
    popupButton.setIcon(ImageDirectory.getImageIcon("/toolbar/line.png"));
    popupButton.setFocusable(true);

    group.add(popupButton);
    toolBar.add(popupButton);

    return popupButton;
  }
}
