package org.opentcs.kernel.extensions.statistics;

import org.opentcs.hibernate.HibernateConfiguration;
import org.opentcs.hibernate.entities.VehicleEntity;
import org.opentcs.hibernate.entities.VehicleHistoryEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;

import static java.util.Objects.requireNonNull;

public class OperationDataCollector implements KernelExtension, EventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OperationDataCollector.class);

    private final HibernateConfiguration configuration;

    private final EventSource eventSource;

    private SessionFactory sessionFactory;

    private boolean initialized;

    @Inject
    public OperationDataCollector(HibernateConfiguration configuration,
                                  @ApplicationEventBus EventSource eventSource) {
        this.configuration = requireNonNull(configuration, "StatisticsCollectorConfiguration");
        this.eventSource = requireNonNull(eventSource, "eventSource");
    }

    @Override
    public void initialize() {
        if (isInitialized()) {
            return;
        }

        sessionFactory = createSessionFactory();
        eventSource.subscribe(this);
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void terminate() {
        if (!isInitialized()) {
            return;
        }

        eventSource.unsubscribe(this);
        sessionFactory.close();
        sessionFactory = null;
        initialized = false;
    }

    private SessionFactory createSessionFactory() {
        Configuration sessionConfig = new Configuration();
        sessionConfig.configure();
        String connectionUrl = String.format("jdbc:postgresql://%s:%s/%s",
                                                configuration.hostname(),
                                                configuration.port(),
                                                configuration.datasource());
        sessionConfig.setProperty("hibernate.connection.url", connectionUrl);
        sessionConfig.setProperty("hibernate.connection.username", configuration.username());
        sessionConfig.setProperty("hibernate.connection.password", configuration.password());

        return sessionConfig.buildSessionFactory();
    }

    @Override
    public void onEvent(Object event) {
        if (!isInitialized()) {
            LOG.warn("Not properly initialized, ignoring event.");
            return;
        }
        if (event instanceof TCSObjectEvent) {
            processObjectEvent((TCSObjectEvent) event);
        }
    }

    private void processObjectEvent(TCSObjectEvent event) {
        TCSObject<?> object = event.getCurrentOrPreviousObjectState();
        if (object instanceof Vehicle) {
            updateVehicleState(event);
        }
    }

    private void insertDataTable(Vehicle vehicle, AgvError agvError){
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        VehicleHistoryEntity vhe = new VehicleHistoryEntity();
        vhe.setVehicle(session.get(VehicleEntity.class, Integer.parseInt(vehicle.getProperty("id"))));
        vhe.setPosition(vehicle.getCurrentPosition() == null ? "0" : vehicle.getCurrentPosition().getName());
        vhe.setState(vehicle.getState().toString());
        vhe.setBattery(vehicle.getEnergyLevel());
        vhe.setCurrent(vehicle.getCurrent());
        vhe.setVoltage(vehicle.getVoltage());
        vhe.setTimeLog(new Timestamp(System.currentTimeMillis()));
        vhe.setNote(vehicle.getState() == Vehicle.State.ERROR ? agvError.name() : "");
        session.save(vhe);
        session.getTransaction().commit();
    }

    public void updateVehicleState(TCSObjectEvent event) {
        Vehicle.State stateOld = ((Vehicle) event.getPreviousObjectState()).getState();
        Vehicle.State stateNew = ((Vehicle) event.getCurrentObjectState()).getState();
        Vehicle vehicle = (Vehicle) event.getCurrentOrPreviousObjectState();
        if (stateOld.equals(Vehicle.State.UNKNOWN) && !stateNew.equals(Vehicle.State.UNKNOWN)) {
            insertDataTable(vehicle, null);
        }
        if (!stateOld.equals(Vehicle.State.UNKNOWN) && stateNew.equals(Vehicle.State.UNKNOWN)) {
            insertDataTable(vehicle,null);
        }
        if (!stateOld.equals(Vehicle.State.WARNING) && stateNew.equals(Vehicle.State.WARNING)) {
            insertDataTable(vehicle,null);
        }
        if (stateNew.equals(Vehicle.State.ERROR)) {
            updateVehicleError(event);
        }
    }

    public void updateVehicleError(TCSObjectEvent event) {
        ArrayList<AgvError> errors = getNewErrorFromPrevious(((Vehicle) event.getPreviousObjectState()).getErrorCode(),
                                                             ((Vehicle) event.getCurrentObjectState()).getErrorCode());
        if(errors.size() > 0){
            for(AgvError error : errors){
                insertDataTable((Vehicle) event.getCurrentOrPreviousObjectState(), error);
            }
        }
    }
    private ArrayList<AgvError> decodeToList(int errorCode) {
        ArrayList<AgvError> arrayList = new ArrayList<>();
        int i = 0;
        for (AgvError entry : AgvError.values()) {
            if (((errorCode >> i) & 1) == 1) {
                arrayList.add(entry);
            }
            i++;
        }
        return arrayList;
    }

    private ArrayList<AgvError> getNewErrorFromPrevious(int previousErrorCode, int errorCode) {
        int newErrors = (previousErrorCode ^ errorCode) & errorCode;
        return decodeToList(newErrors);
    }

    public enum AgvError {
        OUTLINE,
        LOSS_GUIDELINE,
        LOSS_CAN,
        OVERLOAD,
        E_STOP,
        LOW_BATTERY,
        WRONG_POINT,
    }
}
