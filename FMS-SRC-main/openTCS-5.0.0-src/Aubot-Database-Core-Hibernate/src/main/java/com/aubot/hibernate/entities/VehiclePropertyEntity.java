package com.aubot.hibernate.entities;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
@Getter
@Setter
@Entity
@Table(name = "vehicle_property")
public class VehiclePropertyEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private VehicleEntity vehicle;

    @Column(name = "key")
    private String key;

    @Column(name = "value")
    private String value;
}
