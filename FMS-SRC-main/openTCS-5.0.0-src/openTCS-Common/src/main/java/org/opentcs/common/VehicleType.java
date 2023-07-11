package org.opentcs.common;

public enum VehicleType {
    T500,
    C450,
    C50,
    CRA200,
    PM1600,
    R4,
    A500,
    OTHER;
    public static VehicleType parseVehicleType(String type){
        switch (type){
            case "T500":
                return VehicleType.T500;
            case "C450":
                return VehicleType.C450;
            case "C50":
                return VehicleType.C50;
            case "CRA200":
                return VehicleType.CRA200;
            case "PM1600":
                return VehicleType.PM1600;
            case "R4":
                return VehicleType.R4;
            case "A500":
                return VehicleType.A500;
            default:
                return VehicleType.OTHER;
        }
    }
}
