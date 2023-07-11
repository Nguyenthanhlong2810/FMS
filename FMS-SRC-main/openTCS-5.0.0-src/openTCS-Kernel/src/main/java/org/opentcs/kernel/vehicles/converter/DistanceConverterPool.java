package org.opentcs.kernel.vehicles.converter;

import java.util.HashMap;
import java.util.Map;

public class DistanceConverterPool {

    private double scaleX;
    private double scaleY;

    private Map<String, DistanceToPrecisionConverter> converterPool = new HashMap<>();

    public DistanceConverterPool() {};

    public void addConverter(String vehicleName) {
        converterPool.put(vehicleName, new DistanceToPrecisionConverter(scaleX, scaleY));
    }

    public void removeConverter(String vehicleName) {
        converterPool.remove(vehicleName);
    }

    public void setupScale(double scaleX, double scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        for (Map.Entry<String, DistanceToPrecisionConverter> entry : converterPool.entrySet()) {
            DistanceToPrecisionConverter converter = entry.getValue();
            converter.setScale(scaleX, scaleY);
        }
    }

    public DistanceToPrecisionConverter getConverter(String vehicleName) {
        return converterPool.get(vehicleName);
    }

    public void clearPool() {
        converterPool.clear();
    }
}
