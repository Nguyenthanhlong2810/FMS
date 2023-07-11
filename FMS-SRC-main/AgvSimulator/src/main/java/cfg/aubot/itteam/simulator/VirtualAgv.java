package cfg.aubot.itteam.simulator;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Timer;
import java.util.TimerTask;

public abstract class VirtualAgv extends Thread {
  protected String position = "0005";
  protected String nextPosition = "0000";
  protected char operationState = 'I';
  protected char previousStateBeforeError;
  protected char loadState = 'E';
  protected int energyLevel = 99;
  protected float current = 5.425f;  //(dòng điện i=5.5V)
  protected float voltage = 12.732f;  //(điện áp = 12.7W)
  protected long PROCESS_TIME = 5000;
  protected float distance = 0;
  protected boolean isLoop = false;
  protected WorkingRoutes routes = new WorkingRoutes();
  protected WorkingRoutes tempRoutes = new WorkingRoutes();

  protected Timer orderProcess = new Timer();

  @Override
  public void run() {
    //xu li order
    orderProcess = new Timer();
    orderProcess.schedule(new TimerTask() {
      @Override
      public void run() {
        processRequest();
      }
    }, 0, 3000);

    //pub
    try {
      setupAgv();
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  protected abstract void setupAgv() throws MqttException;

  protected abstract void processRequest();

  public void open() throws  Exception{
    initialize();
    this.start();
  }
  public abstract void initialize() throws Exception;
}
