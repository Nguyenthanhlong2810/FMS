package org.opentcs.guing.components.routing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.opentcs.guing.components.properties.type.ColorProperty;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
public class Graph {
  @Getter
  private List<Node> nodes = new ArrayList<>();
  @Getter
  private List<Route> foundedRoutes = new ArrayList<>();

  public void addEdge(Edge edge) {
    Node startExists = nodes.stream()
                            .filter(node -> node.getPoint().equals(edge.getStart()))
                            .findFirst()
                            .orElseGet(() -> {
      Node newStart = new Node();
      newStart.setPoint(edge.getStart());
      nodes.add(newStart);
      return newStart;
    });
    Node endExists = nodes.stream()
                          .filter(node -> node.getPoint().equals(edge.getEnd()))
                          .findFirst()
                          .orElseGet(() -> {
      Node newEnd = new Node();
      newEnd.setPoint(edge.getEnd());
      nodes.add(newEnd);
      return newEnd;
    });

    startExists.addEnd(endExists, edge);
  }

  public void removeEdge(Edge edge) {
    nodes.stream()
          .filter(start -> start.getPoint().equals(edge.getStart()))
          .findFirst()
          .ifPresent(exsist -> {
      exsist.getEnds().keySet()
              .stream()
              .filter(end -> end.getPoint().equals(edge.getEnd()))
              .findFirst()
              .ifPresent((node) -> {
        exsist.getEnds().remove(node);
      });
    });
  }

  public Node findNode(String point) {
    return nodes.stream()
            .filter(node -> node.getPoint().equals(point))
            .findFirst()
            .orElse(null);
  }

  public void dfs(Node dest, Node currentNode, Route currentRoute) {
    if (currentNode == dest) {
      return;
    }
    if (currentNode.getEnds().isEmpty()) {
      foundedRoutes.remove(currentRoute);
      return;
    }
    Set<Node> nexts = currentNode.getEnds().keySet();
    Iterator<Node> i = nexts.iterator();
    Node first = i.next();
    while (i.hasNext()) {
      Node node = i.next();
      if (!currentRoute.containEnd(node.getPoint())) {
        Route route = currentRoute.clone();
        route.getRoute().add(currentNode.getEnds().get(node));
        foundedRoutes.add(route);
        dfs(dest, node, route);
      }
    }
    if (currentRoute.containEnd(first.getPoint())) {
      foundedRoutes.remove(currentRoute);
      return;
    }
    currentRoute.getRoute().add(currentNode.getEnds().get(first));
    dfs(dest, first, currentRoute);
  }

  public void searchForCycles(Node start) {
    if (!nodes.contains(start)) {
      return;
    }

    foundedRoutes.clear();
    start.getEnds().keySet().forEach(node -> {
      Route route = new Route();
      route.getRoute().add(start.getEnds().get(node));
      foundedRoutes.add(route);
      dfs(start, node, route);
    });
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public class Route {
    private List<Edge> route = new ArrayList<>();
    private int cost;
    private String name;
    private ColorProperty color;
    private boolean isHighlight;

    @Override
    public Route clone() {
      Route route = new Route();
      route.setCost(cost);
      route.setRoute(new ArrayList<>(this.route));

      return route;
    }

    boolean containStart(String point) {
      return route.stream().anyMatch(step -> step.getStart().equals(point));
    }

    boolean containEnd(String point) {
      return route.stream().anyMatch(step -> step.getEnd().equals(point));
    }

    public byte[] toByteArray() {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte edgeCount = (byte) this.getRoute().size();
      outputStream.write(edgeCount);
      this.getRoute().forEach(edge -> {
        try {
          outputStream.write(edge.toByteArray());
        } catch (IOException ignored) { }
      });
      return outputStream.toByteArray();
    }
  }
}
