package org.opentcs.database.logging;

import org.opentcs.database.logging.logentity.Logs;

import java.util.ArrayList;


public class LogManager extends ThreadManager<Logs> {
    @Override
    public void doProcess(ArrayList<Logs> items) {
        LogThread logThread = new LogThread();
        logThread.setItems(items);
        threadPool.submit(logThread);
    }
}
