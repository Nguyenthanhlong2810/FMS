/**
 * Copyright (c) Fraunhofer IML
 */
package cfg.aubot.commadapter.tcp;

import cfg.aubot.commadapter.tcp.netty.ConnectionEventListener;
import cfg.aubot.commadapter.tcp.netty.TcpClientChannelManager;
import com.google.common.primitives.Ints;
import com.google.inject.assistedinject.Assisted;
import cfg.aubot.commadapter.tcp.comm.VehicleTelegramDecoder;
import cfg.aubot.commadapter.tcp.comm.VehicleTelegramEncoder;
import cfg.aubot.commadapter.tcp.exchange.ExampleProcessModelTO;
import cfg.aubot.commadapter.tcp.telegrams.*;
import cfg.aubot.commadapter.tcp.telegrams.StateResponse.LoadState;
import org.opentcs.common.MoveState;
import org.opentcs.data.model.WorkingRoute;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.example.dispatching.LoadAction;
import org.opentcs.example.telegrams.*;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.SimVehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static org.opentcs.data.order.DriveOrder.Destination.OP_NOP;
import static org.opentcs.example.telegrams.BoundedCounter.UINT16_MAX_VALUE;
import static java.util.Objects.requireNonNull;

/**
 * An example implementation for a communication adapter.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class ExampleCommAdapter
    extends BasicVehicleCommAdapter
    implements ConnectionEventListener<Response>,
               TelegramSender,
               SimVehicleCommAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ExampleCommAdapter.class);
  /**
   * Maps movement commands from openTCS to the telegrams sent to the attached vehicle.
   */
  private final OrderMapper orderMapper;
  /**
   * The components factory.
   */
  private final ExampleAdapterComponentsFactory componentsFactory;
  /**
   * The kernel's executor service.
   */
  private final ExecutorService kernelExecutor;
  /**
   * Manages counting the ids for all {@link Request} telegrams.
   */
  private final BoundedCounter globalRequestCounter = new BoundedCounter(0, UINT16_MAX_VALUE);
  /**
   * Maps commands to order IDs so we know which command to report as finished.
   */
  private final Map<MovementCommand, Integer> orderIds = new ConcurrentHashMap<>();
  /**
   * Manages the channel to the vehicle.
   */
  private TcpClientChannelManager<Request, Response> vehicleChannelManager;
  /**
   * Matches requests to responses and holds a queue for pending requests.
   */
  private RequestResponseMatcher requestResponseMatcher;
  /**
   * A task for enqueuing state requests periodically.
   */
  private StateRequesterTask stateRequesterTask;
  /**
   * Creates a new instance.
   *
   * @param vehicle The attached vehicle.
   * @param orderMapper The order mapper for movement commands.
   * @param componentsFactory The components factory.
   * @param kernelExecutor The kernel's executor service.
   */
  @Inject
  public ExampleCommAdapter(@Assisted Vehicle vehicle,
                            OrderMapper orderMapper,
                            ExampleAdapterComponentsFactory componentsFactory,
                            @KernelExecutor ExecutorService kernelExecutor) {
    super(new ExampleProcessModel(vehicle), 3, 2, LoadAction.CHARGE, kernelExecutor);
    this.orderMapper = requireNonNull(orderMapper, "orderMapper");
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
  }

  @Override
  public void initialize() {
    super.initialize();
    this.requestResponseMatcher = componentsFactory.createRequestResponseMatcher(this);
    this.stateRequesterTask = componentsFactory.createStateRequesterTask(e -> {
      if (!getProcessModel().isVehicleIdle() && requestResponseMatcher.getSize() < 20) {
        requestResponseMatcher.enqueueRequest(new StateRequest(Telegram.ID_DEFAULT));
      }
    });
  }

  @Override
  public void terminate() {
    stateRequesterTask.disable();
    super.terminate();
  }

  @Override
  public synchronized void enable() {
    if (isEnabled()) {
      return;
    }

    //Create the channel manager responsible for connections with the vehicle
    vehicleChannelManager = new TcpClientChannelManager<>(this,
                                                          this::getChannelHandlers,
                                                          getProcessModel().getVehicleIdleTimeout(),
                                                          getProcessModel().isLoggingEnabled());
    //Initialize the channel manager
    vehicleChannelManager.initialize();
    super.enable();
  }

  @Override
  public synchronized void disable() {
    if (!isEnabled()) {
      return;
    }

    super.disable();
    vehicleChannelManager.terminate();
    vehicleChannelManager = null;
  }

  @Override
  public synchronized void clearCommandQueue() {
    super.clearCommandQueue();
    orderIds.clear();
  }

  @Override
  public void initVehiclePosition(String newPos) {
    kernelExecutor.submit(() -> {
      getProcessModel().setVehiclePosition(newPos);
    });
  }

  @Override
  protected synchronized void connectVehicle() {
    if (vehicleChannelManager == null) {
      LOG.warn("{}: VehicleChannelManager not present.", getName());
      return;
    }

    vehicleChannelManager.connect(getProcessModel().getVehicleHost(),
                                  getProcessModel().getVehiclePort());
  }

  @Override
  protected synchronized void disconnectVehicle() {
    if (vehicleChannelManager == null) {
      LOG.warn("{}: VehicleChannelManager not present.", getName());
      return;
    }

    vehicleChannelManager.disconnect();
  }

  @Override
  protected synchronized boolean isVehicleConnected() {
    return vehicleChannelManager != null && vehicleChannelManager.isConnected();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    super.propertyChange(evt);
    if (!(evt.getSource() instanceof ExampleProcessModel)) {
      return;
    }

    // Handling of events from the vehicle gui panels start here
    if (Objects.equals(evt.getPropertyName(),
                       VehicleProcessModel.Attribute.COMM_ADAPTER_CONNECTED.name())) {
      if (getProcessModel().isCommAdapterConnected()) {
        // Once the connection is established, ensure that logging is enabled/disabled for it as
        // configured by the user.
        vehicleChannelManager.setLoggingEnabled(getProcessModel().isLoggingEnabled());
      }
    }
    if (Objects.equals(evt.getPropertyName(),
                       VehicleProcessModel.Attribute.COMM_ADAPTER_CONNECTED.name())
        || Objects.equals(evt.getPropertyName(),
                          ExampleProcessModel.Attribute.PERIODIC_STATE_REQUESTS_ENABLED.name())) {
      if (getProcessModel().isCommAdapterConnected()
          && getProcessModel().isPeriodicStateRequestEnabled()) {
        stateRequesterTask.enable();
      }
      else {
        stateRequesterTask.disable();
      }
    }
    if (Objects.equals(evt.getPropertyName(),
                       ExampleProcessModel.Attribute.PERIOD_STATE_REQUESTS_INTERVAL.name())) {
      stateRequesterTask.setRequestInterval(getProcessModel().getStateRequestInterval());
    }
  }

  @Override
  public final ExampleProcessModel getProcessModel() {
    return (ExampleProcessModel) super.getProcessModel();
  }

  @Override
  protected VehicleProcessModelTO createCustomTransferableProcessModel() {
    //Add extra information of the vehicle when sending to other software like control center or 
    //plant overview
    return new ExampleProcessModelTO()
        .setVehicleRef(getProcessModel().getVehicleReference())
        .setCurrentState(getProcessModel().getCurrentState())
        .setPreviousState(getProcessModel().getPreviousState())
        .setLastOrderSent(getProcessModel().getLastOrderSent())
        .setDisconnectingOnVehicleIdle(getProcessModel().isDisconnectingOnVehicleIdle())
        .setLoggingEnabled(getProcessModel().isLoggingEnabled())
        .setReconnectDelay(getProcessModel().getReconnectDelay())
        .setReconnectingOnConnectionLoss(getProcessModel().isReconnectingOnConnectionLoss())
        .setVehicleHost(getProcessModel().getVehicleHost())
        .setVehicleIdle(getProcessModel().isVehicleIdle())
        .setVehicleIdleTimeout(getProcessModel().getVehicleIdleTimeout())
        .setVehiclePort(getProcessModel().getVehiclePort())
        .setPeriodicStateRequestEnabled(getProcessModel().isPeriodicStateRequestEnabled())
        .setStateRequestInterval(getProcessModel().getStateRequestInterval());
  }

  @Override
  public synchronized void sendCommand(MovementCommand cmd)
      throws IllegalArgumentException {
    requireNonNull(cmd, "cmd");

    try {
      OrderRequest telegram = orderMapper.mapToOrder(cmd);
      orderIds.put(cmd, telegram.getOrderId());
      LOG.debug("{}: Enqueuing order request with: order id={}, dest. id={}, dest. action={}",
                getName(),
                telegram.getOrderId(),
                telegram.getDestinationId(),
                telegram.getDestinationAction());

      // Add the telegram to the queue. Telegram will be send later when its the first telegram in 
      // the queue. This ensures that we always wait for a response until we send a new request.
      requestResponseMatcher.enqueueRequest(telegram);
      //requestResponseMatcher.enqueueRequest(telegram);
    }
    catch (IllegalArgumentException exc) {
      LOG.error("{}: Failed to enqueue command {}", getName(), cmd, exc);
    }
  }

  @Override
  public synchronized ExplainedBoolean canProcess(List<String> operations) {
    requireNonNull(operations, "operations");
    boolean canProcess = true;
    String reason = "";
    if (!isEnabled()) {
      canProcess = false;
      reason = "Adapter not enabled";
    }
    if (canProcess && !isVehicleConnected()) {
      canProcess = false;
      reason = "Vehicle does not seem to be connected";
    }
    if (canProcess
        && getProcessModel().getCurrentState().getLoadState() == LoadState.UNKNOWN) {
      canProcess = false;
      reason = "Vehicle's load state is undefined";
    }
    boolean loaded = getProcessModel().getCurrentState().getLoadState() == LoadState.FULL;
    final Iterator<String> opIter = operations.iterator();
//    while (canProcess && opIter.hasNext()) {
//      final String nextOp = opIter.next();
//      // If we're loaded, we cannot load another piece, but could unload.
//      if (loaded) {
//        if (nextOp.startsWith(LoadAction.LOAD)) {
//          canProcess = false;
//          reason = "Cannot load when already loaded";
//        }
//        else if (nextOp.startsWith(LoadAction.UNLOAD)) {
//          loaded = false;
//        }
//        else if (nextOp.startsWith(DriveOrder.Destination.OP_PARK)) {
//          canProcess = false;
//          reason = "Vehicle shouldn't park while in a loaded state.";
//        }
//        else if (nextOp.startsWith(LoadAction.CHARGE)) {
//          canProcess = false;
//          reason = "Vehicle shouldn't charge while in a loaded state.";
//        }
//      }
//      // If we're not loaded, we could load, but not unload.
//      else if (nextOp.startsWith(LoadAction.LOAD)) {
//        loaded = true;
//      }
//      else if (nextOp.startsWith(LoadAction.UNLOAD)) {
//        canProcess = false;
//        reason = "Cannot unload when not loaded";
//      }
//    }
    return new ExplainedBoolean(canProcess, reason);
  }

  @Override
  public void processMessage(Object message) {
    if (message instanceof MoveState) {
      if (isVehicleConnected()) {
        requestResponseMatcher.enqueueRequest(new MovingRequest(Telegram.ID_DEFAULT, (MoveState) message));
      }
    } else if (message instanceof List) {
      if (((List<?>) message).get(0) instanceof DriveOrder) {
        List<DriveOrder> driveOrders = (List<DriveOrder>) message;
        final int[] count = {driveOrders.stream().mapToInt(driveOrder -> driveOrder.getRoute().getSteps().size()).sum()};
        driveOrders.forEach(driveOrder -> {
          Iterator<Route.Step> stepIter = driveOrder.getRoute().getSteps().iterator();
          while (stepIter.hasNext()) {
            Route.Step curStep = stepIter.next();
            boolean isFinalMovement = !stepIter.hasNext();
            requestResponseMatcher.enqueueRequest(orderMapper.mapToRouteStep(curStep,
                    isFinalMovement ? driveOrder.getDestination().getOperation() : OP_NOP, count[0]--));
          }
        });
      }
    }
  }

  @Override
  public void sendVehicleRoutes(int mapId, Map<Integer, Route> routes) {
    stateRequesterTask.disable();
    routes.forEach((index, route) -> {
      Map<String, Character> pointDirections = new LinkedHashMap<>();
      route.getSteps().forEach(step -> {
        char direction = 'S';
        if (Objects.equals(step.getDestinationPoint().getName(), step.getSourcePoint().getLeftPoint())) {
          direction = 'L';
        } else if (Objects.equals(step.getDestinationPoint().getName(), step.getSourcePoint().getRightPoint())) {
          direction = 'R';
        }
        pointDirections.put(step.getSourcePoint().getName(), direction);
      });
      requestResponseMatcher.enqueueRequest(new RouteRequest(mapId, routes.size(), index, pointDirections));
    });
    stateRequesterTask.enable();
  }

  @Override
  public void askVehicleCurrentRoute(int mapId) {

  }

  @Override
  public void setVehicleCurrentRoute(WorkingRoute.WorkingRouteRaw workingRoute) {
    requestResponseMatcher.enqueueRequest(new SetRouteRequest(workingRoute));
  }

  @Override
  public void onConnect() {
    if (!isEnabled()) {
      return;
    }
    LOG.debug("{}: connected", getName());
    getProcessModel().setCommAdapterConnected(true);
//    getProcessModel().setVehicleState(Vehicle.State.IDLE);
    // Request the vehicle's current state (preparation for the state requester task)
    requestResponseMatcher.clearRouteRequests();
    requestResponseMatcher.enqueueRequest(new StateRequest(Telegram.ID_DEFAULT));
    requestResponseMatcher.enqueueRequest(new AskRouteRequest(0));
    // Check for resending last request
    requestResponseMatcher.checkForSendingNextRequest();
  }

  @Override
  public void onFailedConnectionAttempt() {
    if (!isEnabled()) {
      return;
    }
    getProcessModel().setCommAdapterConnected(false);
    if (isEnabled() && getProcessModel().isReconnectingOnConnectionLoss()) {
      vehicleChannelManager.scheduleConnect(getProcessModel().getVehicleHost(),
                                            getProcessModel().getVehiclePort(),
                                            getProcessModel().getReconnectDelay());
    }
  }

  @Override
  public void onDisconnect() {
    LOG.debug("{}: disconnected", getName());
    getProcessModel().setCommAdapterConnected(false);
    getProcessModel().setVehicleIdle(true);
    getProcessModel().setVehicleState(Vehicle.State.UNKNOWN);
    if (isEnabled() && getProcessModel().isReconnectingOnConnectionLoss()) {
      vehicleChannelManager.scheduleConnect(getProcessModel().getVehicleHost(),
                                            getProcessModel().getVehiclePort(),
                                            getProcessModel().getReconnectDelay());
    }
  }

  @Override
  public void onIdle() {
    LOG.debug("{}: idle", getName());
    getProcessModel().setVehicleIdle(true);
    // If we are supposed to reconnect automatically, do so.
    if (isEnabled() && getProcessModel().isDisconnectingOnVehicleIdle()) {
      LOG.debug("{}: Disconnecting on idle timeout...", getName());
      disconnectVehicle();
    }
  }

  @Override
  public synchronized void onIncomingTelegram(Response response) {
    requireNonNull(response, "response");

    // Remember that we have received a sign of life from the vehicle
    getProcessModel().setVehicleIdle(false);

    //Check if the response matches the current request
    if (!requestResponseMatcher.tryMatchWithCurrentRequest(response)) {
      // XXX Either ignore the message or close the connection
      return;
    }

    if (response instanceof StateResponse) {
      onStateResponse((StateResponse) response);
    } else if (response instanceof ErrorResponse) {
      handleErrorMessage((ErrorResponse) response);
    } else if (response instanceof RouteResponse) {
      checkRouteSent((RouteResponse) response);
    } else if (response instanceof CurrentRouteResponse) {
      updateVehicleCurrentRoute((CurrentRouteResponse) response);
    } else if (response instanceof SetRouteResponse) {
      checkVehicleCurrentRoute((SetRouteResponse) response);
    } else {
      LOG.debug("{}: Receiving response: {}", getName(), response);
      System.out.println("Response: " + response.getRawContent()[3] + response.getRawContent()[4] + " " + response.getRawContent()[5] + response.getRawContent()[6]);
    }

    //Send the next telegram if one is waiting
    requestResponseMatcher.checkForSendingNextRequest();
  }

  @Override
  public synchronized void sendTelegram(Request telegram) {
    requireNonNull(telegram, "telegram");
    if (!isVehicleConnected()) {
      LOG.debug("{}: Not connected - not sending request '{}'",
                getName(),
                telegram);
      return;
    }

    // Update the request's id
    telegram.updateRequestContent(globalRequestCounter.getAndIncrement());

    vehicleChannelManager.send(telegram);

    // If the telegram is an order, remember it.
    if (telegram instanceof OrderRequest) {
      System.out.println("Request: " + telegram.getRawContent()[3] + telegram.getRawContent()[4] + " " + telegram.getRawContent()[5] + telegram.getRawContent()[6]
       + " " + (char) telegram.getRawContent()[9]);
      getProcessModel().setLastOrderSent((OrderRequest) telegram);
    }

    // If we just sent a state request, restart the state requester task to schedule the next
    // state request
    if (telegram instanceof StateRequest
        && getProcessModel().isPeriodicStateRequestEnabled()) {
      stateRequesterTask.restart();
    }
  }

  public RequestResponseMatcher getRequestResponseMatcher() {
    return requestResponseMatcher;
  }

  private void onStateResponse(StateResponse stateResponse) {
    requireNonNull(stateResponse, "stateResponse");

    final StateResponse previousState = getProcessModel().getCurrentState();
    final StateResponse currentState = stateResponse;


    kernelExecutor.submit(() -> {
      // Update the vehicle's current state and remember the old one.
      getProcessModel().setPreviousState(previousState);
      getProcessModel().setCurrentState(currentState);

      checkForVehiclePositionUpdate(previousState, currentState);
      checkForVehiclePropertiesUpdate(previousState, currentState);
      checkForVehicleStateUpdate(previousState, currentState);
      checkOrderFinished(previousState, currentState);

      // XXX Process further state updates extracted from the telegram here.
    });
  }

  //adding energy level information - customized
  private void checkForVehiclePropertiesUpdate(StateResponse previousState,
                                               StateResponse currentState) {
    if (previousState.getEnergyLevel() != currentState.getEnergyLevel()) {
      getProcessModel().setVehicleEnergyLevel(currentState.getEnergyLevel());
    }

    if (previousState.getVoltage() != currentState.getVoltage()) {
      getProcessModel().setVehicleVoltage(currentState.getVoltage());
    }

    if (previousState.getCurrent() != currentState.getCurrent()) {
      getProcessModel().setVehicleCurrent(currentState.getCurrent());
    }
  }

  private void checkForVehiclePositionUpdate(StateResponse previousState,
                                             StateResponse currentState) {
    String currentPosition = String.valueOf(currentState.getPositionId());
    LOG.debug("{}: Vehicle is now at point {}", getName(), currentPosition);
    //    if (currentState.getPositionId() != 0) {
    getProcessModel().setVehiclePosition(currentPosition);
    //    }
//      if (currentState.getNextPosition() != 0) {
    getProcessModel().setVehicleNextPosition(currentState.getNextPosition());
//      }
    if (getProcessModel().getVehiclePosition() != null) {
      getProcessModel().setVehiclePrecisePositionByDistance(currentState.getDistance());
    }
  }

  private void checkForVehicleStateUpdate(StateResponse previousState,
                                          StateResponse currentState) {
//    if (previousState.getOperationState() == currentState.getOperationState()
//            && currentState.getOperationState() != StateResponse.OperationState.ERROR) {
//      return;
//    }
//    if (currentState.getOperationState() == StateResponse.OperationState.ERROR) {
//      requestResponseMatcher.enqueueRequest(new ErrorRequest(Telegram.ID_DEFAULT));
//    }
    getProcessModel().setVehicleState(translateVehicleState(currentState.getOperationState()));
    getProcessModel().setVehicleWarningState(currentState.isWarning());
    getProcessModel().setVehicleErrorCode(currentState.getErrorCode());
  }

  private void checkOrderFinished(StateResponse previousState, StateResponse currentState) {
    if (currentState.getLastFinishedOrderId() == 0) {
      if (previousState.getLastReceivedOrderId() != 0 && currentState.getLastReceivedOrderId() == 0) {
          cancelAllCommand();
        return;
      }
    }

//    if (currentState.getOperationState() == StateResponse.OperationState.ERROR) {
//      if (currentState.isWrongPoint()) {
//        cancelAllCommand();
//        requestResponseMatcher.enqueueRequest(orderMapper.getNullOrderRequest(currentState));
//        return;
//      }
//    }
    // If the last finished order ID hasn't changed, don't bother.
    if (previousState.getLastFinishedOrderId() == currentState.getLastFinishedOrderId()) {
      return;
    }
    // Check if the new finished order ID is in the queue of sent orders.
    // If yes, report all orders up to that one as finished.
    if (!orderIds.containsValue(currentState.getLastFinishedOrderId())) {
      LOG.debug("{}: Ignored finished order ID {} (reported by vehicle, not found in sent queue).",
                getName(),
                currentState.getLastFinishedOrderId());
      return;
    }

    Iterator<MovementCommand> cmdIter = getSentQueue().iterator();
    boolean finishedAll = false;
    while (!finishedAll && cmdIter.hasNext()) {
      MovementCommand cmd = cmdIter.next();
      cmdIter.remove();
      int orderId = orderIds.remove(cmd);
      if (orderId == currentState.getLastFinishedOrderId()) {
        finishedAll = true;
      }

      LOG.debug("{}: Reporting command with order ID {} as executed: {}", getName(), orderId, cmd);
      getProcessModel().commandExecuted(cmd);
      if (cmd.getLoopCount() == 0) {
        orderMapper.setLoopMode(false);
      } else if (cmd.getLoopCount() == 2) {
        orderMapper.setLoopMode(true);
        getProcessModel().setVehicleIntegrationLevelToRespect();
      }
    }
  }

  private void cancelAllCommand() {
    Iterator<MovementCommand> cmdIter = getSentQueue().iterator();
    while (cmdIter.hasNext()) {
      MovementCommand cmd = cmdIter.next();
      cmdIter.remove();
      orderIds.remove(cmd);
      getProcessModel().commandFailed(cmd);
    }
  }

  private void handleErrorMessage(ErrorResponse errorResponse) {
    String previousMessage = getProcessModel().getErrorMessageAsString();
    String currentMessage = errorResponse.getMessage();

    if (previousMessage.equals(errorResponse.getMessage())) {
      return;
    }
    getProcessModel().setVehicleErrorsMessage(currentMessage);
  }


  private void checkRouteSent(RouteResponse response) {
    RouteRequest request = (RouteRequest) response.getRequest();
    int error = 0;
    if (response.getMapId() != request.getMapId()) {
      error = 1;
    }
    if (response.getRouteCount() != request.getRouteCount()) {
      error = 2;
    }
    if (response.getRouteId() != request.getRouteId()) {
      error = 3;
    }
    if (response.getRouteState() == 0) {
      error = 4;
    }
    if (response.isFinalRoute() && response.getRouteState() == 'E') {
      error = 5;
    }
    if (error > 0) {
      getProcessModel().syncRoutesFailed(String.format("Syncing route error 0%d", error));
      requestResponseMatcher.clearRouteRequests();
    } else if (response.isFinalRoute()) {
      getProcessModel().syncRoutesSuccess(response.getMapId());
    }
  }

  private void updateVehicleCurrentRoute(CurrentRouteResponse response) {
    getProcessModel().setVehicleCurrentRoute(new WorkingRoute.WorkingRouteRaw(response.getMapId(),
                                                                         response.getRouteId(),
                                                                         response.getPointActions()));
  }

  private void checkVehicleCurrentRoute(SetRouteResponse response) {

  }

  /**
   * Map the vehicle's operation states to the kernel's vehicle states.
   *
   * @param operationState The vehicle's current operation state.
   */
  private Vehicle.State translateVehicleState(StateResponse.OperationState operationState) {
    switch (operationState) {
      case IDLE:
        return Vehicle.State.IDLE;
      case MOVING:
      case ACTING:
        return Vehicle.State.EXECUTING;
      case WARNING:
        return Vehicle.State.WARNING;
      case CHARGING:
        return Vehicle.State.CHARGING;
      case ERROR:
        return Vehicle.State.ERROR;
      default:
        LOG.debug("{}: Vehicle Unknown state {}", getName(), operationState);
        return Vehicle.State.UNKNOWN;
    }
  }

  /**
   * Returns the channel handlers responsible for writing and reading from the byte stream.
   *
   * @return The channel handlers responsible for writing and reading from the byte stream
   */
  private List<ChannelHandler> getChannelHandlers() {
    return Arrays.asList(//new LengthFieldBasedFrameDecoder(getMaxTelegramLength(), 1, 1, 2, 0),
                         new VehicleTelegramDecoder(this),
                         new VehicleTelegramEncoder());
  }

  private int getMaxTelegramLength() {
    return Ints.max(MovingResponse.TELEGRAM_LENGTH,
                    OrderResponse.TELEGRAM_LENGTH,
                    StateResponse.TELEGRAM_LENGTH,
                    ErrorResponse.TELEGRAM_LENGTH);
  }
}
