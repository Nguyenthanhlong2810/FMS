package org.opentcs.data.model;

import org.opentcs.data.TCSObjectReference;

import java.io.Serializable;

public class WorkingRoutePoint implements Serializable {

  private final TCSObjectReference<Group> parent;
  private TCSObjectReference<Point> point;
  private VehicleAction action;

  public WorkingRoutePoint(TCSObjectReference<Group> group, TCSObjectReference<Point> point) {
    this.parent = group;
    this.point = point;
  }

  public WorkingRoutePoint(TCSObjectReference<Group> parent) {
    this.parent = parent;
  }

  public TCSObjectReference<Group> getRoute() {
    return parent;
  }

  public String getName() {
    return point.getName();
  }

  public TCSObjectReference<Point> getPoint() {
    return point;
  }

  public VehicleAction getAction() {
    return action;
  }

  public WorkingRoutePoint withAction(VehicleAction action) {
    this.action = action;

    return this;
  }
}
