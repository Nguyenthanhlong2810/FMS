package com.aubot.agv.integration.rfid;

public class Rfid {

  private String name;

  private boolean beginIntersection;

  private boolean endIntersection;

  private int intersectionNo;

  private int intersectionRoadNo;

  public Rfid(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isBeginIntersection() {
    return beginIntersection;
  }

  public void setBeginIntersection(boolean beginIntersection) {
    this.beginIntersection = beginIntersection;
    this.endIntersection &= !beginIntersection;
  }

  public boolean isEndIntersection() {
    return endIntersection;
  }

  public void setEndIntersection(boolean endIntersection) {
    this.endIntersection = endIntersection;
    this.beginIntersection &= !endIntersection;
  }

  public int getIntersectionNo() {
    return intersectionNo;
  }

  public void setIntersectionNo(int intersectionNo) {
    this.intersectionNo = intersectionNo;
  }

  public int getIntersectionRoadNo() {
    return intersectionRoadNo;
  }

  public void setIntersectionRoadNo(int intersectionRoadNo) {
    this.intersectionRoadNo = intersectionRoadNo;
  }
}
