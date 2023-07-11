package org.opentcs.hibernate.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "vehicle")
public class VehicleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private int id;

  @Column(name = "name")
  private String name;

  @Column(name = "type")
  private String type;

  @Column(name = "color")
  private int colorHex;

  @Column(name = "create_at")
  private Timestamp createAt;

  @Column(name = "update_at")
  private Timestamp updateAt;

  @Column(name = "delete_at")
  private Timestamp deleteAt;

  @OneToMany(mappedBy = "vehicle", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private Set<VehiclePropertyEntity> properties;
}
