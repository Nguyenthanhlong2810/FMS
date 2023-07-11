package org.opentcs.database.logging;

import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract class ThreadManager<T> {
    public final int BATCH_SIZE = 10;//process doProcess when queue há more than 10 element
    public final long WAIT_TIME_OUT = 1000; //ms
    public final long TIME_OUT = 2 * 1000; //ms. process doProcess with period  2 second
    private final Logger logger = org.apache.logging.log4j.LogManager.getLogger(ThreadManager.class);
    private final BlockingQueue<T> sourceQueue = new LinkedBlockingQueue<>(); // queue that save logs need to insert into database
    protected ArrayList<T> items = new ArrayList<>(BATCH_SIZE);//save object from  sourceQueue transmitted to doProcess to insert into database
    protected AtomicBoolean shouldWork = new AtomicBoolean(true);
    protected AtomicBoolean isRunning = new AtomicBoolean(true);
    private boolean listening = false;
    private String name = "ACTION/API LOGGER";
    protected ExecutorService threadPool = Executors.newFixedThreadPool(5);
    private Thread mainThread;

    public ThreadManager() {
        logger.debug("Start task manager named: " + name);
        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Queued job manager " + name + " is running and watching for queue... ");
                isRunning.set(true);
                int recNum = 0;
                long lgnStart = System.currentTimeMillis();
                while (shouldWork.get()) {
                    try {
                        T item = sourceQueue.poll(WAIT_TIME_OUT, TimeUnit.MILLISECONDS);//remove item from  BlockingQueue. Return null if queue blank
                        if (item != null) {
                            items.add(item);
                            recNum++;
                        }

                        if (recNum >= BATCH_SIZE || timedOut(lgnStart)) {
                            if (items.size() > 0) {
                                logger.info(String.format("Thread %s: %s submits %d item(s)",
                                        Thread.currentThread().getName(), name, items.size()));
                                doProcess(items);
                                items = new ArrayList<>(BATCH_SIZE);
                                lgnStart = System.currentTimeMillis();
                                recNum = 0;
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                    isRunning.set(false);
                }
                logger.info("Taskmanager " + name + " is stopped!!");
            }

            private boolean timedOut(Long startTime) {
                return System.currentTimeMillis() - startTime > TIME_OUT;
            }
        });

    }

    /**
     * abstract method insert data into database
     * @param items
     */
    public abstract void doProcess(ArrayList<T> items);

    /**
     * start listen to data from queue
     */
    public synchronized void listen() {
        if (!listening) {
            mainThread.start();
            listening = true;
        }
    }

    public BlockingQueue<T> getSourceQueue() {
        return sourceQueue;
    }

    public void stop() {
        logger.info(String.format("%s received a termination signal, stopping ... ", name));
        this.shouldWork.set(false);
        int tryTime = 0;
        while (isRunning.get() && tryTime < 50) {
            try {
                Thread.sleep(50L);
            } catch (Exception ignored) {}
            tryTime++;
        }
    }

    /**
     * Submit log into queue
     * @param item
     */
    public void submit(T item) {
        sourceQueue.offer(item);
    }//add object to the last position of BlockingQueue. return false if queue is full
}

