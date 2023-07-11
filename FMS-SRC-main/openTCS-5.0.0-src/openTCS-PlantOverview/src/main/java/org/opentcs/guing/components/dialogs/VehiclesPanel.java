/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dialogs;

import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.properties.SelectionPropertiesComponent;
import org.opentcs.guing.components.tree.ComponentsTreeViewManager;
import org.opentcs.guing.components.tree.TreeViewManager;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.event.SystemModelTransitionEvent;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.util.event.EventHandler;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Objects.requireNonNull;

/**
 * Shows every vehicle available in the system in a panel.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class VehiclesPanel
    extends JPanel
    implements EventHandler {

  /**
   * Provides the current system model.
   */
  private final ModelManager modelManager;
  /**
   * A factory for vehicle views.
   */
  private final SingleVehicleViewFactory vehicleViewFactory;
  /**
   * The vehicle views sorted.
   */

  private final TreeViewManager treeViewManager;

  private final SelectionPropertiesComponent propertiesComponent;

  private final SortedSet<SingleVehicleView2> vehicleViews = new TreeSet<>();

  private final OpenTCSDrawingEditor drawingEditor;

  private int indexOfVehicleInPanel = -1;

  public static final Color NORMAL_BORDER_COLOR = new Color(243,112,34);

  public static final Color HIGHLIGHT_BORDER_COLOR = new Color(0,153,220);

  private final ScrollablePanel panelVehicles = new ScrollablePanel();

  /**
   * Creates a new instance.
   *
   * @param modelManager Provides the current system model.
   * @param vehicleViewFactory A factory for vehicle views.
   */
  @Inject
  VehiclesPanel(ModelManager modelManager,
                SingleVehicleViewFactory vehicleViewFactory,
                ComponentsTreeViewManager treeViewManager,
                SelectionPropertiesComponent propertiesComponent,
                OpenTCSDrawingEditor drawingEditor) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.vehicleViewFactory = requireNonNull(vehicleViewFactory,
                                             "vehicleViewFactory");
    this.treeViewManager = requireNonNull(treeViewManager,"treeViewManager");
    this.propertiesComponent = requireNonNull(propertiesComponent,"propertiesComponent");
    this.drawingEditor = requireNonNull(drawingEditor,"drawingEditor");
    initComponents();
    scrollPaneVehicles.getVerticalScrollBar().setUnitIncrement(15);
    scrollPaneVehicles.setBackground(new Color(243,112,34));
    setPreferredSize(new Dimension(0, 97));
    setMinimumSize(new Dimension(140, 120));
    scrollPaneVehicles.setViewportView(panelVehicles);
  }
  @Override
  public void onEvent(Object event) {
    if (event instanceof OperationModeChangeEvent) {
      handleModeChange((OperationModeChangeEvent) event);
    }
    if (event instanceof SystemModelTransitionEvent) {
      handleSystemModelTransition((SystemModelTransitionEvent) event);
    }
  }

  private void handleModeChange(OperationModeChangeEvent evt) {
    switch (evt.getNewMode()) {
      case OPERATING:
        setVehicleModels(modelManager.getModel().getVehicleModels());
        break;
      case MODELLING:
      default:
        clearVehicles();
    }
  }

  /**
   * Initializes this panel with the current vehicles.
   *
   * @param vehicleModels The vehicle models.
   */
  public void setVehicleModels(Collection<VehicleModel> vehicleModels) {
    // Remove vehicles of the previous model from panel
    for (SingleVehicleView2 vehicleView : vehicleViews) {
      for (MouseListener mouseListener : vehicleView.getMouseListeners()) {
        vehicleView.removeMouseListener(mouseListener);
      }
      panelVehicles.remove(vehicleView);
    }

    // Remove vehicles of the previous model from list
    vehicleViews.clear();
    // Add vehicles of actual model to list
    for (VehicleModel vehicle : vehicleModels) {
      createVehicleView(vehicle);
    }

    // Add vehicles of actual model to panel, sorted by name
  }

  public void createVehicleView(VehicleModel vehicle) {
    SingleVehicleView2 svv = vehicleViewFactory.createSingleVehicleView(vehicle);
    vehicleViews.add(svv);
    panelVehicles.add(svv);
    MouseAdapter focusAdapter = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        focusVehicleInPanel(svv);
      }
    };
    svv.addMouseListener(focusAdapter);
    panelVehicles.revalidate();
    panelVehicles.repaint();
  }

  public SingleVehicleView2 getVehicleView(String name){
    for(SingleVehicleView2 SingleVehicleView2 : getVehicleViews()){
      if(SingleVehicleView2.getVehicleModel().getName().equals(name)){
        return SingleVehicleView2;
      }
    }
    return null;
  }

  /**
   * Clears the vehicles in this panel.
   */
  public void clearVehicles() {
    for (SingleVehicleView2 vehicleView : vehicleViews) {
      panelVehicles.remove(vehicleView);
    }
    vehicleViews.clear();
    repaint();
  }

  public void removeVehicle(SingleVehicleView2 vehicleView){
    panelVehicles.remove(vehicleView);
    revalidate();
    repaint();
  }

  private SingleVehicleView2 getSingleVehicleView2Current(int index){
    for(SingleVehicleView2 view : vehicleViews){
      int indexOfVehicleInPanelCurrent = new ArrayList<>(vehicleViews).indexOf(view);
      if(indexOfVehicleInPanelCurrent == index){
        return view;
      }
    }
    return null;
  }

  private SingleVehicleView2 getVehicleViewCurrent(int index){
    for(SingleVehicleView2 view : vehicleViews){
      int indexOfVehicleInPanelCurrent = new ArrayList<>(vehicleViews).indexOf(view);
      if(indexOfVehicleInPanelCurrent == index){
        return (SingleVehicleView2) view;
      }
    }
    return null;
  }

  public void focusVehicleInPanel(SingleVehicleView2 view){
    treeViewManager.selectItem(view.getVehicleModel());
    propertiesComponent.setModel(view.getVehicleModel());
    SingleVehicleView2 SingleVehicleView2 = getSingleVehicleView2Current(indexOfVehicleInPanel);
    SingleVehicleView2 vehicleView = getVehicleViewCurrent(indexOfVehicleInPanel);
    if((SingleVehicleView2 != null && SingleVehicleView2 != view) || (vehicleView != null && vehicleView != view)){
      SingleVehicleView2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(NORMAL_BORDER_COLOR),BorderFactory.createEmptyBorder(5,5,5,5)));
//      SingleVehicleView2.setBackground(new Color(243,112,34));
    }
    view.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(HIGHLIGHT_BORDER_COLOR, 3),
            BorderFactory.createEmptyBorder(5,5,5,5)));
//    view.setBackground(new Color(0,153,220));
    Rectangle rect = view.getBounds();
    Rectangle r2 = ((JViewport) panelVehicles.getParent()).getVisibleRect();
    panelVehicles.scrollRectToVisible(new Rectangle(rect.x, rect.y, (int) r2.getWidth(), (int) r2.getHeight()));
    indexOfVehicleInPanel = new ArrayList<>(vehicleViews).indexOf(view);
  }

  @Override
  public void repaint() {
    super.repaint();
    if (vehicleViews != null) {
      for (SingleVehicleView2 view : vehicleViews) {
        view.repaint();
      }
    }
  }

  private void handleSystemModelTransition(SystemModelTransitionEvent evt) {
    switch (evt.getStage()) {
      case UNLOADING:
        clearVehicles();
        break;
      case LOADED:
        setVehicleModels(modelManager.getModel().getVehicleModels());
        break;
      default:
      // Do nada.
    }
  }

  public SortedSet<SingleVehicleView2> getVehicleViews() {
    return vehicleViews;
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPaneVehicles = new javax.swing.JScrollPane();

        setName("VehiclesPanel"); // NOI18N
        setLayout(new java.awt.GridLayout(1, 0));
        add(scrollPaneVehicles);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/panels/vehicleView"); // NOI18N
        getAccessibleContext().setAccessibleName(bundle.getString("vehiclesPanel.title")); // NOI18N
        getAccessibleContext().setAccessibleDescription("");
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPaneVehicles;
    // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
