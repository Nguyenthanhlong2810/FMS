package cfg.aubot.itteam.simulator;

public class Main {
    public static void main(String[] args) {

        //VirtualAgv agv1 = new VirtualMqttAgv("Vehicle-01", 2);
        //VirtualAgv agv2 = new VirtualMqttAgv("Vehicle-02", 4);
        VirtualTCPAgv agv1 = new VirtualTCPAgv(2020, "0001");
//        VirtualTCPAgv agv2 = new VirtualTCPAgv(2021, 74);
//        VirtualTCPAgv agv3 = new VirtualTCPAgv(2022, 75);
//        VirtualTCPAgv agv4 = new VirtualTCPAgv(2023, 76);
//        VirtualTCPAgv agv5 = new VirtualTCPAgv(2024, 77);
//        VirtualTCPAgv agv6 = new VirtualTCPAgv(2025, 78);
//        AgvVirtualError errorManager = new AgvVirtualError(agv1);
//        agv1.addErrorManager(errorManager);
        agv1.open();
//        agv2.open();
//        agv3.open();
//        agv4.open();
//        agv5.open();
//        agv6.open();
    }
}
