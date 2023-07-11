package org.opentcs.guing.plugins.themes;

import org.opentcs.data.model.Vehicle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.EnumMap;

import static java.util.Objects.requireNonNull;

public class AubotVehicleTheme {

    private static final String PATH = "/org/opentcs/guing/plugins/themes/symbols/vehicle/";

    private static StatefulImageVehicleTheme defaultTheme = new StatefulImageVehicleTheme();
    private EnumMap<Vehicle.State, Image> unloadedStateMap = new EnumMap<>(Vehicle.State.class);
    private EnumMap<Vehicle.State, Image> loadedStateMap = new EnumMap<>(Vehicle.State.class);
    private Vehicle vehicle;
    private String oldName = "";
    public AubotVehicleTheme() {
    };

    public AubotVehicleTheme updateMaps(String newName) {
        if (newName.equalsIgnoreCase(oldName)) {
            return this;
        }
        String NEW_PATH = newName.equalsIgnoreCase("Default") ? PATH
                : new File("theme").getAbsolutePath() + File.separatorChar + newName + File.separatorChar;
        putImage(unloadedStateMap, Vehicle.State.CHARGING, NEW_PATH  + "unloaded_charging.png");
        putImage(unloadedStateMap, Vehicle.State.ERROR, NEW_PATH + "unloaded_error.png");
        putImage(unloadedStateMap, Vehicle.State.WARNING, NEW_PATH + "unloaded_normal.png");
        putImage(unloadedStateMap, Vehicle.State.EXECUTING, NEW_PATH + "unloaded_normal.png");
        putImage(unloadedStateMap, Vehicle.State.IDLE, NEW_PATH + "unloaded_normal.png");
        putImage(unloadedStateMap, Vehicle.State.UNAVAILABLE, NEW_PATH + "unloaded_normal.png");
        putImage(unloadedStateMap, Vehicle.State.UNKNOWN,NEW_PATH + "unloaded_normal.png");

        putImage(loadedStateMap, Vehicle.State.CHARGING, NEW_PATH + "loaded_charging.png");
        putImage(loadedStateMap, Vehicle.State.ERROR, NEW_PATH + "loaded_error.png");
        putImage(loadedStateMap, Vehicle.State.WARNING, NEW_PATH + "loaded_normal.png");
        putImage(loadedStateMap, Vehicle.State.EXECUTING, NEW_PATH + "loaded_normal.png");
        putImage(loadedStateMap, Vehicle.State.IDLE, NEW_PATH + "loaded_normal.png");
        putImage(loadedStateMap, Vehicle.State.UNAVAILABLE, NEW_PATH + "loaded_normal.png");
        putImage(loadedStateMap, Vehicle.State.UNKNOWN,NEW_PATH + "loaded_normal.png");

        oldName = newName;
        return this;
    }

    private void putImage(EnumMap<Vehicle.State, Image> map, Vehicle.State state, String fileName) {
        try {
            map.put(state, ImageIO.read(new File(fileName)));
        } catch (Exception e) {
            map.put(state, defaultTheme.statefulImage(new Vehicle("temp").withState(state)));
        }
    }

    public Image getImage(Vehicle vehicle) {
        requireNonNull(vehicle, "vehicle");

        return loaded(vehicle)
                ? loadedStateMap.get(vehicle.getState())
                : unloadedStateMap.get(vehicle.getState());
    }

    private boolean loaded(Vehicle vehicle) {
        return vehicle.getLoadHandlingDevices().stream()
                .anyMatch(lhd -> lhd.isFull());
    }
}
