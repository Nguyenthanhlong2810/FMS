package cfg.aubot.itteam.simulator;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class VirtualTCPAgv extends VirtualAgv implements MovingListener {

    private int port;
    private ServerSocket server;
    Socket client;

    protected LinkedList<byte[]> orderQueue = new LinkedList<>();

    private int lastReceivedOrderId = 0;
    private int currentOrderId = 0;
    private int lastFinishedOrderId = 0;

    private Timer loopProcess = new Timer();


    private LinkedList<byte[]> loopOrderQueue = new LinkedList<>();

    private int errorCode = 0;

    private Map<String, String> errors = new HashMap<>();

    private AgvVirtualError errorManager;

    public VirtualTCPAgv(int port, String initialPosition) {
        this.port = port;
        this.position = initialPosition;
    }

    public void addErrorManager(AgvVirtualError errorManager) {
        this.errorManager = errorManager;
    }

    public void open() {
        try {
            server = new ServerSocket(port);
            System.out.println("Server is open on port " + port);
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize() throws Exception {

    }

    @Override
    protected void setupAgv() {
        loopProcess = new Timer();
        loopProcess.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isLoop) {
                    try {
                        byte[] request = loopOrderQueue.peek();
                        processStepOrder(request);
                        loopOrderQueue.remove();
                        loopOrderQueue.add(request);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 1);

        if (errorManager != null) {
            errorManager.setVisible(true);
        }

        while (true) {
            try {
                client = server.accept();
                System.out.println("Client " + client.getRemoteSocketAddress() + " connected");
                DataInputStream dis = new DataInputStream(client.getInputStream());
                while (true) {
                    byte[] request;
                    byte[] response = new byte[1];
                    dis.readByte();
                    int length = dis.readByte();
                    int type = dis.readByte();
                    request = new byte[length + 1];
                    dis.read(request);
                    switch (type) {
                        case 1:
                            response = createStateResponse(request);
                            break;
                        case 2:
                            response = processOrderRequest(request);
                            break;
                        case 3:
                            response = createErrorResponse(request);
                            break;
                        case 4:
                            response = createMovingResponse(request);
                            break;
                        case 5:
                            response = processRouteRequest(request);
                            break;
                        case 6:
                            response = processCurrentRouteRequest(request);
                            break;
                        case 7:
                            response = processSetRouteRequest(request);
                    }

                    BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());
                    bos.write(response);
                    bos.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Socket closed.");
            } finally {
                closeSocket(client);
            }
        }
    }

    @Override
    protected void processRequest() {
        if (!orderQueue.isEmpty()) {
            try {
                Thread.sleep(100);
                byte[] request = orderQueue.peek();
                processStepOrder(request);
                if (request[8] == 1) {
                    isLoop = true;
                }
                orderQueue.remove();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] createStateResponse(byte[] request) {
        byte[] response = new byte[42];
        byte[] positionByte = position.getBytes();
        byte[] nextPositionByte = nextPosition.getBytes();
        byte[] lastReceivedOrderIdByte = convertIntToByte2(lastFinishedOrderId);
        byte[] currentOrderIdByte = convertIntToByte2(currentOrderId);
        byte[] lastFinishedOrderIdByte = convertIntToByte2(lastFinishedOrderId);
        byte[] getCurrent = convertFloatTo4Bytes(current);
        byte[] getVoltage = convertFloatTo4Bytes(voltage);
        byte[] distanceByte = convertFloatTo4Bytes(distance);
        byte[] errorCodeByte = convertIntToByte2(errorCode);

        response[0] = (byte) 0xFD;
        response[1] = (byte) (response.length - 4);
        response[2] = (byte) 1;
        response[3] = request[0];
        response[4] = request[1];
        response[5] = positionByte[0];
        response[6] = positionByte[1];
        response[7] = positionByte[2];
        response[8] = positionByte[3];
        response[9] = (byte) operationState;
        response[10] = (byte) loadState;
        response[11] = lastReceivedOrderIdByte[0];
        response[12] = lastReceivedOrderIdByte[1];
        response[13] = currentOrderIdByte[0];
        response[14] = currentOrderIdByte[1];
        response[15] = lastFinishedOrderIdByte[0];
        response[16] = lastFinishedOrderIdByte[1];
        response[17] = getVoltage[0];
        response[18] = getVoltage[1];
        response[19] = getVoltage[2];
        response[20] = getVoltage[3];
        response[21] = getCurrent[0];
        response[22] = getCurrent[1];
        response[23] = getCurrent[2];
        response[24] = getCurrent[3];
        response[25] = (byte) energyLevel;
        response[26] = distanceByte[0];
        response[27] = distanceByte[1];
        response[28] = distanceByte[2];
        response[29] = distanceByte[3];
        response[30] = 0;
        response[31] = nextPositionByte[0];
        response[32] = nextPositionByte[1];
        response[33] = nextPositionByte[2];
        response[34] = nextPositionByte[3];
        response[35] = errorCodeByte[0];
        response[36] = errorCodeByte[1];
        response[37] = 0;
        response[38] = 0;
        response[39] = 0;
        response[40] = getCheckSum(response);
        response[41] = (byte) 0xFE;

//        System.out.print("State Response: ");
//        for (int i = 0; i < response.length - 2; i++) {
//            System.out.print(i + 2 + "|" + (int) response[i] + "  ");
//        }
//        System.out.println();
//        System.out.println(distance);

        return response;
    }

    private byte[] processOrderRequest(byte[] request) {
        lastReceivedOrderId = convertByte2ToInt(request[2], request[3]);
        orderQueue.add(request);
        if (request[8] != 0) {
            loopOrderQueue.add(request);
        } else if (isLoop) {
            loopOrderQueue.clear();
            isLoop = false;
        }
        return createOrderResponse(request);
    }

    private void processStepOrder(byte[] request) throws InterruptedException {
        currentOrderId = convertByte2ToInt(request[2], request[3]);
        operationState = 'M';
        String destination = new String(new byte[] {request[4], request[5], request[6], request[7]});
        nextPosition = destination;

        float process = 0;
        do {
            distance = process;
            process += 0.05;
            Thread.sleep(20);
        } while (process < 5);
        distance = 0;

        position = destination;
        nextPosition = "0000";
        char action = (char) request[6];
        if (action == 'L') {
            loadState = 'F';
        } else if (action == 'U') {
            loadState = 'E';
        }
        lastFinishedOrderId = currentOrderId;
        currentOrderId = 0;
        operationState = 'I';
    }

    private byte[] createOrderResponse(byte[] request) {
        byte[] response = new byte[9];
        byte[] lastReceivedOrderIdByte = convertIntToByte2(lastReceivedOrderId);
        response[0] = (byte) 0xFD;
        response[1] = (byte) (response.length - 4);
        response[2] = (byte) 2;
        response[3] = request[0];
        response[4] = request[1];
        response[5] = lastReceivedOrderIdByte[0];
        response[6] = lastReceivedOrderIdByte[1];
        response[7] = getCheckSum(response);
        response[8] = (byte) 0xFE;

//        System.out.print("Order Request: ");
//        for (int i = 0; i < request.length - 2; i++) {
//            System.out.print(i + 2 + "|" + (int) request[i] + "  ");
//        }
//        System.out.println();
        return response;
    }

    private byte[] processRouteRequest(byte[] request) {
        tempRoutes.setMapId(convertByte2ToInt(request[2], request[3]));
        Map<String, String> pointDirections = new HashMap<>();
        int index = 6;
        while (index + 5 <= request.length) {
            pointDirections.put(new String(new byte[]{
                    request[index++],
                    request[index++],
                    request[index++],
                    request[index++]
            }), String.valueOf(request[index++]));
        }
        tempRoutes.getRoutes().add(new WorkingRoutes.Route(request[5], pointDirections));
        boolean isDone = tempRoutes.getRoutes().size() == request[4];
        if (isDone) {
            routes = tempRoutes;
            tempRoutes = new WorkingRoutes();
        }

        return createRouteResponse(request, isDone);
    }

    private byte[] createRouteResponse(byte[] request, boolean isDone) {
        byte[] response = new byte[13];
        int index = 0;
        response[index++] = (byte) 0xFD;
        response[index++] = (byte) (response.length - 4);
        response[index++] = 5;
        response[index++] = request[0];
        response[index++] = request[1];
        response[index++] = request[2];
        response[index++] = request[3];
        response[index++] = request[4];
        response[index++] = request[5];
        response[index++] = 1;
        response[index++] = (byte) (isDone ? 'S' : 'N');
        response[index++] = getCheckSum(response);
        response[index++] = (byte) 0xFE;

        return response;
    }

    private byte[] processCurrentRouteRequest(byte[] request) {
        return createCurrentRouteResponse(request);
    }

    private byte[] processSetRouteRequest(byte[] request) {
        Map<String, String> pointActions = new HashMap<>();
        int mapId = convertByte2ToInt(request[2], request[3]);
        int routeId = request[4];
        WorkingRoutes.Route foundRoute = routes.getRoutes().stream()
                .filter(route -> route.getId() == routeId)
                .findFirst()
                .orElse(null);
        if (mapId == routes.getMapId() && foundRoute != null) {
            int index = 6;
            while (index + 5 <= request.length) {
                pointActions.put(new String(new byte[]{
                        request[index++],
                        request[index++],
                        request[index++],
                        request[index++]
                }), new String(new byte[] {request[index++]}));
            }
            routes.setCurrentRoute(foundRoute);
            routes.setPointActions(pointActions);
        }

        return createCurrentRouteResponse(request);
    }

    private byte[] createCurrentRouteResponse(byte[] request) {
        int length = 7 + routes.getPointActions().size() * 5;
        byte[] response = new byte[length + 4];
        int index = 0;
        byte[] mapIdByte = convertIntToByte2(routes.getMapId());
        response[index++] = (byte) 0xFD;
        response[index++] = (byte) length;
        response[index++] = 6;
        response[index++] = request[0];
        response[index++] = request[1];
        response[index++] = mapIdByte[0];
        response[index++] = mapIdByte[1];
        response[index++] = (byte) (routes.getCurrentRoute() != null ? routes.getCurrentRoute().getId() : -1);
        response[index++] = (byte) routes.getPointActions().size();
        for (Map.Entry<String, String> pa : routes.getPointActions().entrySet()) {
            byte[] pointByte = pa.getKey().getBytes();
            response[index++] = pointByte[0];
            response[index++] = pointByte[1];
            response[index++] = pointByte[2];
            response[index++] = pointByte[3];
            response[index++] = (byte) pa.getValue().charAt(0);
        }
        response[index++] = getCheckSum(response);
        response[index++] = (byte) 0xFE;

        return response;
    }

    private int decodePosition(byte b1, byte b2) {
        String pos = new String(new byte[] {b1, b2});
        return Integer.parseInt(pos);
    }

    private byte[] convertPosition(int pos) {
        byte[] result = new byte[2];
        result = String.format("%02d", pos).getBytes();
        return result;
    }

    private byte[] convertIntToByte2(int x) {
        byte[] result = new byte[2];
        result[1] = (byte) (x >> 8);
        result[0] = (byte) x;
        return result;
    }
    public byte[] convertFloatTo4Bytes(float value) {
        int intBits = Float.floatToIntBits(value);
        byte[] result = new byte[4];
        result[0] = (byte) (intBits >> 0);
        result[1] = (byte) (intBits >> 8);
        result[2] = (byte) (intBits >> 16);
        result[3] = (byte) (intBits >> 24);

        return result;
    }

    private byte[] createErrorResponse(byte[] request) {
        byte[] response = new byte[207];

        response[0] = (byte) 0xFD;
        response[1] = (byte) (response.length - 4);
        response[2] = (byte) 3;
        response[3] = request[0];
        response[4] = request[1];
        int i = 5;
        byte[] errorsByte = getErrorsInBytes();
        for (byte b : errorsByte) {
            response[i++] = b;
        }
        response[request.length - 1] = 3;
        response[response.length - 2] = getCheckSum(response);

        return response;
    }

    private byte[] createMovingResponse(byte[] request) {
        byte[] response = new byte[7];
        response[0] = (byte) 0xFD;
        response[1] = (byte) (response.length - 4);
        response[2] = (byte) 4;
        response[3] = request[0];
        response[4] = request[1];
        response[5] = getCheckSum(response);
        response[6] = (byte) 0xFE;

        return response;
    }

    private int convertByte2ToInt(byte b1, byte b2) {
        return b2 * 256 + b1;
    }

    public static byte getCheckSum(byte[] rawContent) {
        int cs = 0;
        int length = rawContent[1] & 0xFF;
        for (int i = 0; i < length; i++) {
            cs ^= rawContent[2 + i];
        }
        return (byte) cs;
    }

    public void closeSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDistanceChange(float distance) {
        this.distance = distance;
    }

    @Override
    public void onError(Map<String, String> errors) {
//        if (!errors.isEmpty()) {
//            if (operationState != 'E') previousStateBeforeError = operationState;
////            if (orderProcess.isAlive()) orderProcess.interrupt();
////            if (loopProcess.isAlive()) loopProcess.interrupt();
//            operationState = 'E';
//        } else {
//            operationState = previousStateBeforeError;
////            if (orderProcess.isInterrupted()) orderProcess.start();
////            if (loopProcess.isInterrupted() && isLoop) loopProcess.start();
//        }
        this.errors = errors;
    }

    @Override
    public void onError(int errors) {
        if (errorCode != 0) {
            if (operationState != 'E') previousStateBeforeError = operationState;
//            if (orderProcess.isAlive()) orderProcess.interrupt();
//            if (loopProcess.isAlive()) loopProcess.interrupt();
            operationState = 'E';
        } else {
            operationState = previousStateBeforeError;
//            if (orderProcess.isInterrupted()) orderProcess.start();
//            if (loopProcess.isInterrupted() && isLoop) loopProcess.start();
        }
        this.errorCode = errors;
    }

    private byte[] getErrorsInBytes() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> error : errors.entrySet()) {
            builder.append(error.getKey()).append("|").append(error.getValue()).append(";");
        }
        return builder.toString().getBytes();
    }
}
