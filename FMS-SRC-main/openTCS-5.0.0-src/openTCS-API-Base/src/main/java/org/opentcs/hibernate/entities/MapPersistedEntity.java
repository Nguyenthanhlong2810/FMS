package org.opentcs.hibernate.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "map_persisted")
public class MapPersistedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private int id;

  @Column(name = "name")
  private String name;

  @Column(name = "persist_at")
  private Timestamp persistAt;

  @Column(name = "client_ip")
  private String clientIp;
}
