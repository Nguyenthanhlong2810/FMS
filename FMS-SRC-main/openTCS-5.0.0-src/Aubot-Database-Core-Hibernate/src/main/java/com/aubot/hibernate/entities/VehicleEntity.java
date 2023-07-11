package com.aubot.hibernate.entities;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Set;
import javax.persistence.*;

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

  @OneToMany(mappedBy = "vehicle", fetch = FetchType.EAGER, cascade = CascadeType.ALL,orphanRemoval = true)
  private Set<VehiclePropertyEntity> properties;
}
