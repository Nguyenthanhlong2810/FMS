package org.opentcs.data.model;


import org.opentcs.data.TCSObjectReference;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class WorkingRoute implements Serializable {

  private int mapId = -1;

  private int routeId;

  private TCSObjectReference<Group> currentGroup;

  private List<WorkingRoutePoint> currentGroupDetail = new ArrayList<>();

  private TCSObjectReference<Point> circleMark;

  public WorkingRoute(int mapId) {
    this.mapId = mapId;
  }

  public WorkingRoute(int mapId, Group group, List<WorkingRoutePoint> currentGroupDetail) {
    this.mapId = mapId;
    this.routeId = group.getId();
    this.currentGroup = group.getReference();
    this.currentGroupDetail = currentGroupDetail;
  }

  public int getMapId() {
    return mapId;
  }

  public TCSObjectReference<Group> getCurrentGroup() {
    return currentGroup;
  }

  public List<WorkingRoutePoint> getCurrentGroupDetail() {
    return currentGroupDetail;
  }

  public int getRouteId() {
    return routeId;
  }

  public WorkingRouteRaw toRaw() {
    return new WorkingRouteRaw(mapId, routeId,
            currentGroupDetail.stream()
                    .collect(Collectors.toMap(WorkingRoutePoint::getName,
                            wrp -> wrp.getAction().getPresentation())));
  }

  public WorkingRoute setCurrentGroup(Group group) {
    this.routeId = group.getId();
    this.currentGroup = group.getReference();
    return this;
  }

  public WorkingRoute setCurrentGroupDetail(List<WorkingRoutePoint> currentGroupDetail) {
    this.currentGroupDetail = currentGroupDetail;
    return this;
  }

  public boolean haveRoute() {
    return currentGroup != null;
  }

  public WorkingRoute setMapId(int mapId) {
    this.mapId = mapId;
    return this;
  }

  public TCSObjectReference<Point> getCircleMark() {
    return circleMark;
  }

  public WorkingRoute setCircleMark(TCSObjectReference<Point> circleMark) {
    this.circleMark = circleMark;
    return this;
  }

  public static class WorkingRouteRaw implements Serializable {

    private int mapId;

    private int routeId = -1;

    private Map<String, Character> pointActions = new HashMap<>();

    public WorkingRouteRaw(int mapId, int routeId, Map<String, Character> pointActions) {
      this.mapId = mapId;
      this.routeId = routeId;
      this.pointActions = pointActions;
    }

    public WorkingRouteRaw(int mapId) {
      this.mapId = mapId;
    }

    public int getMapId() {
      return mapId;
    }

    public WorkingRouteRaw setMapId(int mapId) {
      this.mapId = mapId;
      return this;
    }

    public int getRouteId() {
      return routeId;
    }

    public WorkingRouteRaw setRouteId(int routeId) {
      this.routeId = routeId;
      return this;
    }

    public Map<String, Character> getPointActions() {
      return pointActions;
    }

    public WorkingRouteRaw setPointActions(Map<String, Character> pointActions) {
      this.pointActions = pointActions;
      return this;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      WorkingRouteRaw that = (WorkingRouteRaw) o;
      return mapId == that.mapId && routeId == that.routeId && Objects.equals(pointActions, that.pointActions);
    }

    @Override
    public int hashCode() {
      return Objects.hash(mapId, routeId, pointActions);
    }
  }
}
