package org.opentcs.guing.components.routing;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
public class Node {

  private String point;
  private Map<Node, Edge> ends = new HashMap<>();

  public String getPoint() {
    return point;
  }

  public Node setPoint(String point) {
    this.point = point;
    return this;
  }

  public Map<Node, Edge> getEnds() {
    return ends;
  }

  public Node setEnds(Map<Node, Edge> ends) {
    this.ends = ends;
    return this;
  }

  public void addEnd(Node node, Edge edge) {
    if (!ends.containsKey(node)) {
      ends.put(node, edge);
    }
  }

  public void removeEnd(Node node) {
    ends.remove(node);
  }
}
