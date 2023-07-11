package cfg.aubot.itteam.simulator;

import cfg.aubot.itteam.simulator.telegrams.*;
import org.eclipse.paho.client.mqttv3.*;

import java.util.*;

public class VirtualMqttAgv extends VirtualAgv implements MovingListener,MqttCallbackExtended {

  private IMqttClient mqttClient;
  private final String name;


  private final Timer loopProcess = new Timer();

  private Map<String, String> errors = new HashMap<>();

  private AgvVirtualError errorManager;
  protected LinkedList<OrderRequestMessage> orderQueue = new LinkedList<>();

  public VirtualMqttAgv(String name, String initialPosition) {
    this.name = name;
    this.position = initialPosition;

    // Khởi tạo Mqtt client
    try {
      mqttClient = new MqttClient("tcp://localhost:1883", name);
      mqttClient.setCallback(this);
    } catch (MqttException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void initialize() throws Exception {
    mqttClient.connect();
    System.out.println("connected");
  }

  @Override
  public void onDistanceChange(float distance) {
    this.distance = distance;
  }

  @Override
  public void onError(Map<String, String> errors) {
    if (!errors.isEmpty()) {
      if (operationState != 'E') previousStateBeforeError = operationState;
//            if (orderProcess.isAlive()) orderProcess.interrupt();
//            if (loopProcess.isAlive()) loopProcess.interrupt();
      operationState = 'E';
    } else {
      operationState = previousStateBeforeError;
//            if (orderProcess.isInterrupted()) orderProcess.start();
//            if (loopProcess.isInterrupted() && isLoop) loopProcess.start();
    }
    this.errors = errors;
  }

  @Override
  public void onError(int errors) {

  }

  @Override
  protected void setupAgv() throws MqttException {

    // Mỗi 0.5s publish vào topic agvs/state/<name>

    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        MqttTelegram mqttTelegram = new AgvStateMessage(name, String.format("%04d", position), operationState, loadState, voltage, current, energyLevel
                , String.format("%04d", nextPosition), distance);
        try {

          MqttMessage state = new MqttMessage(TelegramFactory.toJson(mqttTelegram).getBytes());
          state.setQos(0);

          mqttClient.publish("agvs/state/".concat(name), state);
        } catch (MqttException e) {
          e.printStackTrace();
        }
      }
    }, 0, 1000);


    // Subscribe vào topic agvs/req/<name>
    mqttClient.subscribe("agvs/req/".concat(name));

  }

  // Hàm xử lý hàng đợi message của xe
  @Override
  protected void processRequest() {

    // Lấy một request ra xử lý
    if (!orderQueue.isEmpty()) {
      try {
        OrderRequestMessage orderRequestMessage = orderQueue.peek();

        //executing
//        MqttTelegram exeResponseMessage = new OrderResponseMessage(orderRequestMessage.getThingName(),
//        orderRequestMessage.getRequestId(), OrderResponseMessage.OrderState.EXECUTING, "Executing");
//
//        MqttMessage executingMessage = new MqttMessage(TelegramFactory.toJson(exeResponseMessage).getBytes());

//        mqttClient.publish("agvs/res/".concat(name), executingMessage);
//        System.out.println("Published executing message");
        for (int i = 0; i < 5; i++) {
          Thread.sleep(500);
          distance += 0.25;
        }


        String destination = orderRequestMessage.getDestination();
//        operationState = 'A';
//        nextPosition = destination;


        //finished
        distance = 0;
        position = destination;
        nextPosition = "0000";
        operationState = 'I';

        char action = orderRequestMessage.getAction().charAt(0);
        loadState = action;

        // Xử lý xong thì publish vào agvs/res/<name> một order-response status FINISHED

        MqttTelegram finishResponseMessage = new OrderResponseMessage(orderRequestMessage.getThingName(),
                orderRequestMessage.getRequestId(), OrderResponseMessage.OrderState.FINISHED, "finished");

        MqttMessage finishMessage = new MqttMessage(TelegramFactory.toJson(finishResponseMessage).getBytes());

        mqttClient.publish("agvs/res/".concat(name), finishMessage);

        System.out.println("new position: " + position);

        orderQueue.remove();

      } catch (InterruptedException | MqttException e) {
        e.printStackTrace();
      }
    }
  }


  @Override
  public void connectComplete(boolean b, String s) {
    System.out.println("connect complete " + s);
  }

  @Override
  public void connectionLost(Throwable throwable) {
    System.out.println("Connect Lost");
  }

  @Override
  public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
    MqttTelegram mqttTelegram = TelegramFactory.parse(mqttMessage.toString());

    System.out.println("Has message: " + mqttTelegram.getClass().getCanonicalName());

    // Message nhận được thì chỉ xử lý message có type là order-request
    if (mqttTelegram instanceof OrderRequestMessage) {
      OrderRequestMessage orderRequestMessage = (OrderRequestMessage) mqttTelegram;

      // Message order-request gửi về thì xử lý cho đơn vào queue và publish vào agvs/res/<name> một order-response status RECEIVED
      orderQueue.add(orderRequestMessage);
      System.out.println("Added request message to queue");

      MqttTelegram orderResponseMessage = new OrderResponseMessage(orderRequestMessage.getThingName(),
              orderRequestMessage.getRequestId(), OrderResponseMessage.OrderState.RECEIVED, "Received");

      MqttMessage recievedMessage = new MqttMessage(TelegramFactory.toJson(orderResponseMessage).getBytes());

      mqttClient.publish("agvs/res/".concat(name), recievedMessage);
      System.out.println("Published recieved message");

    }
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

  }
}