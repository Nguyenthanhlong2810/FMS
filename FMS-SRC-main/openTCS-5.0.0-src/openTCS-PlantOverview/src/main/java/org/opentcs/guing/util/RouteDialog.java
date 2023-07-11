/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.guing.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.TCSObject;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.util.gui.StringListCellRenderer;

/**
 *
 * @author yue
 */
public class RouteDialog
    extends javax.swing.JDialog {

  private static final Logger LOG = LoggerFactory.getLogger(RouteDialog.class);

  private final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.ROUTE_PATH);

  private final SharedKernelServicePortalProvider portalProvider;

  private static VehicleModel vehicle;

  private final ModelManager modelManager;

  private final OpenTCSDrawingEditor drawingEditor;

  private PointRouteTableModel model;

  private int pos;

  private static boolean active;

  private static List<ModelComponent> routePaths = new ArrayList<>();

  public static VehicleModel getVehicle() {
    return vehicle;
  }

  public static boolean isRouteActive() {
    return active;
  }

  public static List<ModelComponent> getRoutePaths() {
    return routePaths;
  }
  
  /**
   * Creates new form RouteDialog
   */
  public RouteDialog(VehicleModel vehicle,
                     SharedKernelServicePortalProvider portalProvider,
                     OpenTCSDrawingEditor drawingEditor,
                     ModelManager modelManager) {
    super((Frame) null, false);
    RouteDialog.vehicle = requireNonNull(vehicle, "vehicle");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    initComponents();
    setLocationRelativeTo(null);
    initRouteTable();
    initEvents();
    btnImport.doClick();
    active = true;
  }
  
  private void initRouteTable() {
    model = new PointRouteTableModel();
    tblPointRoutes.setModel(model);
    sltPoint.setSelectedIndex(-1);
    sltPoint.setRenderer(new StringListCellRenderer<>(x -> x == null ? "" : x.getName()));
    portalProvider.register().getPortal()
            .getVehicleService()
            .fetchObjects(Point.class).stream().sorted(Comparator.comparing(TCSObject<Point>::getName))
        .forEach(sltPoint::addItem);
    pointPanel.setSize(180, 150);

    model.addTableModelListener(l -> {
      if (model.getRowCount() < 2 || !model.isRoutable()) {
        return;
      }
      SystemModel systemModel = modelManager.getModel();
      routePaths = new ArrayList<>();
      model.getComputedRoutes().stream().flatMap(data -> data.getRoute().getSteps().stream()).forEach(step -> {
        routePaths.add(systemModel.getPathModel(step.getPath().getName()));
      });
      drawingEditor.getActiveView().repaintDrawingArea();
    });
  }

  private void initEvents() {
    btnAdd.addActionListener(l -> {
      pos = -1;
      pointPanel.setVisible(true);
      pointPanel.setLocationRelativeTo(null);
      txtAction.setText("NOP");
    });

    btnEdit.addActionListener(l -> {
      if (tblPointRoutes.getSelectedRow() >= 0) {
        pos = tblPointRoutes.getSelectedRow();
        pointPanel.setVisible(true);
        pointPanel.setLocationRelativeTo(null);
        sltPoint.setSelectedItem(model.getValueAt(pos, 0));
        txtAction.setText((String) model.getValueAt(pos, 1));
      }
    });

    btnOk.addActionListener(l -> {
      if (txtAction.getText() == null || "".equals(txtAction.getText())) {
        JOptionPane.showMessageDialog(null, "Action empty");
        return;
      }
      if (pos == -1) {
        model.addRow((Point) sltPoint.getSelectedItem(), txtAction.getText());
      }
      else {
        model.editRow(pos, (Point) sltPoint.getSelectedItem(), txtAction.getText());
      }
      pointPanel.setVisible(false);
    });

    btnCancel.addActionListener(l -> pointPanel.setVisible(false));

    btnRemove.addActionListener(l -> {
      if (tblPointRoutes.getSelectedRow() >= 0) {
        model.removeRow(tblPointRoutes.getSelectedRow());
      }
    });

    btnImport.addActionListener(evt -> {
        List<DriveOrder> data = portalProvider.register().getPortal()
                .getRouterService().importRoute(vehicle.getVehicle().getReference());
        if (data != null) {
            model.setData(data);
        }
    });

    btnSend.addActionListener(evt -> {
      if (!model.isRoutable()) {
        JOptionPane.showMessageDialog(null, bundle.getString("routeUnroutable.text"));
        return;
      }
      int status = JOptionPane.showConfirmDialog(null,
                                                 bundle.getFormatted("confirmSendRoute.description", vehicle.getName()),
                                                 bundle.getString("confirmSendRoute.title"),
                                                 JOptionPane.YES_NO_OPTION);
      if (status == JOptionPane.YES_OPTION) {
        List<DriveOrder> driveOrders = model.getComputedRoutes();
        portalProvider.register().getPortal()
                .getVehicleService().sendCommAdapterMessage(vehicle.getVehicle().getReference(), driveOrders);
        portalProvider.register().getPortal()
                .getRouterService().exportRoute(vehicle.getVehicle().getReference(), driveOrders);
      }
    });
  }

  private Route getRoute(Point sourcePoint, Point destinationPoint) {
    return portalProvider.register().getPortal().getRouterService().getRoute(vehicle.getVehicle(), sourcePoint, destinationPoint);
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    pointPanel = new javax.swing.JDialog();
    contentPanel = new javax.swing.JPanel();
    lblAction = new javax.swing.JLabel();
    lblPoint = new javax.swing.JLabel();
    sltPoint = new javax.swing.JComboBox<>();
    txtAction = new javax.swing.JTextField();
    buttonPanel = new javax.swing.JPanel();
    btnOk = new javax.swing.JButton();
    btnCancel = new javax.swing.JButton();
    lblRouteTable = new javax.swing.JLabel();
    jPanel1 = new javax.swing.JPanel();
    routeResult = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    btnSend = new javax.swing.JButton();
    btnImport = new javax.swing.JButton();
    lblRouteResult = new javax.swing.JLabel();
    jPanel3 = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    tblPointRoutes = new javax.swing.JTable();
    jPanel4 = new javax.swing.JPanel();
    jPanel5 = new javax.swing.JPanel();
    btnAdd = new javax.swing.JButton();
    btnEdit = new javax.swing.JButton();
    btnRemove = new javax.swing.JButton();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(50, 0), new java.awt.Dimension(50, 0), new java.awt.Dimension(50, 32767));

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/dialogs/routeDialog"); // NOI18N
    pointPanel.setTitle(bundle.getString("pointPanel.title")); // NOI18N

    contentPanel.setPreferredSize(new java.awt.Dimension(150, 100));
    contentPanel.setLayout(new java.awt.GridBagLayout());

    lblAction.setText(bundle.getString("pointPanel.action.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
    contentPanel.add(lblAction, gridBagConstraints);

    lblPoint.setText(bundle.getString("pointPanel.point.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
    contentPanel.add(lblPoint, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.4;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
    contentPanel.add(sltPoint, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.4;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
    contentPanel.add(txtAction, gridBagConstraints);

    pointPanel.getContentPane().add(contentPanel, java.awt.BorderLayout.CENTER);

    btnOk.setText(bundle.getString("pointPanel.ok")); // NOI18N
    btnOk.setPreferredSize(new java.awt.Dimension(65, 25));
    buttonPanel.add(btnOk);

    btnCancel.setText(bundle.getString("pointPanel.cancel")); // NOI18N
    btnCancel.setPreferredSize(new java.awt.Dimension(65, 25));
    buttonPanel.add(btnCancel);

    pointPanel.getContentPane().add(buttonPanel, java.awt.BorderLayout.PAGE_END);

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    lblRouteTable.setText(bundle.getString("guide.text")); // NOI18N
    lblRouteTable.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
    lblRouteTable.setPreferredSize(new java.awt.Dimension(331, 30));
    getContentPane().add(lblRouteTable, java.awt.BorderLayout.PAGE_START);

    jPanel1.setPreferredSize(new java.awt.Dimension(615, 120));
    jPanel1.setLayout(new java.awt.BorderLayout());

    routeResult.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    routeResult.setPreferredSize(new java.awt.Dimension(34, 100));
    jPanel1.add(routeResult, java.awt.BorderLayout.CENTER);

    jPanel2.setPreferredSize(new java.awt.Dimension(30, 30));
    jPanel2.setLayout(new java.awt.GridBagLayout());

    btnSend.setText(bundle.getString("sendRoute.text")); // NOI18N
    btnSend.setPreferredSize(new java.awt.Dimension(60, 23));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
    jPanel2.add(btnSend, gridBagConstraints);

    btnImport.setText(bundle.getString("importRoute.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
    jPanel2.add(btnImport, gridBagConstraints);

    jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_END);

    lblRouteResult.setText(bundle.getString("findRoute.text")); // NOI18N
    lblRouteResult.setPreferredSize(new java.awt.Dimension(64, 30));
    jPanel1.add(lblRouteResult, java.awt.BorderLayout.PAGE_START);

    getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

    jPanel3.setLayout(new java.awt.BorderLayout(30, 0));

    tblPointRoutes.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Point", "Action"
      }
    ));
    jScrollPane1.setViewportView(tblPointRoutes);

    jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    jPanel4.setPreferredSize(new java.awt.Dimension(80, 10));
    jPanel4.setLayout(new java.awt.BorderLayout());

    jPanel5.setPreferredSize(new java.awt.Dimension(100, 90));
    jPanel5.setLayout(new java.awt.GridLayout(0, 1, 0, 5));

    btnAdd.setText(bundle.getString("addPointRoute.text")); // NOI18N
    jPanel5.add(btnAdd);

    btnEdit.setText(bundle.getString("editPointRoute.text")); // NOI18N
    jPanel5.add(btnEdit);

    btnRemove.setText(bundle.getString("removePointRoute.text")); // NOI18N
    jPanel5.add(btnRemove);

    jPanel4.add(jPanel5, java.awt.BorderLayout.PAGE_START);

    jPanel3.add(jPanel4, java.awt.BorderLayout.LINE_START);
    jPanel3.add(filler1, java.awt.BorderLayout.LINE_END);

    getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    RouteDialog.active = false;
    routePaths.clear();
  }//GEN-LAST:event_formWindowClosing
  
  public class PointRouteTableModel
      extends AbstractTableModel {

    public static final int POINT_COLUMN = 0;
    public static final int ACTION_COLUMN = 1;

    private List<PointRouteData> data;

    private boolean routable = false;

    public void setData(List<DriveOrder> driveOrders) {
        driveOrders.forEach(driveOrder -> addRow(driveOrder.getRoute().getFinalDestinationPoint(),
                                                 driveOrder.getDestination().getOperation()));
    }

    public List<PointRouteData> getData() {
      return data;
    }

    public List<DriveOrder> getComputedRoutes() {
      List<DriveOrder> driveOrders = data.stream().map(d ->
          new DriveOrder(new DriveOrder.Destination(d.getPoint().getReference())
                  .withOperation(d.getAction()))
                .withRoute(d.getRoute()))
              .collect(Collectors.toList());
      int all = driveOrders.stream().mapToInt(driveOrder -> driveOrder.getRoute().getSteps().size()).sum();
      for (int i = 0; i < driveOrders.size(); i++) {
        DriveOrder driveOrder = driveOrders.get(i);
        driveOrder.setCurrentCountOnSteps(all);
        all -= driveOrder.getRoute().getSteps().size();
      }

      return driveOrders;
    }

    public boolean isRoutable() {
      return routable;
    }

    public PointRouteTableModel() {
      this.data = new ArrayList<>();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      switch (columnIndex) {
        case POINT_COLUMN:
          return Point.class;
        case ACTION_COLUMN:
          return String.class;
        default:
          return Object.class;
      }
    }

    @Override
    public int getRowCount() {
      return data.size();
    }

    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
      switch (columnIndex) {
        case POINT_COLUMN:
          return bundle.getString("model.pointColumn");
        case ACTION_COLUMN:
          return bundle.getString("model.actionColumn");
        default:
          return "???";
      }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case POINT_COLUMN:
          data.get(rowIndex).setPoint((Point) aValue);
          break;
        case ACTION_COLUMN:
          data.get(rowIndex).setAction((String) aValue);
          break;
        default:
        //do nothing
      }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case POINT_COLUMN:
          return data.get(rowIndex).getPoint().getName();
        case ACTION_COLUMN:
          return data.get(rowIndex).getAction();
        default:
          return "???";
      }
    }

    public void addRow(Point point, String action) {
      data.add(new PointRouteData(point, action));
      updateRoute(data.size() - 1);
      fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void editRow(int rowIndex, Point point, String action) {
      data.set(rowIndex, new PointRouteData(point, action));
      updateRoute(rowIndex);
      fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public void removeRow(int rowIndex) {
      data.remove(rowIndex);
      updateRoute(rowIndex == data.size() ? 0 : rowIndex);
      fireTableRowsDeleted(rowIndex, rowIndex);
    }

    private void updateRoute(int rowIndex) {
      int size = data.size();
      if (size > 1) {
        data.get(rowIndex).setRoute(getRoute(data.get(rowIndex == 0 ? size - 1 : rowIndex - 1).getPoint(),
                                             data.get(rowIndex).getPoint()));
        data.get(rowIndex == size - 1 ? 0 : rowIndex + 1).setRoute(getRoute(data.get(rowIndex).getPoint(),
                                                                            data.get(rowIndex == size - 1 ? 0 : rowIndex + 1).getPoint()));

        if (data.stream().map(PointRouteData::getRoute).anyMatch(Objects::isNull)) {
          routeResult.setText(bundle.getString("model.routeUnroutable"));
          routable = false;
          return;
        }

        StringBuilder builder = new StringBuilder("<html>");
        data.stream().map(PointRouteData::getRoute).forEach(route -> {
          route.getSteps().subList(0, route.getSteps().size() - 1).forEach(
              step -> builder.append(" >> ").append(step.getDestinationPoint().getName())
          );
          builder.append(" >> <b>").append(route.getFinalDestinationPoint().getName()).append("</b>").append("<br>");
        });
        builder.append("</html>");
        routeResult.setText(builder.toString());
        routable = true;
      }
      else {
        routeResult.setText("");
        routable = false;
      }
    }
  }

  public static class PointRouteData {
      private Point point;
      private String action;
      private Route route;

      public PointRouteData(Point point, String action) {
          this.point = point;
          this.action = action;
      }

      public Point getPoint() {
          return point;
      }

      public void setPoint(Point point) {
          this.point = point;
      }

      public String getAction() {
          return action;
      }

      public void setAction(String action) {
          this.action = action;
      }

      public Route getRoute() {
          return route;
      }

      public void setRoute(Route route) {
          this.route = route;
      }
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnAdd;
  private javax.swing.JButton btnCancel;
  private javax.swing.JButton btnEdit;
  private javax.swing.JButton btnImport;
  private javax.swing.JButton btnOk;
  private javax.swing.JButton btnRemove;
  private javax.swing.JButton btnSend;
  private javax.swing.JPanel buttonPanel;
  private javax.swing.JPanel contentPanel;
  private javax.swing.Box.Filler filler1;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JLabel lblAction;
  private javax.swing.JLabel lblPoint;
  private javax.swing.JLabel lblRouteResult;
  private javax.swing.JLabel lblRouteTable;
  private javax.swing.JDialog pointPanel;
  private javax.swing.JLabel routeResult;
  private javax.swing.JComboBox<Point> sltPoint;
  private javax.swing.JTable tblPointRoutes;
  private javax.swing.JTextField txtAction;
  // End of variables declaration//GEN-END:variables
}