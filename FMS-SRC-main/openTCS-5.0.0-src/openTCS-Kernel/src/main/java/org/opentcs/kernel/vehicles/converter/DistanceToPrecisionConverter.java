package org.opentcs.kernel.vehicles.converter;

import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.ModelLayoutElement;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Author: Khoi
 * From COM-AUBOT with LUV
 * Comment using Vietnamese.
 * Class này được tạo ra để xử lý t�?a độ chính xác của xe trên map
 * trong trư�?ng hợp xe không gửi lại t�?a độ 2 chi�?u (x,y) mà gửi lại khoảng cách tính từ điểm hiện tại nó chiếm.
 * Tính toán sử dụng
 */
public class DistanceToPrecisionConverter {

  /**
   * Danh sách các điểm đi�?u khiển trong trư�?ng hợp đư�?ng đi của xe là đư�?ng cong.
   */
  private ArrayList<Point2D.Double> ctrlPoints = new ArrayList<>();
  /**
   * �?iểm trước đó của xe được chuyển đổi dựa theo khoảng cách được gửi v�? ở lần trước đó.
   */
  private Point2D.Double previousPoint;

  /**
   * �?ộ chia của vùng map (thử nghiệm ở mm, chưa thử nghiệm ở các đơn vị kia)
   */
  private double scaleX;
  private double scaleY;
  /**
   * �?ộ chia của đư�?ng mà xe đi, tức là số lượng điểm giả định trên đư�?ng đó.
   */
  private double division;
  private double t = 0;
  private double previousLength = 0;

  public DistanceToPrecisionConverter() {};

  public void setScale(double scaleX, double scaleY) {
    this.scaleX = scaleX;
    this.scaleY = scaleY;
  }

  public DistanceToPrecisionConverter(double scaleX, double scaleY) {
    this.scaleX = scaleX;
    this.scaleY = scaleY;
  }

  public boolean comparePoint(Point2D.Double ctrlPoint, Point point) {
    double x = (double) point.getPosition().getX() / scaleX;
    double y = (double) point.getPosition().getY() / scaleY * (-1);
    return (ctrlPoint.x == x && ctrlPoint.y == y);
  }

  public boolean isOldPath(Point srcPoint, Point desPoint) {
    return (ctrlPoints.size() != 0
            && comparePoint(ctrlPoints.get(0), srcPoint)
            && comparePoint(ctrlPoints.get(ctrlPoints.size() - 1), desPoint));
  }

  public void updatePath(Point srcPoint, Point desPoint, ModelLayoutElement mle) {
    ctrlPoints.clear();
    ctrlPoints.add(new Point2D.Double(
        (double) srcPoint.getPosition().getX() / scaleX,
        (double) srcPoint.getPosition().getY() / scaleY * (-1)
    ));
    if (mle.getProperties().get("CONN_TYPE").equals("BEZIER")
        || mle.getProperties().get("CONN_TYPE").equals("BEZIER_3")) {
      addControlPoints(mle.getProperties().get("CONTROL_POINTS"));
    }

    ctrlPoints.add(new Point2D.Double(
        (double) desPoint.getPosition().getX() / scaleX,
        (double) desPoint.getPosition().getY() / scaleY * (-1)
    ));
    division = 1 / (getLength(100) * 2);
    t = division;
    previousLength = 0;
    previousPoint = ctrlPoints.get(0);
  }

  public void addControlPoints(String ctrlPointsString) {
    String[] points = ctrlPointsString.split(";");
    for (String point : points) {
      String[] coordinate = point.split(",");
      Point2D.Double ctrlPoint = new Point2D.Double(
          Double.parseDouble(coordinate[0]),
          Double.parseDouble(coordinate[1])
      );
      ctrlPoints.add(ctrlPoint);
    }
  }

  public Triple toPrecisePosition(Point2D.Double point) {
    long x = (long) (point.x * scaleX);
    long y = (long) (point.y * scaleY * (-1));

    return new Triple(x, y, 0);
  }

  public double toAngle(Point2D.Double point) {
    if (point.x == previousPoint.x) {
      return point.y > previousPoint.y ? -90 : 90;
    } else if (point.y == previousPoint.y) {
      return point.x > previousPoint.x ? 0 : 180;
    }
    double angle = Math.toDegrees(Math.atan(-(point.y - previousPoint.y) / (point.x - previousPoint.x)));
    return (point.x <= previousPoint.x) ? angle + 180 : angle;
  }

  public Point2D.Double getPointByPath(double length) {
    //Unit mm - modify scale to fit layout
    length = length / ((scaleX + scaleY) / 2);

    if (previousLength >= length) {
      length = previousLength + length;
    }
    Point2D.Double currentPoint;
    double i = t;
    while (i <= 1) {
      currentPoint = getPointByParameter(i);
      double currentLength = previousLength + distance(previousPoint, currentPoint);
      if (currentLength >= length) {
        return currentPoint;
      }
      i += division;
      t = i;
      previousPoint = currentPoint;
      previousLength = currentLength;
    }
    return ctrlPoints.get(ctrlPoints.size() - 1);
  }

  public Point2D.Double getPointByParameter(double t) {
    int n = ctrlPoints.size() - 1;
    Point2D.Double point = new Point2D.Double(0, 0);
    for (int i = 0; i <= n; i++) {
      point.x += combinations(i, n) * Math.pow(1 - t, n - i) * Math.pow(t, i) * ctrlPoints.get(i).x;
      point.y += combinations(i, n) * Math.pow(1 - t, n - i) * Math.pow(t, i) * ctrlPoints.get(i).y;
    }
    return point;
  }

  public double distance(Point2D.Double p1, Point2D.Double p2) {
    return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
  }

  public double getLength(double division) {
    Point2D.Double previousPoint = ctrlPoints.get(0);
    double t = 1 / division;
    double l = 0;
    for (double i = t; i < 1; i += t) {
      Point2D.Double currentPoint = getPointByParameter(i);
      double distance = distance(previousPoint, currentPoint);
      l += distance;
      previousPoint = currentPoint;
    }
    return l;
  }

  public long combinations(int k, int n) {
    return factorial(n) / factorial(k) / factorial(n - k);
  }

  public long factorial(int n) {
    long p = 1;
    while (n != 0) {
      p *= n;
      n--;
    }
    return p;
  }
}
