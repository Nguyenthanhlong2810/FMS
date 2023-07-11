package org.opentcs.hibernate.entities;

import lombok.Builder;
import lombok.Data;
import org.opentcs.data.model.Vehicle;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "vehicle_history")
public class VehicleHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private VehicleEntity vehicle;

    @Column(name = "state")
    private String state;

    @Column(name = "note")
    private String note;

    @Column(name = "battery")
    private int battery;

    @Column(name = "current")
    private float current;

    @Column(name = "voltage")
    private float voltage;

    @Column(name = "position")
    private String position;

    @Column(name = "time_log")
    private Timestamp timeLog;

}
