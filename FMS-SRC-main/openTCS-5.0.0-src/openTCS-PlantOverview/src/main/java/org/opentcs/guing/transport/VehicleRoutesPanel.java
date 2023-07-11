/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.guing.transport;

import com.google.inject.assistedinject.Assisted;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.*;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.commands.SendVehicleRoutesCommand;
import org.opentcs.drivers.vehicle.commands.SetVehicleRouteCommand;
import org.opentcs.events.WorkingRouteEvent;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.dialogs.DialogContent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.UserMessageHelper;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.VEHICLE_ROUTES;

/**
 *
 * @author ADMIN
 */
public class VehicleRoutesPanel
    extends JDialog implements EventHandler {

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(VEHICLE_ROUTES);

  private static final String TAB_ROUTES = "tabRoutes";
  private static final String TAB_SYNC_ROUTES = "tabSyncRoutes";
  private static final String TAB_DISCONNECTED = "tabDisconnected";

  private final EventBus eventBus;
  /**
   * Provides the current system model.
   */
  private final ModelManager modelManager;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * openTCS view
   */
  private final OpenTCSView openTCSView;

  private final UserMessageHelper helper;

  private final PointActionTableModel model = new PointActionTableModel();

  private int mapId;

  private boolean editMode = false;

  private volatile boolean returned = false;

  private Vehicle vehicle;

  private WorkingRoute.WorkingRouteRaw sentWorkingRoute;
  /**
   * Creates new form VehicleRoutesPanel
   */
  @Inject
  public VehicleRoutesPanel(@ApplicationFrame JFrame applicationFrame,
                            @ApplicationEventBus EventBus eventBus,
                            ModelManager modelManager,
                            OpenTCSView openTCSView,
                            UserMessageHelper helper,
                            SharedKernelServicePortalProvider portalProvider,
                            @Assisted @Nullable VehicleModel vehicleModel) {
    super(applicationFrame, true);
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.openTCSView = requireNonNull(openTCSView, "openTCSView");
    this.helper = requireNonNull(helper, "helper");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    btnSyncRoutes = new JButton(BUNDLE.getString("btnSyncRoutes"));
    this.setTitle(BUNDLE.getString("title"));
    this.setSize(330, 560);
    this.setLocationRelativeTo(applicationFrame);
    initComponents();
    initPanels();
    initFields();

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        openTCSView.removeFocusGroup();
      }
    });

    if (vehicleModel != null) {
      cbxVehicle.setSelectedItem(vehicleModel);
    }
  }

  private void initPanels() {
    pnlVehicleRoute.removeAll();
    pnlVehicleRoute.add(pnlDisconnected, TAB_DISCONNECTED);
    pnlVehicleRoute.add(pnlSyncRoutes, TAB_SYNC_ROUTES);
    pnlVehicleRoute.add(pnlVehicleRouteContent, TAB_ROUTES);

    cbxGroup.setEditor(new RouteCellEditor());
    cbxGroup.setRenderer(new RouteListCellRender(openTCSView));
    tblPointActions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  }

  public void initFields() {
    mapId = Integer.parseInt(modelManager.getModel().getPropertyMiscellaneous().getItems().stream()
            .filter(kvp -> kvp.getKey().equals("id")).findFirst().get().getValue());
    tblPointActions.setModel(model);
    JComboBox<VehicleAction> cbxAction = new JComboBox<>();
    Arrays.stream(VehicleAction.values()).forEach(cbxAction::addItem);
    tblPointActions.setDefaultEditor(VehicleAction.class, new DefaultCellEditor(cbxAction));

    modelManager.getModel().getVehicleModels().forEach(vehicle -> cbxVehicle.addItem(vehicle));
    modelManager.getModel().getGroupModels().forEach(group -> cbxGroup.addItem(group));

    cbxVehicle.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        updateVehicleRoute();
        setEditMode(false);
      }
    });

    cbxGroup.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.DESELECTED) {
        GroupModel group = modelManager.getModel().getGroupModel(e.getItem().toString());
//        openTCSView.highlightRoute(group, false);
      }
      else if (e.getStateChange() == ItemEvent.SELECTED) {
        model.clear();
        GroupModel group = (GroupModel) cbxGroup.getSelectedItem();
//        openTCSView.highlightRoute(group, true);
        group.getChildComponents().stream()
                .filter(component -> component instanceof PointModel)
                .forEach(pointModel -> {
          model.addRow((PointModel) pointModel, VehicleAction.NONE);
        });
        openTCSView.focusGroup(group, -1);
      }
    });

    btnEdit.addActionListener(e -> setEditMode(true));

    btnCancel.addActionListener(e -> setEditMode(false));

    btnSyncRoutes.addActionListener(e -> {
      try (SharedKernelServicePortal portal = portalProvider.register();) {
        if (cbxVehicle.getSelectedIndex() < 0) {
          return;
        }
        VehicleService vehicleService = portal.getPortal().getVehicleService();
        Set<Group> groups = vehicleService.fetchObjects(Group.class);
        Map<Entry<String, String>, Path> pathMapByPoint = new HashMap<>();
        Map<String, Point> pointMap = new HashMap<>();
        vehicleService.fetchObjects(Path.class).forEach(path -> pathMapByPoint.put(
                new AbstractMap.SimpleEntry<>(path.getSourcePoint().getName(), path.getDestinationPoint().getName()),
                path));
        vehicleService.fetchObjects(Point.class).forEach(point -> pointMap.put(point.getName(), point));
        Map<Integer, Route> routes = new HashMap<>();
        groups.stream().forEach(group -> {
          List<TCSObjectReference<?>> points = group.getMembers().stream()
                  .filter(mem -> mem.getReferentClass().equals(Point.class))
                  .collect(Collectors.toList());
          List<Route.Step> steps = new ArrayList<>();
          int index = 0;
          for (int i = 0; i < points.size(); i++) {
            Point srcPoint = pointMap.get(points.get(i).getName());
            Point dstPoint = pointMap.get(points.get(i + 1 == points.size() ? 0 : i + 1).getName());
            Path path = pathMapByPoint.get(new AbstractMap.SimpleEntry<>(srcPoint.getName(), dstPoint.getName()));
            steps.add(new Route.Step(path, srcPoint, dstPoint, Vehicle.Orientation.FORWARD, index++));
          }
          routes.put(group.getId(), new Route(steps, 0));
        });

        VehicleModel vehicleModel = (VehicleModel) cbxVehicle.getSelectedItem();
        Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, vehicleModel.getName());
        vehicleService.sendCommAdapterCommand(vehicle.getReference(),
                new SendVehicleRoutesCommand(mapId, routes));
        try {
          btnSyncRoutes.setEnabled(false);
          waitingForChanging(vehicle);
        } catch (Exception exception) {
          helper.showMessageDialog(BUNDLE.getString("error.title"), exception.getMessage(), UserMessageHelper.Type.ERROR);
        }
      } catch (KernelRuntimeException ex) {
        ex.printStackTrace();
      } finally {
        btnSyncRoutes.setEnabled(true);
      }
    });

    btnUpdate.addActionListener(e -> {
      try (SharedKernelServicePortal portal = portalProvider.register();) {
        VehicleService vehicleService = portal.getPortal().getVehicleService();
        Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, this.vehicle.getReference());
        if (!vehicleService.fetchProcessModel(vehicle.getReference()).isCommAdapterConnected()) {
          helper.showMessageDialog(BUNDLE.getString("error.title"),
                  BUNDLE.getString("error.vehicleDisconnected"),
                  UserMessageHelper.Type.ERROR);
          updateVehicleRoute();
          return;
        }
        Group group = vehicleService.fetchObject(Group.class, ((GroupModel) cbxGroup.getSelectedItem()).getName());
        Map<String, Character> pointActions = new LinkedHashMap<>();
        for (int i = 0; i < model.getRowCount(); i++) {
          Entry<PointModel, VehicleAction> pointAction = model.getPointAction(i);
          if (pointAction.getValue() != VehicleAction.NONE) {
            pointActions.put(pointAction.getKey().getName(), pointAction.getValue().getPresentation());
          }
        }
        WorkingRoute.WorkingRouteRaw workingRoute = new WorkingRoute.WorkingRouteRaw(mapId, group.getId(), pointActions);
        SetVehicleRouteCommand svrc = new SetVehicleRouteCommand(workingRoute);
        vehicleService.sendCommAdapterCommand(vehicle.getReference(), svrc);
        sentWorkingRoute = workingRoute;
        try {
          btnUpdate.setEnabled(false);
          this.waitingForChanging(vehicle);
          setEditMode(false);
//          lblStatus.setForeground(Color.blue.brighter());
//          lblStatus.setText("Send route successfully");
        } catch (Exception ex) {
          ex.printStackTrace();
          helper.showMessageDialog("Error", ex.getMessage(), UserMessageHelper.Type.ERROR);
//          lblStatus.setForeground(Color.red);
//          lblStatus.setText(ex.getMessage());
        }
      } catch (KernelRuntimeException ex) {
        ex.printStackTrace();
      } finally {
        btnUpdate.setEnabled(true);
      }
    });

    tblPointActions.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        int index = tblPointActions.getSelectedRow();
        if (index < 0) {
          return;
        }
        PointModel pointModel = model.getPointAction(index).getKey();
        openTCSView.figureSelected(pointModel);
      }
    });

    setEditMode(false);
    updateVehicleRoute();
  }

  private void updateVehicleRoute() {
    model.clear();
    lblCurrentRoute.setText(BUNDLE.getString("currentRoute.noRoute"));
    try (SharedKernelServicePortal portal = portalProvider.register();) {
      VehicleService vehicleService = portal.getPortal().getVehicleService();
      VehicleModel vehicleModel = (VehicleModel) cbxVehicle.getSelectedItem();
      openTCSView.figureSelected(vehicleModel);
      Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, vehicleModel.getName());
      if (vehicle != null) {
        this.vehicle = vehicle;
        if (!vehicleService.fetchProcessModel(vehicle.getReference()).isCommAdapterConnected()) {
          ((CardLayout) pnlVehicleRoute.getLayout()).show(pnlVehicleRoute, TAB_DISCONNECTED);
          return;
        }
        WorkingRoute workingRoute = vehicle.getWorkingRoute();
        if (workingRoute.getMapId() != mapId) {
          ((CardLayout) pnlVehicleRoute.getLayout()).show(pnlVehicleRoute, TAB_SYNC_ROUTES);
          return;
        }
        if (workingRoute.getCurrentGroup() != null) {
          GroupModel group = modelManager.getModel().getGroupModel(workingRoute.getCurrentGroup().getName());
//          openTCSView.highlightRoute(group, true);
          openTCSView.focusGroup(group, -1);
          lblCurrentRoute.setText(group.getName());
          cbxGroup.setSelectedItem(group);
          Map<String, VehicleAction> pointActions = workingRoute.getCurrentGroupDetail().stream()
                  .collect(Collectors.toMap(wrp -> wrp.getPoint().getName(), WorkingRoutePoint::getAction));
          group.getChildComponents().forEach(mem -> {
            if (mem instanceof PointModel) {
              VehicleAction action = pointActions.getOrDefault(mem.getName(), VehicleAction.NONE);
              model.addRow((PointModel) mem, action);
            }
          });
        } else if (editMode) {
          GroupModel group = (GroupModel) cbxGroup.getSelectedItem();
          group.getChildComponents().forEach(mem -> {
            if (mem instanceof PointModel) {
              model.addRow((PointModel) mem, VehicleAction.NONE);
            }
          });
        }
        ((CardLayout) pnlVehicleRoute.getLayout()).show(pnlVehicleRoute, TAB_ROUTES);
      }
    } catch (KernelRuntimeException ex) {
      ex.printStackTrace();
    }
    lblStatus.setText("");

    pnlVehicleRoute.revalidate();
  }

  public void setEditMode(boolean editMode) {
    this.editMode = editMode;
    btnEdit.setVisible(!editMode);
    btnCancel.setVisible(editMode);
    btnUpdate.setVisible(editMode);
    cbxGroup.setVisible(editMode);
    model.setEditMode(editMode);
    updateVehicleRoute();
  }

  public void waitingForChanging(Vehicle vehicle) throws Exception {
    this.vehicle = vehicle;
    eventBus.subscribe(this);
    int time = 0;
    while (!returned) {
      if (time >= 5000) {
        eventBus.unsubscribe(this);
        throw new Exception(BUNDLE.getString("error.requestTimeout"));
      }
      time += 100;
      Thread.sleep(100);
    }
    eventBus.unsubscribe(this);
    returned = false;
    setEditMode(false);
  }

  /**
   * Processes the event object.
   *
   * @param event The event object.
   */
  @Override
  public void onEvent(Object event) {
    if (event instanceof WorkingRouteEvent) {
      WorkingRouteEvent evt = (WorkingRouteEvent) event;
      if (evt.getVehicle().getReference().equals(vehicle.getReference())) {
        returned = true;
        switch (evt.getType()) {
          case SYNC_ROUTES_SUCCESS:
            helper.showMessageDialog(BUNDLE.getString("message.syncRoute.title"),
                    BUNDLE.getFormatted("message.syncRoute.success.text", evt.getVehicle().getName()),
                    UserMessageHelper.Type.INFO);
            break;
          case SYNC_ROUTES_FAILED:
            helper.showMessageDialog(BUNDLE.getString("message.syncRoute.title"),
                    BUNDLE.getFormatted("message.syncRoute.failed.text", evt.getVehicle().getName()),
                    UserMessageHelper.Type.ERROR);
            break;
          case CURRENT_ROUTE:
            if (sentWorkingRoute == null) {
              return;
            }
            if (Objects.equals(evt.getVehicle().getWorkingRoute().toRaw(), sentWorkingRoute)) {
              helper.showMessageDialog(BUNDLE.getString("message.setRoute.title"),
                      BUNDLE.getFormatted("message.setRoute.success.text", evt.getVehicle().getName()),
                      UserMessageHelper.Type.INFO);
            } else {
              helper.showMessageDialog(BUNDLE.getString("message.setRoute.title"),
                      BUNDLE.getFormatted("message.setRoute.failed.text", evt.getVehicle().getName()),
                      UserMessageHelper.Type.ERROR);
            }
            sentWorkingRoute = null;
            break;
        }
      }
    }
//    if (event instanceof TCSObjectEvent) {
//      TCSObjectEvent evt = (TCSObjectEvent) event;
//      if (evt.getCurrentOrPreviousObjectState() instanceof Vehicle) {
//        if (Objects.equals(evt.getCurrentOrPreviousObjectState().getName(),
//                cbxVehicle.getSelectedItem().toString())) {
//          if (evt.getType() == TCSObjectEvent.Type.OBJECT_MODIFIED) {
//            Vehicle oldVehicle = (Vehicle) evt.getPreviousObjectState();
//            Vehicle newVehicle = (Vehicle) evt.getCurrentObjectState();
//            if (oldVehicle.getWorkingRoute() != newVehicle.getWorkingRoute()) {
//              returned = true;
//            }
//          }
//        }
//      }
//    }
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

    pnlVehicle = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    cbxVehicle = new javax.swing.JComboBox<>();
    pnlVehicleRoute = new javax.swing.JPanel();
    pnlVehicleRouteContent = new javax.swing.JPanel();
    pnlVehicleRouteEdit = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    lblCurrentRoute = new javax.swing.JLabel();
    btnEdit = new javax.swing.JButton();
    cbxGroup = new javax.swing.JComboBox<>();
    btnCancel = new javax.swing.JButton();
    jScrollPane1 = new javax.swing.JScrollPane();
    tblPointActions = new javax.swing.JTable();
    pnlUpdate = new javax.swing.JPanel();
    lblStatus = new javax.swing.JLabel();
    btnUpdate = new javax.swing.JButton();
    pnlDisconnected = new javax.swing.JPanel();
    lblVehicleDisconnected = new javax.swing.JLabel();
    pnlSyncRoutes = new javax.swing.JPanel();
    lblSyncRoutes = new javax.swing.JLabel();
    btnSyncRoutes = new javax.swing.JButton();

    pnlVehicle.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 1, 10));
    pnlVehicle.setPreferredSize(new java.awt.Dimension(614, 50));
    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEADING);
    flowLayout1.setAlignOnBaseline(true);
    pnlVehicle.setLayout(flowLayout1);

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/dialogs/vehicleRoutes"); // NOI18N
    jLabel1.setText(bundle.getString("vehicle")); // NOI18N
    pnlVehicle.add(jLabel1);

    cbxVehicle.setName(""); // NOI18N
    cbxVehicle.setPreferredSize(new java.awt.Dimension(200, 29));
    pnlVehicle.add(cbxVehicle);

    getContentPane().add(pnlVehicle, java.awt.BorderLayout.PAGE_START);

    pnlVehicleRoute.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 10, 10));
    pnlVehicleRoute.setLayout(new java.awt.CardLayout());

    pnlVehicleRouteContent.setLayout(new java.awt.BorderLayout(0, 5));

    pnlVehicleRouteEdit.setPreferredSize(new java.awt.Dimension(278, 70));
    pnlVehicleRouteEdit.setLayout(new java.awt.GridBagLayout());

    jLabel2.setText(bundle.getString("currentRoute")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.weighty = 0.1;
    pnlVehicleRouteEdit.add(jLabel2, gridBagConstraints);

    lblCurrentRoute.setText(" ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    pnlVehicleRouteEdit.add(lblCurrentRoute, gridBagConstraints);

    btnEdit.setText(bundle.getString("btnEdit")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weighty = 0.1;
    pnlVehicleRouteEdit.add(btnEdit, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weighty = 0.1;
    pnlVehicleRouteEdit.add(cbxGroup, gridBagConstraints);

    btnCancel.setText(bundle.getString("btnCancel")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    pnlVehicleRouteEdit.add(btnCancel, gridBagConstraints);

    pnlVehicleRouteContent.add(pnlVehicleRouteEdit, java.awt.BorderLayout.PAGE_START);

    tblPointActions.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {

      }
    ));
    jScrollPane1.setViewportView(tblPointActions);

    pnlVehicleRouteContent.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    pnlUpdate.setLayout(new java.awt.BorderLayout());

    lblStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblStatus.setText("Status...");
    lblStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 5, 1));
    pnlUpdate.add(lblStatus, java.awt.BorderLayout.CENTER);

    btnUpdate.setText(bundle.getString("btnUpdate")); // NOI18N
    pnlUpdate.add(btnUpdate, java.awt.BorderLayout.PAGE_END);

    pnlVehicleRouteContent.add(pnlUpdate, java.awt.BorderLayout.PAGE_END);

    pnlVehicleRoute.add(pnlVehicleRouteContent, "card2");
    pnlVehicleRouteContent.getAccessibleContext().setAccessibleName(TAB_ROUTES);

    pnlDisconnected.setLayout(new java.awt.GridBagLayout());

    lblVehicleDisconnected.setText(bundle.getString("error.vehicleDisconnected")); // NOI18N
    pnlDisconnected.add(lblVehicleDisconnected, new java.awt.GridBagConstraints());

    pnlVehicleRoute.add(pnlDisconnected, "card3");

    pnlSyncRoutes.setLayout(new java.awt.GridBagLayout());

    lblSyncRoutes.setText(bundle.getString("error.vehicleRouteNotMatch")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.1;
    pnlSyncRoutes.add(lblSyncRoutes, gridBagConstraints);

    btnSyncRoutes.setText(bundle.getString("btnSyncRoutes")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    pnlSyncRoutes.add(btnSyncRoutes, gridBagConstraints);

    pnlVehicleRoute.add(pnlSyncRoutes, "card4");

    getContentPane().add(pnlVehicleRoute, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnCancel;
  private javax.swing.JButton btnEdit;
  private javax.swing.JButton btnSyncRoutes;
  private javax.swing.JButton btnUpdate;
  private javax.swing.JComboBox<GroupModel> cbxGroup;
  private javax.swing.JComboBox<VehicleModel> cbxVehicle;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JLabel lblCurrentRoute;
  private javax.swing.JLabel lblStatus;
  private javax.swing.JLabel lblSyncRoutes;
  private javax.swing.JLabel lblVehicleDisconnected;
  private javax.swing.JPanel pnlDisconnected;
  private javax.swing.JPanel pnlSyncRoutes;
  private javax.swing.JPanel pnlUpdate;
  private javax.swing.JPanel pnlVehicle;
  private javax.swing.JPanel pnlVehicleRoute;
  private javax.swing.JPanel pnlVehicleRouteContent;
  private javax.swing.JPanel pnlVehicleRouteEdit;
  private javax.swing.JTable tblPointActions;
  // End of variables declaration//GEN-END:variables

  public static class PointActionTableModel extends AbstractTableModel {

    private ArrayList<PointModel> pointModels;
    private ArrayList<VehicleAction> actions;
    private boolean editMode;

    public PointActionTableModel() {
      pointModels = new ArrayList<>();
      actions = new ArrayList<>();
    }

    public void addRow(PointModel pointModel, VehicleAction action) {
      pointModels.add(pointModel);
      actions.add(action);
      fireTableRowsInserted(pointModels.size() - 1, pointModels.size() - 1);
    }

    public void clear() {
      pointModels.clear();
      actions.clear();
    }

    /**
     * Returns the number of rows in the model. A
     * <code>JTable</code> uses this method to determine how many rows it
     * should display.  This method should be quick, as it
     * is called frequently during rendering.
     *
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    @Override
    public int getRowCount() {
      return pointModels.size();
    }

    /**
     * Returns the number of columns in the model. A
     * <code>JTable</code> uses this method to determine how many columns it
     * should create and display by default.
     *
     * @return the number of columns in the model
     * @see #getRowCount
     */
    @Override
    public int getColumnCount() {
      return 2;
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param rowIndex    the row whose value is to be queried
     * @param columnIndex the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case 0:
          return pointModels.get(rowIndex);
        case 1:
          return actions.get(rowIndex);
        default:
          return null;
      }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (columnIndex == 1) {
        actions.set(rowIndex, (VehicleAction) aValue);
        fireTableRowsUpdated(rowIndex, rowIndex);
      }
    }

    public Entry<PointModel, VehicleAction> getPointAction(int rowIndex) {
      return new AbstractMap.SimpleEntry<>(pointModels.get(rowIndex), actions.get(rowIndex));
    }

    /**
     * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
     *
     * @param columnIndex the column being queried
     * @return the Object.class
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
      switch (columnIndex) {
        case 0:
          return PointModel.class;
        case 1:
          return VehicleAction.class;
        default:
          return Object.class;
      }
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return BUNDLE.getString("pointActionTable.column.point");
        case 1:
          return BUNDLE.getString("pointActionTable.column.action");
        default:
          return "???";
      }
    }

    public void setEditMode(boolean editMode) {
      this.editMode = editMode;
      fireTableChanged(new TableModelEvent(this));
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      if (columnIndex == 1) {
        return editMode;
      }
      return false;
    }
  }
}
