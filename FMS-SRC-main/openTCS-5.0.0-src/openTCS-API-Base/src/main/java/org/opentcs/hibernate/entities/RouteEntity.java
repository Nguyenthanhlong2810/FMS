package org.opentcs.hibernate.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "route")
public class RouteEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private int id;

  @Column(name = "name")
  private String name;

  @Column(name = "cost")
  private int cost;

  @Column(name = "color")
  private int colorHex;

//  @OneToMany(mappedBy = "routeTemplate")
//  private List<VehicleRouteEntity> vehicleRoutes;

  @OneToMany(mappedBy = "route")
  private List<VehicleRouteHistoryEntity> histories;

  @ManyToOne
  @JoinColumn(name = "map_id")
  private MapPersistedEntity map;

  @Column(name = "route_sequence")
  private int sequence;
}
