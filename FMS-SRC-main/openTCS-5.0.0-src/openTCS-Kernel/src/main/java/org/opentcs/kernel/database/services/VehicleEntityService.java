package org.opentcs.kernel.database.services;

import com.aubot.hibernate.database.DatabaseSessionFactory;
import org.hibernate.Session;
import org.opentcs.hibernate.entities.VehicleEntity;

public class VehicleEntityService {

  private final DatabaseSessionFactory sessionFactory;


  public VehicleEntityService(DatabaseSessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public VehicleEntity createVehicle(VehicleEntity vehicleEntity) {
    Session session = sessionFactory.getSession();
    session.beginTransaction();
    int id = (int) session.save(vehicleEntity);
    vehicleEntity.setId(id);
    session.getTransaction().commit();
    session.close();

    return vehicleEntity;
  }
}
