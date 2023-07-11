/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dialogs;

import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.action.report.config.MyTableCellRender;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.properties.table.ColorPropertyCellEditor;
import org.opentcs.guing.components.properties.table.ColorPropertyCellRenderer;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.IntegerProperty;
import org.opentcs.guing.components.routing.*;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.*;
import org.opentcs.util.gui.Icons;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.CREATEGROUP_PATH;

/**
 * A panel to create a group.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class CreateGroupPanel
    extends javax.swing.JDialog {

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(CREATEGROUP_PATH);
  /**
   * The application's main view.
   */
  private final OpenTCSView view;
  /**
   * Provides the current system model.
   */
  private final ModelManager modelManager;
  private Graph mapGraph = new Graph();
  private Map<Edge, PathConnection> edges = new HashMap<>();
  private List<PointModel> points;
  private List<PointModel> startPoints = new ArrayList<>();
  private RouteTableModel tableModel;
  private List<Graph.Route> foundedRoutes = new ArrayList<>();
  private Boolean headerCheckAll = true;
  private final List<Set<String>> createdRoutes;
  /**
   * Creates new form CreateGroupPanel.
   *
   * @param view The application's main view.
   * @param modelManager Provides the current system model.
   */
  @Inject
  public CreateGroupPanel(OpenTCSView view, ModelManager modelManager, @ApplicationFrame JFrame applicationFrame) {
    super(applicationFrame, true);
    this.view = requireNonNull(view, "view");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    initComponents();
    setIconImages(Icons.getOpenTCSIcons());
    setTitle(ResourceBundleUtil.getBundle(CREATEGROUP_PATH)
        .getString("createGroupPanel.title"));
    initLists();
    initMap();
    initListenerTable();
    createdRoutes = modelManager.getModel().getGroupModels().stream()
            .map(group -> group.getChildComponents().stream()
                    .filter(mem -> mem instanceof PointModel)
                    .map(ModelComponent::getName)
                    .collect(Collectors.toSet()))
            .collect(Collectors.toList());
  }

  /**
   * Initializes all lists with values.
   */
  private void initLists() {
    SystemModel systemModel = modelManager.getModel();
    points = systemModel.getPointModels();
    Collections.sort(points, Comparators.modelComponentsByName());
    for (PointModel model : points) {
      pointCbx.addItem(model.getName());
    }
    revalidate();
  }

  private void addItemToPointPanel(String point){
    PointModel pointModel = modelManager.getModel().getPointModel(point);
    startPoints.add(pointModel);
    JButton btnSelectedPoint = new JButton(point);
    ImageIcon icon = ImageDirectory.getImageIcon("/menu/close.png");
    btnSelectedPoint.setIcon(icon);
    btnSelectedPoint.setHorizontalTextPosition(SwingConstants.LEADING);
    btnSelectedPoint.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        startPoints.removeIf(pm -> pm.getName().equals(point));
        if (startPoints.size() > 0) {
          //loai bo bot row
          resetRoutesColor(foundedRoutes);
          calculateRoutes(startPoints.get(0).getName());
          for (PointModel model : startPoints) {
            findSuitableRoute(model.getName());
          }
//          highlightAllRoute();
        } else {
          setDefaultRoute(foundedRoutes);
          foundedRoutes = new ArrayList<>();
          tableModel.refresh(new ArrayList<>());
        }
        pointCbx.setSelectedItem("");
        selectedPointPanel.remove(e.getComponent());
        selectedPointPanel.revalidate();
      }
    });
    selectedPointPanel.add(btnSelectedPoint, selectedPointPanel.getComponents().length - 1);
    pointCbx.setSelectedItem("");

    if(startPoints.size() == 1) {
        calculateRoutes(startPoints.get(0).getName());
    }else{
        setDefaultRoute(foundedRoutes);
        findSuitableRoute(point);
    }
//    highlightAllRoute();
  }

  private void findSuitableRoute(String point){
    List<Graph.Route> routes = new ArrayList<>();
    for (Graph.Route foundedRoute : foundedRoutes) {
      if (checkPointInRoute(point, foundedRoute)) {
        routes.add(foundedRoute);
      }
    }
    foundedRoutes = new ArrayList<>();
    foundedRoutes.addAll(routes);
    setDataTable();
  }
  private boolean checkPointInRoute(String point, Graph.Route route){
     return getEdgeList(route).contains(point);
  }
  private void initMap(){
        List<PathModel> pathModelList = modelManager.getModel().getPathModels();
        for (PathModel pathModel : pathModelList) {
          Edge edge = new Edge(pathModel.getStartComponent().getName(),pathModel.getEndComponent().getName());
          PathConnection pathConnection = (PathConnection) modelManager.getModel().getFigure(pathModel);

          edges.put(edge,pathConnection);
          mapGraph.addEdge(edge);
        }
  }
  private void updateRouteCost(Graph.Route route) {
    double length = route.getRoute().stream().mapToDouble(edge -> (double) edges.get(edge).calculateLength().getValue()).sum();
    route.setCost((int) Math.round(length));
  }

  private boolean checkPointExisted(String point){
    for (PointModel pointModel : startPoints){
      if(pointModel.getName().equals(point)){
        return true;
      }
    }
    return false;
  }

  private void calculateRoutes(String startPoint) {
    foundedRoutes = new ArrayList<>();
    Node start = mapGraph.findNode(startPoint);
    if (start == null) {
      JOptionPane.showMessageDialog(this, "Point not found", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    mapGraph.searchForCycles(start);
    foundedRoutes = mapGraph.getFoundedRoutes().stream()
            .filter(r -> createdRoutes.stream()
                    .noneMatch(cr -> cr.containsAll(r.getRoute().stream()
                            .map(Edge::getStart)
                            .collect(Collectors.toSet()))))
            .collect(Collectors.toList());
    for (Graph.Route route : foundedRoutes) {
      updateRouteCost(route);
    }
    setDataTable();
  }
  private void setDataTable(){
    List<GroupModel> routes = foundedRoutes.stream().map(route -> {
      GroupModel routeModel = new GroupModel();
      routeModel.setProperty("cost", new IntegerProperty(routeModel, route.getCost()));
      routeModel.getPropertyColor().setColor(new Color(new Random().nextInt(0xffffff)));
      convertEdgeToPoint(route.getRoute()).forEach(routeModel::add);
      convertEdgeToPath(route.getRoute()).forEach(routeModel::add);

      return routeModel;
    }).collect(Collectors.toList());
    tableModel.refresh(routes);
    revalidate();
  }

  private void initListenerTable(){
    routesTbl.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int row = routesTbl.getSelectedRow();
//        int column = ROUTE_TABLE_COLUMN_CHECK;
//        if(row >= 0){
//          if(!(Boolean) routesTbl.getValueAt(row,column)) {
//            foundedRoutes.get(row).setHighlight((Boolean) routesTbl.getValueAt(row, column));
//            highlightRoute(foundedRoutes.get(row));
//            highlightAllRoute();
//          }else{
//            highlightAllRoute();
//            foundedRoutes.get(row).setHighlight((Boolean) routesTbl.getValueAt(row, column));
//            highlightRoute(foundedRoutes.get(row));
//          }
//        }
        view.focusGroup(tableModel.get(row), -1);
      }

      @Override
      public void mousePressed(MouseEvent e) {

      }

      @Override
      public void mouseReleased(MouseEvent e) {

      }

      @Override
      public void mouseEntered(MouseEvent e) {

      }

      @Override
      public void mouseExited(MouseEvent e) {

      }
    });
  }

  private void resetRoutesColor(List<Graph.Route> foundedRoutes){
    if(foundedRoutes.size() == 0){
      return;
    }
    for (Graph.Route foundedRoute : foundedRoutes) {
      foundedRoute.setColor(new ColorProperty(null,Color.black));
      foundedRoute.setHighlight(true);
    }
//    highlightAllRoute();
  }

  private void highlightAllRoute(){
    if(foundedRoutes.size() == 0){
      return;
    }
    for (Graph.Route route : foundedRoutes){
      highlightRoute(route);
    }
  }

  private void setDefaultRoute(List<Graph.Route> foundedRoutes){
    for (Graph.Route route : foundedRoutes){
      List<Edge> edges = route.getRoute();
      List<PathModel> pathModels = convertEdgeToPath(edges);
      view.resetRouteColor(pathModels);
    }
  }

  public void highlightRoute(Graph.Route foundedRoute){
      List<Edge> edges = foundedRoute.getRoute();
      List<PathModel> pathModels = convertEdgeToPath(edges);
      view.highlightRoute(pathModels,foundedRoute.isHighlight(),
              foundedRoute.getColor().getColor());
  }

  private List<String> getEdgeList(Graph.Route foundedRoute){
      List<String> edges = new ArrayList<>();
      List<Edge> edgeList = foundedRoute.getRoute();
      for(Edge edge : edgeList){
          edges.add(edge.getStart());
      }
      return edges;
  }

  private List<PointModel> convertEdgeToPoint(List<Edge> edges){
    Map<String, PointModel> pointsName = modelManager.getModel().getPointModels().stream()
            .collect(Collectors.toMap(PointModel::getName, p -> p));
    List<PointModel> resultPoint = edges.stream()
            .map(edge -> pointsName.get(edge.getStart()))
            .collect(Collectors.toList());
    return resultPoint;
  }

  private List<PathModel> convertEdgeToPath(List<Edge> edges){
    Map<Map.Entry<String, String>, PathModel> allPath = modelManager.getModel().getPathModels().stream()
            .collect(Collectors.toMap(path -> new AbstractMap.SimpleEntry<>(
                    path.getStartComponent().getName(), path.getEndComponent().getName()),
                    path -> path));
    List<PathModel> pathModels = new ArrayList<>();
    for(Edge edge : edges){
      pathModels.add(allPath.get(new AbstractMap.SimpleEntry<>(edge.getStart(), edge.getEnd())));
    }
    return pathModels;
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
    // CHECKSTYLE:OFF
    // Variables declaration - do not modify//GEN-BEGIN:variables
    JPanel buttonPanel = new JPanel();
    JButton createGroupButton = new JButton();
    JButton cancelButton = new JButton();

    setMinimumSize(new java.awt.Dimension(500, 500));

    setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
    this.setLayout(new BorderLayout());
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/dialogs/createGroup"); // NOI18N

    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new BorderLayout());
    JLabel lblStartPoint = new JLabel(bundle.getString("createGroupPanel.label_points.text"));
    inputPanel.add(lblStartPoint, BorderLayout.WEST);

    selectedPointPanel = new JPanel();
    selectedPointPanel.setPreferredSize(new Dimension(0, 80));
    selectedPointPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
    selectedPointPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    pointCbx = new JComboBox<>();
    pointCbx.addItem("");
    selectedPointPanel.add(pointCbx);
    pointCbx.addActionListener(l->{
      if(!Objects.equals(pointCbx.getSelectedItem(), "") && !checkPointExisted((String) pointCbx.getSelectedItem())) {
        addItemToPointPanel((String) pointCbx.getSelectedItem());
        revalidate();
      }else {
        pointCbx.setSelectedItem("");
      }
    });
//    inputPanel.add(startPointsPanel);
    inputPanel.add(selectedPointPanel);
    this.add(inputPanel, BorderLayout.NORTH);

    tableModel = new RouteTableModel();
    routesTbl = new JTable(tableModel);
    routesTbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    routesTbl.setShowGrid(true);
    JScrollPane routesPanel = new JScrollPane(routesTbl);
    routesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),bundle.getString("createGroupPanel.panel_routes.title")));
    routesTbl.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//    tableModel.addColumn(bundle.getString("createGroupPanel.table_routes.column_checkAll"));
//    tableModel.addColumn(bundle.getString("createGroupPanel.table_routes.column_name"));
//    tableModel.addColumn(bundle.getString("createGroupPanel.table_routes.column_cost"));
//    tableModel.addColumn(bundle.getString("createGroupPanel.table_routes.column_color"));
    configTable(routesTbl);
    TableColumn tc = routesTbl.getColumnModel().getColumn(RouteTableModel.ROUTE_TABLE_COLUMN_CHECK);
    tc.setCellEditor(routesTbl.getDefaultEditor(Boolean.class));
    tc.setCellRenderer(routesTbl.getDefaultRenderer(Boolean.class));
    tc.setHeaderRenderer(new CheckBoxHeader(new MyItemListener()));
    TableColumn color = routesTbl.getColumnModel().getColumn(RouteTableModel.ROUTE_TABLE_COLUMN_COLOR);
    color.setCellRenderer(new ColorPropertyCellRenderer());
    color.setCellEditor(new ColorPropertyCellEditor());

    this.add(routesPanel);
    buttonPanel.setLayout(new FlowLayout());
    createGroupButton.setText(bundle.getString("createGroupPanel.button_createGroup.text")); // NOI18N
    createGroupButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        createGroupButtonActionPerformed(evt);
      }
    });
    buttonPanel.add(createGroupButton);

    cancelButton.setText(bundle.getString("createGroupPanel.button_cancel.text")); // NOI18N
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cancelButtonActionPerformed(evt);
      }
    });
    buttonPanel.add(cancelButton);
    this.add(buttonPanel, BorderLayout.SOUTH);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        view.removeFocusGroup();
        setDefaultRoute(foundedRoutes);
      }
    });
  }// </editor-fold>//GEN-END:initComponents
  // CHECKSTYLE:ON

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    this.dispose();
  }//GEN-LAST:event_cancelButtonActionPerformed

  private void createGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createGroupButtonActionPerformed
    for(int i = 0; i < foundedRoutes.size(); i++){
      foundedRoutes.get(i).setName((String) routesTbl.getValueAt(i, RouteTableModel.ROUTE_TABLE_COLUMN_NAME));
    }
//    for (Graph.Route foundedRoute : foundedRoutes) {
//      if (foundedRoute.isHighlight()) {
//        List<ModelComponent> modelComponents = new ArrayList<>();
//        modelComponents.addAll(convertEdgeToPoint(foundedRoute.getRoute()));
//        modelComponents.addAll(convertEdgeToPath(foundedRoute.getRoute()));
//        view.createRoute(modelComponents, foundedRoute.getName(), foundedRoute.getColor());
//      }
//    }
    tableModel.getRoutes().stream().filter(GroupModel::isHighlight).forEach(view::createGroup);
    setDefaultRoute(foundedRoutes);
    cancelButtonActionPerformed(null);
  }//GEN-LAST:event_createGroupButtonActionPerformed

  private  JComboBox<String> pointCbx;
  private JPanel selectedPointPanel;
  private JTable routesTbl;

  class MyItemListener implements ItemListener {

    public void itemStateChanged(ItemEvent e) {
      Object source = e.getSource();
      if (!(source instanceof AbstractButton)) {
        return;
      }
      headerCheckAll = e.getStateChange() == ItemEvent.SELECTED;
      for (int x = 0, y = routesTbl.getRowCount(); x < y; x++) {
        routesTbl.setValueAt(headerCheckAll, x, RouteTableModel.ROUTE_TABLE_COLUMN_CHECK);
        foundedRoutes.get(x).setHighlight(headerCheckAll);
      }
//      highlightAllRoute();
    }
  }

  public void configTable(JTable table) {

    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setHorizontalAlignment(JLabel.CENTER);
    renderer.setForeground(new Color(31, 72, 219));
    renderer.setBackground(new Color(222, 219, 217));
    renderer.setHorizontalAlignment(JLabel.CENTER);
    table.getTableHeader().setDefaultRenderer(renderer);
    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
      {
        final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setHorizontalAlignment(CENTER);
        return c;
      }
    });
    MyTableCellRender cellRenderLeft = new MyTableCellRender();
    cellRenderLeft.setHorizontalAlignment(SwingConstants.LEFT);
    table.getColumnModel().getColumn(RouteTableModel.ROUTE_TABLE_COLUMN_CHECK).setPreferredWidth(30);
    table.getColumnModel().getColumn(RouteTableModel.ROUTE_TABLE_COLUMN_NAME).setPreferredWidth(200);
    table.getColumnModel().getColumn(RouteTableModel.ROUTE_TABLE_COLUMN_COST).setPreferredWidth(70);
    table.getColumnModel().getColumn(RouteTableModel.ROUTE_TABLE_COLUMN_COLOR).setPreferredWidth(200);
    table.setRowHeight(30);
  }

  private static class RouteTableModel extends AbstractTableModel {

    private static final int ROUTE_TABLE_COLUMN_CHECK = 0;
    private static final int ROUTE_TABLE_COLUMN_NAME = 1;
    private static final int ROUTE_TABLE_COLUMN_COST = 2;
    private static final int ROUTE_TABLE_COLUMN_COLOR = 3;

    private List<GroupModel> list = new ArrayList<>();

    public void refresh(List<GroupModel> groupModel) {
      list = groupModel;
      fireTableDataChanged();
    }

    public GroupModel get(int index) {
      return list.get(index);
    }

    @Override
    public int getRowCount() {
      return list.size();
    }

    @Override
    public int getColumnCount() {
      return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      GroupModel route = list.get(rowIndex);
      switch (columnIndex) {
        case ROUTE_TABLE_COLUMN_CHECK:
          return route.isHighlight();
        case ROUTE_TABLE_COLUMN_NAME:
          return route.getName();
        case ROUTE_TABLE_COLUMN_COST:
          return route.getProperty("cost");
        case ROUTE_TABLE_COLUMN_COLOR:
          return route.getPropertyColor();
        default:
          return null;
      }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      GroupModel route = list.get(rowIndex);
      switch (columnIndex) {
        case ROUTE_TABLE_COLUMN_CHECK:
          route.setIsHighlight((boolean) aValue);
          break;
        case ROUTE_TABLE_COLUMN_NAME:
          route.setName((String) aValue);
          break;
        case ROUTE_TABLE_COLUMN_COST:
          route.setProperty("cost", new IntegerProperty(route, (Integer) aValue));
          break;
        case ROUTE_TABLE_COLUMN_COLOR:
          route.getPropertyColor().setColor(((ColorProperty) aValue).getColor());
      }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      switch (columnIndex) {
        case ROUTE_TABLE_COLUMN_CHECK:
          return Boolean.class;
        case ROUTE_TABLE_COLUMN_NAME:
          return String.class;
        case ROUTE_TABLE_COLUMN_COST:
          return Integer.class;
        case ROUTE_TABLE_COLUMN_COLOR:
          return ColorProperty.class;
        default:
          return Object.class;
      }
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case ROUTE_TABLE_COLUMN_CHECK:
          return BUNDLE.getString("createGroupPanel.table_routes.column_checkAll");
        case ROUTE_TABLE_COLUMN_NAME:
          return BUNDLE.getString("createGroupPanel.table_routes.column_name");
        case ROUTE_TABLE_COLUMN_COST:
          return BUNDLE.getString("createGroupPanel.table_routes.column_cost");
        case ROUTE_TABLE_COLUMN_COLOR:
          return BUNDLE.getString("createGroupPanel.table_routes.column_color");
        default:
          return "???";
      }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return true;
    }

    public List<GroupModel> getRoutes() {
      return list;
    }
  }
}
