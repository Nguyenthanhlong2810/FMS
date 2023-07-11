package org.opentcs.guing.components.dialogs;

import com.google.common.base.CaseFormat;
import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.TableFilterHeader;
import org.opentcs.access.*;
import org.opentcs.common.VehicleError;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.VehicleNotificationPredicate;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.data.notification.VehicleNotification;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.event.SystemModelTransitionEvent;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.VEHICLE_NOTIFICATION_PATH;

public class VehicleStateTable extends JPanel implements EventHandler, AttributesChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleStateTable.class);

    private static final ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverview.VEHICLE_STATE_TABLE);
    private static final ResourceBundleUtil VEHICLE_STATE_BUNDLE = ResourceBundleUtil.getBundle(VEHICLE_NOTIFICATION_PATH);

    private JTable tbl_vehicle;

    private DefaultTableModel model;

    private SimpleDateFormat formatter = new SimpleDateFormat(labels.getString("vehicleStateTable.dateFormat"));

    private final ModelManager modelManager;

    private final SharedKernelServicePortalProvider sharedProvider;

    private Map<VehicleModel, Vehicle.State> vehicleModelStateMap = new LinkedHashMap();

    private static final String CONNECT = VEHICLE_STATE_BUNDLE.getString("vehicleNotification.state.connected");

    private static final String DISCONNECT = VEHICLE_STATE_BUNDLE.getString("vehicleNotification.state.disconnected");

    private static final String WARNING_STATE = VEHICLE_STATE_BUNDLE.getString("vehicleNotification.state.warning");

    private static final String ERROR_STATE = VEHICLE_STATE_BUNDLE.getString("vehicleNotification.state.error");

    private static final String WRONG_POSITION = VEHICLE_STATE_BUNDLE.getString("vehicleNotification.state.wrongNextPosition");

    private static final String LOST_ROUTE = VEHICLE_STATE_BUNDLE.getString("vehicleNotification.state.lostWorkingRoute");

    private final int OPERATION_STATE_COLUMN = 2;

    private List<VehicleError> errorsEnum;

    private Map<VehicleModel,Integer> errorMapOld = new HashMap<>();

    private final Color ERROR_ROW_COLOR = Color.RED;

    private final Color WARNING_ROW_COLOR = new Color(253, 147, 5);

    private final Color HEADER_TABLE_COLOR = new Color(242, 112, 24);

    @Inject
    public VehicleStateTable(ModelManager modelManager,
                             SharedKernelServicePortalProvider sharedProvider){
        this.modelManager = requireNonNull(modelManager,"modelManager");
        this.sharedProvider = requireNonNull(sharedProvider,"sharedProvider");
        setData();
        decorateTable(tbl_vehicle, SwingConstants.CENTER);
        TableFilterHeader filterHeader = new TableFilterHeader(tbl_vehicle, AutoChoices.ENABLED);
        filterHeader.setBackground(Color.ORANGE);

        errorsEnum = new ArrayList<>();
        for (VehicleError value : VehicleError.values()) {
            errorsEnum.add(value);
        }
    }

    public void setData(){
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(900,400));
        model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn(decorateTableHeaderText(labels.getString("vehicleStateTable.timeHeader")));
        model.addColumn(decorateTableHeaderText(labels.getString("vehicleStateTable.vehicleHeader")));
        model.addColumn(decorateTableHeaderText(labels.getString("vehicleStateTable.operationState")));
        model.addColumn(decorateTableHeaderText(labels.getString("vehicleStateTable.currentHeader")));
        model.addColumn(decorateTableHeaderText(labels.getString("vehicleStateTable.voltageHeader")));
        model.addColumn(decorateTableHeaderText(labels.getString("vehicleStateTable.batteryHeader")));
        model.addColumn(decorateTableHeaderText(labels.getString("vehicleStateTable.messageHeader")));
        model.addColumn(decorateTableHeaderText(labels.getString("vehicleStateTable.positionHeader")));
        model.setRowCount(0);
        tbl_vehicle = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                String state = model.getValueAt(row, OPERATION_STATE_COLUMN).toString();
                if (Objects.equals(state, WARNING_STATE)) {
                    comp.setForeground(WARNING_ROW_COLOR);
                } else if (Objects.equals(state, ERROR_STATE)
                        || Objects.equals(state, WRONG_POSITION)
                        || Objects.equals(state, LOST_ROUTE)) {
                    comp.setForeground(ERROR_ROW_COLOR);
                } else {
                    comp.setBackground(Color.WHITE);
                    comp.setForeground(Color.BLACK);
                }
                return comp;
            }
        };
        JScrollPane scrollPane = new JScrollPane(tbl_vehicle);
        add(scrollPane);
    }

    public void updateVehicleState(VehicleModel vehicleModel) {
        Vehicle.State stateOld = vehicleModelStateMap.get(vehicleModel);
        Vehicle.State stateNew = vehicleModel.getVehicle().getState();
        if (stateOld.equals(Vehicle.State.UNKNOWN) && !stateNew.equals(Vehicle.State.UNKNOWN)) {
            insertDataTable(vehicleModel, CONNECT, "");
        }
        if (!stateOld.equals(Vehicle.State.UNKNOWN) && stateNew.equals(Vehicle.State.UNKNOWN)) {
            insertDataTable(vehicleModel, DISCONNECT, "");
        }
        if (vehicleModel.getVehicle().isWarning()) {
            insertDataTable(vehicleModel, WARNING_STATE, "");
        }
        if (stateNew.equals(Vehicle.State.ERROR)) {
            updateVehicleError(vehicleModel);
        }else{
            vehicleModelStateMap.put(vehicleModel,vehicleModel.getVehicle().getState());
        }
    }

    public void insertDataTable(VehicleModel vehicleModel, String stateContent, String messageContent){
        Date date = new Date();
        model.insertRow(0, new Object[]{formatter.format(date), vehicleModel.getName(), stateContent,
                vehicleModel.getPropertyCurrent().getValue(), vehicleModel.getPropertyVoltage().getValue(),
                vehicleModel.getPropertyEnergyLevel().getValue(), messageContent, vehicleModel.getPropertyPoint().getText()});

        vehicleModelStateMap.put(vehicleModel,vehicleModel.getVehicle().getState());
        removeUnimportantRows();
    }

    public void insertDataTable(Vehicle vehicle, String stateContent, String messageContent){
        Date date = new Date();
        model.insertRow(0, new Object[]{formatter.format(date), vehicle.getName(), stateContent,
                vehicle.getCurrent(), vehicle.getVoltage(),
                vehicle.getEnergyLevel(), messageContent,
                vehicle.getCurrentPosition() != null ? vehicle.getCurrentPosition().getName() : ""});
        removeUnimportantRows();
    }

    public void updateVehicleError(VehicleModel vehicleModel) {
        int errorNew = vehicleModel.getVehicle().getErrorCode();
        List<VehicleError> errors = VehicleError.getNewErrorFromPrevious(errorMapOld.get(vehicleModel), errorNew);
        if(errors.size() > 0){
            for(VehicleError error : errors){
                Date date = new Date();
                model.insertRow(0, new Object[]{formatter.format(date), vehicleModel.getName(), ERROR_STATE,
                        vehicleModel.getPropertyCurrent().getValue(), vehicleModel.getPropertyVoltage().getValue(),
                        vehicleModel.getPropertyEnergyLevel().getValue(), error, vehicleModel.getPropertyPoint().getText()});
            }
        }
        errorMapOld.put(vehicleModel, errorNew);
    }

    public void removeUnimportantRows(){
        if(model.getRowCount() >= 50){
            for (int i = 0; i < model.getRowCount(); i++) {
                if(model.getValueAt(i,OPERATION_STATE_COLUMN).equals(CONNECT)){
                    model.removeRow(i);
                }
            }
        }
    }

    @Override
    public void onEvent(Object event) {
        if (event instanceof OperationModeChangeEvent) {
            handleModeChange((OperationModeChangeEvent) event);
        }
        if (event instanceof SystemModelTransitionEvent) {
            handleSystemModelTransition((SystemModelTransitionEvent) event);
        }
        if (event instanceof NotificationPublicationEvent) {
            UserNotification notification = ((NotificationPublicationEvent) event).getNotification();
            if (notification instanceof VehicleNotification) {
                handleVehicleNotification((VehicleNotification) notification);
            }
        }
    }

    private void handleModeChange(OperationModeChangeEvent evt) {
        switch (evt.getNewMode()) {
            case OPERATING:
                setVehicleModels(modelManager.getModel().getVehicleModels());
                model.setRowCount(0);
                try (SharedKernelServicePortal servicePortal = sharedProvider.register()) {
                    servicePortal.getPortal().getNotificationService()
                            .fetchUserNotifications(new VehicleNotificationPredicate())
                            .forEach(notification -> handleVehicleNotification((VehicleNotification) notification));
                } catch (KernelRuntimeException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
                break;
            case MODELLING:
            default:
                clearVehicles();
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
        }
    }

    private void handleVehicleNotification(VehicleNotification notification) {
        String stateBundle = VEHICLE_STATE_BUNDLE.getString("vehicleNotification.state."
                        + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, notification.getState().name()));
        switch (notification.getState()) {
            case CONNECTED:
            case DISCONNECTED:
            case WARNING:
            case LOST_WORKING_ROUTE:
                insertDataTable(notification.getVehicle(), stateBundle, "");
                break;
            case ERROR:
                int previousCode = notification.getPreviousVehicle().getErrorCode();
                int currentCode = notification.getVehicle().getErrorCode();
                VehicleError.getNewErrorFromPrevious(previousCode, currentCode).forEach(error ->
                        insertDataTable(notification.getVehicle(), stateBundle, error.name()));
                break;
            case WRONG_NEXT_POSITION:
                insertDataTable(notification.getVehicle(), stateBundle,
                        notification.getPreviousVehicle().getNextPosition().getName());
                break;
        }
    }

    private void setVehicleModels(List<VehicleModel> vehicleModels) {
        clearVehicles();
        vehicleModels.forEach(vehicle -> {
            vehicleModelStateMap.put(vehicle, vehicle.getVehicle().getState());
            errorMapOld.put(vehicle,0);
            vehicle.addAttributesChangeListener(this);
        });
    }

    private void clearVehicles() {
        vehicleModelStateMap.keySet().forEach(vehicle -> vehicle.removeAttributesChangeListener(this));
        vehicleModelStateMap.clear();
        errorMapOld.clear();
    }

    @Override
    public void propertiesChanged(AttributesChangeEvent e) {
//        if(e.getModel() instanceof VehicleModel){
//            updateVehicleState((VehicleModel) e.getModel());
//        };
    }

    public void decorateTable(JTable table, int alignment) {
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        renderer.setHorizontalAlignment(0);
        table.getTableHeader().setForeground(HEADER_TABLE_COLOR);
        renderer.setBackground(Color.RED);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(alignment);

        TableModel tableModel = table.getModel();
        for (int columnIndex = 0; columnIndex < tableModel.getColumnCount(); columnIndex++) {
            table.getColumnModel().getColumn(columnIndex).setCellRenderer(rightRenderer);
        }
    }

    private String decorateTableHeaderText(String text) {
        return "<html><span style='font-size: 10px;'><b>" + text + "</b></span></html>";
    }
}
