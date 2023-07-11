package org.opentcs.hibernate.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "vehicle_route_history")
public class VehicleRouteHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private long id;

  @ManyToOne
  @JoinColumn(name = "vehicle_id")
  private VehicleEntity vehicle;

  @ManyToOne(targetEntity = RouteEntity.class)
  @JoinColumn(name = "route_id")
  private RouteEntity route;

  @Column(name = "complete_at")
  private Timestamp completeAt;

  @Column(name = "battery")
  private int battery;

  @Column(name = "position")
  private String position;
}
